package com.ProjectGraduation.SaveProduct.controller;

import com.ProjectGraduation.SaveProduct.service.SaveProductService;
import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/saving-products")
@RequiredArgsConstructor
public class SaveProductController {

    private final SaveProductService saveService;

    @PostMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> saveProduct(@PathVariable Long productId,
                                                   @RequestHeader("Authorization") String token) {
        try {
            saveService.saveProduct(productId, token);
            return ResponseEntity.ok(new ApiResponse(true, "Product saved successfully!", null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(new ApiResponse(false, "Unauthorized: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Error: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> unSaveProduct(@PathVariable Long productId,
                                                     @RequestHeader("Authorization") String token) {
        try {
            saveService.unSaveProduct(productId, token);
            return ResponseEntity.ok(new ApiResponse(true, "Product unsaved successfully!", null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(new ApiResponse(false, "Unauthorized: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> getAllSavedProducts(@RequestHeader("Authorization") String token) {
        List<Product> savedProducts = saveService.getAllSavedProducts(token);
        return ResponseEntity.ok(new ApiResponse(true, "Saved products fetched successfully", savedProducts));
    }
}
