package com.ProjectGraduation.product.service;
import com.ProjectGraduation.product.entity.Category;
import com.ProjectGraduation.product.entity.Product;
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

    @Autowired
    private ProductRepo repo;

    @Autowired
    private CategoryRepo categoryRepository;

    @Autowired
    private FileService fileService;

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
    }

    public Product addNewProduct(Product product, MultipartFile file) throws Exception {
        String uploadedFileName = fileService.uploadFile(path, file);
        product.setMedia(uploadedFileName);

        // Ensure merchant is set (if not already handled in the controller)
//        if (product.getMerchant() == null) {
//            throw new IllegalArgumentException("Merchant must be provided for the product.");
//        }

        return repo.save(product);
    }

    public Product updateProduct(Long productId, Product product, MultipartFile file) throws Exception {
        Product existingProduct = repo.findById(productId)
                .orElseThrow(() -> new Exception("Product not found with ID: " + productId));

        if (file != null && !file.isEmpty()) {
            if (existingProduct.getMedia() != null) {
                Files.deleteIfExists(Paths.get(path + File.separator + existingProduct.getMedia()));
            }
            String uploadedFileName = fileService.uploadFile(path, file);
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
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
    }

    public String deleteById(Long productId) throws Exception {
        Product product = repo.findById(productId)
                .orElseThrow(() -> new Exception("Product not found with ID: " + productId));
        Files.deleteIfExists(Paths.get(path + File.separator + product.getMedia()));
        repo.delete(product);
        return "Product deleted with ID: " + productId;
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return repo.findProductsByCategory(categoryId);
    }
}
