
package com.ProjectGraduation.product.controller;

import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.category.entity.Category;
import com.ProjectGraduation.product.dto.ProductDTO;
import com.ProjectGraduation.product.dto.ProductResponse;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.exception.*;
import com.ProjectGraduation.category.service.CategoryService;
import com.ProjectGraduation.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final JWTService jwtService;
    private final UserService userService;
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> addNewProduct(
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute ProductDTO productRequest) {
        try {
            String merchantUsername = jwtService.getUsername(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(merchantUsername);

            Product product = new Product();
            product.setName(productRequest.getName());
            product.setShortDescription(productRequest.getShortDescription());
            product.setLongDescription(productRequest.getLongDescription());
            product.setPrice(productRequest.getPrice());
            product.setQuantity(productRequest.getQuantity());
            product.setActive(productRequest.getActive());
            product.setUser(user);

            Category category = categoryService.getCategoryById(productRequest.getCategoryId());
            product.setCategory(category);
            product.setColors(productRequest.getColors());

            Product savedProduct = productService.addNewProduct(product, productRequest.getFile());
            return ResponseEntity.ok(
                    new ApiResponse(true,"Product added successfully", savedProduct)
            );
        } catch (InvalidProductDataException | UnauthorizedMerchantException | CategoryNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false,"Failed",ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false,"Failed to add product: " , ex.getMessage()));
        }
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> updateProduct(
            @RequestHeader("Authorization") String token,
            @RequestParam("product_id") Long productId,
            @Valid @ModelAttribute ProductDTO productRequest) {
        try {
            String merchantUsername = jwtService.getUsername(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(merchantUsername);

            Product product = new Product();
            product.setName(productRequest.getName());
            product.setShortDescription(productRequest.getShortDescription());
            product.setLongDescription(productRequest.getLongDescription());
            product.setPrice(productRequest.getPrice());
            product.setQuantity(productRequest.getQuantity());
            product.setActive(productRequest.getActive());
            product.setUser(user);
            product.setColors(productRequest.getColors());

            Category category = categoryService.getCategoryById(productRequest.getCategoryId());
            product.setCategory(category);

            Product updatedProduct = productService.updateProduct(productId, product, productRequest.getFile());
            return ResponseEntity.ok(
                    new ApiResponse(true,"Product updated successfully", updatedProduct)
            );
        } catch (ProductNotFoundException | UnauthorizedMerchantException | CategoryNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false,"Filed",ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false,"Failed to update product: " , ex.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false,"Validation failed", errors));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getAllActiveProduct() {
        try {
            List<ProductResponse> products = productService.getAllActiveProducts();
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
            List<ProductResponse> products = productService.getAllProducts();
            return ResponseEntity.ok(new ApiResponse(true, "All products retrieved successfully", products));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve products: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable Long id) {
        try {
            ProductResponse product = productService.findById(id);
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
            List<ProductResponse> products = productService.filterByCategoryId(categoryId);
            return ResponseEntity.ok(new ApiResponse(true, "Products by category retrieved successfully", products));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Failed to retrieve products by category: " + ex.getMessage(), null));
        }
    }


    @GetMapping("/filter/color")
    public ResponseEntity<List<ProductResponse>> filterByColor(@RequestParam String color) {
        return ResponseEntity.ok(productService.filterByColor(color));
    }

    @GetMapping("/filter/price")
    public ResponseEntity<List<ProductResponse>> filterByPrice(@RequestParam(required = false) Double min,
                                                               @RequestParam(required = false) Double max) {
        return ResponseEntity.ok(productService.filterByPriceRange(min, max));
    }


    @GetMapping("/merchant/{id}")
    public ResponseEntity<List<ProductResponse>> getMerchantById(@PathVariable Long id) {
        User user = userService.getUserByID(id);
        return ResponseEntity.ok(productService.findMerchantProducts(user));
    }

}