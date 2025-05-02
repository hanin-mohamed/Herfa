package com.ProjectGraduation.offers.coupons.controller;

import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.offers.coupons.dto.CouponRequest;
import com.ProjectGraduation.offers.coupons.entity.Coupon;
import com.ProjectGraduation.offers.coupons.service.CouponService;
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

        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User creator = userService.getUserByUsername(username);

        Coupon coupon = couponService.createCoupon(request, creator);
        return ResponseEntity.ok(new ApiResponse(true, "Coupon created successfully", coupon));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> updateCoupon(@PathVariable Long id,
                                                    @RequestBody CouponRequest request,
                                                    @RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User currentUser = userService.getUserByUsername(username);

        Coupon updatedCoupon = couponService.updateCoupon(id, request, currentUser);

        return ResponseEntity.ok(new ApiResponse(true, "Coupon updated successfully", updatedCoupon));
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse> toggleCouponStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        Coupon toggled = couponService.toggleCouponStatus(id, isActive);
        return ResponseEntity.ok(new ApiResponse(true, "Coupon status updated", toggled));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCouponById(@PathVariable Long id) {
        Coupon coupon = couponService.getCouponById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Coupon fetched successfully", coupon));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCoupons() {
        List<Coupon> all = couponService.getAllCoupons();
        return ResponseEntity.ok(new ApiResponse(true, "All coupons fetched successfully", all));
    }

    @GetMapping("/apply")
    public ResponseEntity<ApiResponse> applyCoupon(@RequestParam Long productId,
                                                   @RequestParam int quantity,
                                                   @RequestParam String code) {
        Product product = productService.getById(productId);
        double discount = couponService.applyCouponToProduct(product, quantity, code);
        return ResponseEntity.ok(new ApiResponse(true, "Coupon applied successfully", discount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(new ApiResponse(true, "Coupon deleted successfully", null));
    }
}
