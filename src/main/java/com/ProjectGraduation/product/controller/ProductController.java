package com.ProjectGraduation.product.controller;
import com.ProjectGraduation.auth.entity.Merchant;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.MerchantService;
import com.ProjectGraduation.product.entity.Category;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService service;

    @Autowired
    private JWTService jwtService ;

    @Autowired
    private MerchantService merchantService ;

    @PostMapping()
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Product addNewProduct(
            @RequestHeader("Authorization") String token,
            @RequestPart("file") MultipartFile file,
            @RequestPart("name") String name,
            @RequestPart("short_description") String shortDescription,
            @RequestPart("long_description") String longDescription,
            @RequestPart("price") String price,
            @RequestPart("quantity") String quantity,
            @RequestPart("active") String active,
            @RequestPart("category_id") String categoryId) throws Exception {

        // Extract merchant username from token
        String merchantUsername = jwtService.getUsername(token.replace("Bearer ", ""));

        // Fetch merchant entity by username (assumes a method to get merchant by username exists)
        Merchant merchant = merchantService.getMerchantByUsername(merchantUsername);

        Product product = new Product();
        product.setName(name);
        product.setShortDescription(shortDescription);
        product.setLongDescription(longDescription);
        product.setPrice(Double.parseDouble(price));
        product.setQuantity(Double.parseDouble(quantity));
        product.setActive(Boolean.parseBoolean(active));
        product.setMerchant(merchant);

        Category category = service.getCategoryById(Long.parseLong(categoryId));
        product.setCategory(category);

        return service.addNewProduct(product, file);
    }
    @PutMapping()
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Product updateProduct(
            @RequestHeader("Authorization") String token,
            @RequestPart("product_id") String productId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("name") String name,
            @RequestPart("short_description") String shortDescription,
            @RequestPart("long_description") String longDescription,
            @RequestPart("price") String price,
            @RequestPart("quantity") String quantity,
            @RequestPart("active") String active,
            @RequestPart("category_id") String categoryId) throws Exception {

        // Extract merchant username from token
        String merchantUsername = jwtService.getUsername(token.replace("Bearer ", ""));

        // Fetch merchant entity by username
        Merchant merchant = merchantService.getMerchantByUsername(merchantUsername);

        Product product = new Product();
        product.setName(name);
        product.setShortDescription(shortDescription);
        product.setLongDescription(longDescription);
        product.setPrice(Double.parseDouble(price));
        product.setQuantity(Double.parseDouble(quantity));
        product.setActive(Boolean.parseBoolean(active));
        product.setMerchant(merchant); // Associate the product with the merchant

        Category category = service.getCategoryById(Long.parseLong(categoryId));
        product.setCategory(category);

        return service.updateProduct(Long.parseLong(productId), product, file);
    }

    @GetMapping("/active")
    public List<Product> getAllActiveProduct() {
        return service.getAllActiveProduct();
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public List<Product> getAllProduct() {
        return service.getAllProduct();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Product getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public void deleteById(@RequestHeader("Authorization") String token, @PathVariable Long id) throws Exception {
        // Extract merchant username from JWT token
        String merchantUsername = jwtService.getUsername(token.replace("Bearer ", ""));

        // Fetch merchant entity by username
        Merchant merchant = merchantService.getMerchantByUsername(merchantUsername);

        // Find the product by ID
        Product product = service.getById(id);

        // Ensure that the product belongs to the merchant
        if (!product.getMerchant().getId().equals(merchant.getId())) {
//            ResponseEntity.ok("You are not authorized to delete this product.");
            throw new IllegalStateException("You are not authorized to delete this product.");
        }

        // If the merchant owns the product, delete it
        service.deleteById(id);
    }

    @GetMapping("/category/{id}")
    public List<Product> getProductsByCategory(@PathVariable Long id) {
        return service.getProductsByCategory(id);
    }
}
