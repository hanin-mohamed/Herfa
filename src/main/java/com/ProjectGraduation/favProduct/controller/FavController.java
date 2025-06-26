package com.ProjectGraduation.favProduct.controller;

import com.ProjectGraduation.favProduct.service.FavService;
import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favourites")
@RequiredArgsConstructor
public class FavController {

    private final FavService service;

    @PostMapping("/{productId}")
//    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> saveFavProduct(@PathVariable Long productId,
                                                      @RequestHeader("Authorization") String token) {
        try {
            service.favProduct(productId, token);
            return ResponseEntity.ok(new ApiResponse(true, "Product added to favourites successfully!", null));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, "Unauthorized: " + ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to add product to favourites: " + ex.getMessage(), null));
        }
    }

    @DeleteMapping("/{productId}")
//    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> unFavProduct(@PathVariable Long productId,
                                                    @RequestHeader("Authorization") String token) {
        try {
            service.UnFavProduct(productId, token);
            return ResponseEntity.ok(new ApiResponse(true, "Product removed from favourites!", null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, "Unauthorized: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to remove product from favourites: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{productId}")
//    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> getUsersByFavProduct(@PathVariable Long productId) {
        try {
            List<User> users = service.getUsersByFavProduct(productId);
            return ResponseEntity.ok(new ApiResponse(true, "Users who favorited this product fetched successfully!", users));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to fetch users: " + ex.getMessage(), null));
        }
    }
    @GetMapping("")
    public ResponseEntity<ApiResponse> getAllFavProducts(@RequestHeader("Authorization") String token) {
        try {
            List<Product> favProducts = service.getAllFavProducts(token);
            return ResponseEntity.ok(new ApiResponse(true, "Favourite products fetched successfully", favProducts));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to fetch favourite products: " + e.getMessage(), null));
        }
    }
}

