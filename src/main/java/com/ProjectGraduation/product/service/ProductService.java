package com.ProjectGraduation.product.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.file.CloudinaryService;
import com.ProjectGraduation.offers.productoffer.service.ProductOfferService;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.exception.*;
import com.ProjectGraduation.product.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductOfferService productOfferService;
    @Value("${project.poster}")
    private String path;

    public Product addNewProduct(Product product, MultipartFile file) throws Exception {
        User user = product.getUser();
        if (user == null) {
            throw new InvalidProductDataException("Merchant must be provided for the product");
        }

        if (!"MERCHANT".equalsIgnoreCase(user.getRole().name())) {
            throw new UnauthorizedMerchantException("Only merchants can add products");
        }

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new InvalidProductDataException("Product name cannot be empty");
        }

        if (product.getPrice() <= 0) {
            throw new InvalidProductDataException("Price must be greater than zero");
        }

        if (file == null || file.isEmpty()) {
            throw new InvalidProductDataException("Product image file is required");
        }

        String uploadedFileName = cloudinaryService.uploadImage(  file ,"product", user.getId());
        product.setMedia(uploadedFileName);

        if (product.getColors() == null || product.getColors().isEmpty()) {
            product.setColors(List.of(" "));
        }

        return productRepository.save(product);
    }


    public Product updateProduct(Long productId, Product product, MultipartFile file) throws Exception {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        User user = product.getUser();
        if (!"MERCHANT".equals(user.getRole().toString())) {
            throw new UnauthorizedMerchantException("Only merchants can update products");
        }

        if (file == null && file.isEmpty()) {
            if (existingProduct.getMedia() != null) {
                Files.deleteIfExists(Paths.get(path + File.separator + existingProduct.getMedia()));
            }
            String uploadedFileName = cloudinaryService.uploadImage(  file ,"product", user.getId());
            existingProduct.setMedia(uploadedFileName);
        }

        existingProduct.setName(product.getName());
        existingProduct.setShortDescription(product.getShortDescription());
        existingProduct.setLongDescription(product.getLongDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setActive(product.getActive());

        existingProduct.setColors(product.getColors());

        return productRepository.save(existingProduct);
    }

    public List<Product> getAllActiveProduct() {
        return productRepository.findActiveProducts(true);
    }

    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        double discountedPrice = productOfferService.getDiscountedPrice(product);
        product.setDiscountedPrice(discountedPrice);
        return product;
    }

    public void deleteById(Long productId) throws Exception {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        Files.deleteIfExists(Paths.get(path + File.separator + product.getMedia()));
        productRepository.delete(product);
    }

    public List<Product> getProductsByIds(List<Long> ids) {
        return productRepository.findAllById(ids);
    }
    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public List<Product> getMerchantProducts(User user) {
        return productRepository.findAllByUser(user);
    }
    public List<Product> filterByCategoryId(Long id) {
        return productRepository.findByCategoryId(id);
    }

    public List<Product> filterByColor(String color) {
        List<Product> all = productRepository.findActiveProducts(true);
        if (color == null || color.isBlank()) return all;

        return all.stream()
                .filter(p -> p.getColors() != null &&
                        p.getColors().stream().anyMatch(c -> c.equalsIgnoreCase(color)))
                .toList();
    }

    public List<Product> filterByPriceRange(Double min, Double max) {
        return productRepository.findActiveProducts(true).stream()
                .filter(p -> (min == null || p.getPrice() >= min) &&
                        (max == null || p.getPrice() <= max))
                .toList();
    }

}