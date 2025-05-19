package com.ProjectGraduation.offers.coupon.controller;

import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.offers.coupon.dto.CouponRequest;
import com.ProjectGraduation.offers.coupon.entity.Coupon;
import com.ProjectGraduation.offers.coupon.service.CouponService;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;
    private final ProductService productService;
    private final JWTService jwtService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> createCoupon(
            @RequestHeader("Authorization") String token,
            @RequestBody CouponRequest request) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            User creator = userService.getUserByUsername(username);

            Coupon coupon = couponService.createCoupon(request, creator);
            return ResponseEntity.ok(new ApiResponse(true, "Coupon created successfully", coupon));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to create coupon: " + ex.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> updateCoupon(@PathVariable Long id,
                                                    @RequestBody CouponRequest request,
                                                    @RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            User currentUser = userService.getUserByUsername(username);

            Coupon updatedCoupon = couponService.updateCoupon(id, request, currentUser);
            return ResponseEntity.ok(new ApiResponse(true, "Coupon updated successfully", updatedCoupon));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to update coupon: " + ex.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse> toggleCouponStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        try {
            Coupon toggled = couponService.toggleCouponStatus(id, isActive);
            return ResponseEntity.ok(new ApiResponse(true, "Coupon status updated", toggled));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to update coupon status: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCouponById(@PathVariable Long id) {
        try {
            Coupon coupon = couponService.getCouponById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Coupon fetched successfully", coupon));
        } catch (Exception ex) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse(false, "Coupon not found: " + ex.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCoupons() {
        try {
            List<Coupon> all = couponService.getAllCoupons();
            return ResponseEntity.ok(new ApiResponse(true, "All coupons fetched successfully", all));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to fetch coupons: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/apply")
    public ResponseEntity<ApiResponse> applyCoupon(@RequestParam Long productId,
                                                   @RequestParam int quantity,
                                                   @RequestParam String code) {
        try {
            Product product = productService.getById(productId);
            double discount = couponService.applyCouponToProduct(product, quantity, code);
            return ResponseEntity.ok(new ApiResponse(true, "Coupon applied successfully", discount));
        } catch (Exception ex) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse(false, "Failed to apply coupon: " + ex.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCoupon(@PathVariable Long id) {
        try {
            couponService.deleteCoupon(id);
            return ResponseEntity.ok(new ApiResponse(true, "Coupon deleted successfully", null));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to delete coupon: " + ex.getMessage(), null));
        }
    }
}
