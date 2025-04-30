package com.ProjectGraduation.product.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.offer.service.OfferService;
import com.ProjectGraduation.product.entity.Category;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.exception.*;
import com.ProjectGraduation.product.repo.CategoryRepo;
import com.ProjectGraduation.product.repo.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo productRepo;
    private final FileService fileService;
    private final OfferService productOfferService;
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

        String uploadedFileName = fileService.uploadFile(path, file, user.getId(), "product", product.getName());
        product.setMedia(uploadedFileName);

        if (product.getColors() == null || product.getColors().isEmpty()) {
            product.setColors(List.of(" "));
        }

        return productRepo.save(product);
    }


    public Product updateProduct(Long productId, Product product, MultipartFile file) throws Exception {
        Product existingProduct = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        User user = product.getUser();
        if (!"MERCHANT".equals(user.getRole().toString())) {
            throw new UnauthorizedMerchantException("Only merchants can update products");
        }

        if (file != null && !file.isEmpty()) {
            if (existingProduct.getMedia() != null) {
                Files.deleteIfExists(Paths.get(path + File.separator + existingProduct.getMedia()));
            }
            String uploadedFileName = fileService.uploadFile(path, file, product.getUser().getId(), "product", product.getName());
            existingProduct.setMedia(uploadedFileName);
        }

        existingProduct.setName(product.getName());
        existingProduct.setShortDescription(product.getShortDescription());
        existingProduct.setLongDescription(product.getLongDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setActive(product.getActive());

        return productRepo.save(existingProduct);
    }

    public List<Product> getAllActiveProduct() {
        return productRepo.findActiveProducts(true);
    }

    public List<Product> getAllProduct() {
        return productRepo.findAll();
    }

    public Product getById(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        double discountedPrice = productOfferService.getDiscountedPrice(product);
        product.setDiscountedPrice(discountedPrice);
        return product;
    }

    public void deleteById(Long productId) throws Exception {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        Files.deleteIfExists(Paths.get(path + File.separator + product.getMedia()));
        productRepo.delete(product);
    }

    public List<Product> getProductsByIds(List<Long> ids) {
        return productRepo.findAllById(ids);
    }
    public void saveProduct(Product product) {
        productRepo.save(product);
    }

    public List<Product> getMerchantProducts(User user) {
        return productRepo.findAllByUser(user);
    }
    public List<Product> filterByCategoryId(Long id) {
        return productRepo.findByCategoryId(id);
    }

    public List<Product> filterByColor(String color) {
        List<Product> all = productRepo.findActiveProducts(true);
        if (color == null || color.isBlank()) return all;

        return all.stream()
                .filter(p -> p.getColors() != null &&
                        p.getColors().stream().anyMatch(c -> c.equalsIgnoreCase(color)))
                .toList();
    }

    public List<Product> filterByPriceRange(Double min, Double max) {
        return productRepo.findActiveProducts(true).stream()
                .filter(p -> (min == null || p.getPrice() >= min) &&
                        (max == null || p.getPrice() <= max))
                .toList();
    }


}