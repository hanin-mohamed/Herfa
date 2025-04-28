package com.ProjectGraduation.product.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.product.entity.Category;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.exception.*;
import com.ProjectGraduation.product.repo.CategoryRepo;
import com.ProjectGraduation.product.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepo repo;
    private final CategoryRepo categoryRepository;
    private final FileService fileService;

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    @Autowired
    public ProductService(ProductRepo repo, CategoryRepo categoryRepository, FileService fileService) {
        this.repo = repo;
        this.categoryRepository = categoryRepository;
        this.fileService = fileService;
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
    }

    public Product addNewProduct(Product product, MultipartFile file) throws Exception {
        User user = product.getUser();
        if (user == null) {
            throw new InvalidProductDataException("Merchant must be provided for the product");
        }
        if (!"MERCHANT".equals(user.getRole().toString())) {
            throw new UnauthorizedMerchantException("Only merchants can add products");
        }
        if (product.getName() == null || product.getName().isEmpty()) {
            throw new InvalidProductDataException("Product name cannot be empty");
        }
        if (product.getPrice() <= 0) {
            throw new InvalidProductDataException("Price must be greater than zero");
        }
        if (file == null || file.isEmpty()) {
            throw new InvalidProductDataException("Product image file is required");
        }

        String uploadedFileName = fileService.uploadFile(path, file, product.getUser().getId(), "product", product.getName());
        product.setMedia(uploadedFileName);

        return repo.save(product);
    }

    public Product updateProduct(Long productId, Product product, MultipartFile file) throws Exception {
        Product existingProduct = repo.findById(productId)
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

        return repo.save(existingProduct);
    }

    public List<Product> getAllActiveProduct() {
        return repo.findActiveProducts(true);
    }

    public List<Product> getAllProduct() {
        return repo.findAll();
    }

    public Product getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
    }

    public void deleteById(Long productId) throws Exception {
        Product product = repo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        Files.deleteIfExists(Paths.get(path + File.separator + product.getMedia()));
        repo.delete(product);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return repo.findProductsByCategory(categoryId);
    }
    public List<Product> getProductsByIds(List<Long> ids) {
        return repo.findAllById(ids);
    }
    public void saveProduct(Product product) {
        repo.save(product);
    }

    public List<Product> getMerchantProducts(User user) {
        return repo.findAllByUserId(user);
    }

}