package com.ProjectGraduation.product.controller;

import com.ProjectGraduation.auth.api.model.ApiResponse;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.category.entity.Category;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.exception.*;
import com.ProjectGraduation.category.service.CategoryService;
import com.ProjectGraduation.product.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final JWTService jwtService;
    private final UserService userService;
    private final CategoryService categoryService;

    @PostMapping()
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> addNewProduct(
            @RequestHeader("Authorization") String token,
            @RequestPart("file") MultipartFile file,
            @RequestPart("name") String name,
            @RequestPart("short_description") String shortDescription,
            @RequestPart("long_description") String longDescription,
            @RequestPart("price") String price,
            @RequestPart("quantity") String quantity,
            @RequestPart("active") String active,
            @RequestPart("category_id") String categoryId,
            @RequestPart("colors") String colors) {
        try {
            String merchantUsername = jwtService.getUsername(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(merchantUsername);

            Product product = new Product();
            product.setName(name);
            product.setShortDescription(shortDescription);
            product.setLongDescription(longDescription);
            product.setPrice(Double.parseDouble(price));
            product.setQuantity(Integer.parseInt(quantity));
            product.setActive(Boolean.parseBoolean(active));
            product.setUser(user);
            Category category = categoryService.getCategoryById(Long.parseLong(categoryId));
            product.setCategory(category);
            product.setColors(Collections.singletonList(colors));
            Product savedProduct = productService.addNewProduct(product, file);
            return ResponseEntity.ok(new ApiResponse(true, "Product added successfully", savedProduct));
        } catch (InvalidProductDataException | UnauthorizedMerchantException | CategoryNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to add product: " + ex.getMessage(), null));
        }
    }

    @PutMapping()
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> updateProduct(
            @RequestHeader("Authorization") String token,
            @RequestPart("product_id") String productId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("name") String name,
            @RequestPart("short_description") String shortDescription,
            @RequestPart("long_description") String longDescription,
            @RequestPart("price") String price,
            @RequestPart("quantity") String quantity,
            @RequestPart("active") String active,
            @RequestPart("category_id") String categoryId,
            @RequestPart("colors") String colors) {
        try {
            String merchantUsername = jwtService.getUsername(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(merchantUsername);

            Product product = new Product();
            product.setName(name);
            product.setShortDescription(shortDescription);
            product.setLongDescription(longDescription);
            product.setPrice(Double.parseDouble(price));
            product.setQuantity(Integer.parseInt(quantity));
            product.setActive(Boolean.parseBoolean(active));
            product.setUser(user);

            ObjectMapper objectMapper = new ObjectMapper();
            List<String> color = objectMapper.readValue(colors, new TypeReference<List<String>>() {});
            product.setColors(color);
            Category category = categoryService.getCategoryById(Long.parseLong(categoryId));
            product.setCategory(category);

            Product updatedProduct = productService.updateProduct(Long.parseLong(productId), product, file);
            return ResponseEntity.ok(new ApiResponse(true, "Product updated successfully", updatedProduct));
        } catch (ProductNotFoundException | UnauthorizedMerchantException | CategoryNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to update product: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getAllActiveProduct() {
        try {
            List<Product> products = productService.getAllActiveProduct();
            return ResponseEntity.ok(new ApiResponse(true, "Active products retrieved successfully", products));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve active products: " + ex.getMessage(), null));
        }
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> getAllProduct() {
        try {
            List<Product> products = productService.getAllProduct();
            return ResponseEntity.ok(new ApiResponse(true, "All products retrieved successfully", products));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve products: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable Long id) {
        try {
            Product product = productService.getById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Product retrieved successfully", product));
        } catch (ProductNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve product: " + ex.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> deleteById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        try {
            String merchantUsername = jwtService.getUsername(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(merchantUsername);

            Product product = productService.getById(id);
            if (!product.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "You are not authorized to delete this product", null));
            }

            productService.deleteById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Product deleted successfully", null));
        } catch (ProductNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to delete product: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/filter/category/{categoryId}")
    public ResponseEntity<ApiResponse> filterByCategoryId(@PathVariable Long categoryId) {
        try {
            List<Product> products = productService.filterByCategoryId(categoryId);
            return ResponseEntity.ok(new ApiResponse(true, "Products by category retrieved successfully", products));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Failed to retrieve products by category: " + ex.getMessage(), null));
        }
    }
    @GetMapping("/filter/color")
    public ResponseEntity<List<Product>> filterByColor(@RequestParam String color) {
        return ResponseEntity.ok(productService.filterByColor(color));
    }
    @GetMapping("/filter/price")
    public ResponseEntity<List<Product>> filterByPrice(@RequestParam(required = false) Double min,
                                                       @RequestParam(required = false) Double max) {
        return ResponseEntity.ok(productService.filterByPriceRange(min, max));
    }

}