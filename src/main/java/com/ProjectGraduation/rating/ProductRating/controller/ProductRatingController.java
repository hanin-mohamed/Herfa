package com.ProjectGraduation.rating.ProductRating.controller;

import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.rating.ProductRating.entity.ProductRating;
import com.ProjectGraduation.rating.ProductRating.service.ProductRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class ProductRatingController {

    private final ProductRatingService service;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> rateProduct(
            @RequestHeader("Authorization") String token,
            @RequestParam Long productId,
            @RequestParam int stars
    ) {
        try {
            ProductRating productRating = service.addOrUpdateRating(token, productId, stars);
            return ResponseEntity.ok(new ApiResponse(true, "Rating submitted successfully", productRating));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, ex.getMessage(), null));
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> getProductAverageRating(@RequestParam Long productId) {
        try {
            double averageRating = service.getAverageRating(productId);
            return ResponseEntity.ok(new ApiResponse(true, "Average rating fetched successfully", averageRating));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, ex.getMessage(), null));
        }
    }
}
