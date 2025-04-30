package com.ProjectGraduation.coupons.controller;

import com.ProjectGraduation.coupons.dto.CouponRequest;
import com.ProjectGraduation.coupons.entity.Coupon;
import com.ProjectGraduation.coupons.service.CouponService;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;
    private final ProductService productService;
    @PostMapping
    public Coupon createCoupon(@RequestBody CouponRequest request) {
        return couponService.createCoupon(request);
    }

    @PutMapping("/{id}")
    public Coupon updateCoupon(@PathVariable Long id, @RequestBody CouponRequest request) {
        return couponService.updateCoupon(id, request);
    }

    @PatchMapping("/{id}/status")
    public Coupon toggleCouponStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        return couponService.toggleCouponStatus(id, isActive);
    }
    @GetMapping("/{id}")
    public Coupon getCouponById(@PathVariable Long id) {
        return couponService.getCouponById(id);
    }

    @GetMapping
    public List<Coupon> getAllCoupons() {
        return couponService.getAllCoupons();
    }

    public double applyCoupon(@RequestParam Long productId, @RequestParam int quantity, @RequestParam String code) {
        Product product = productService.getById(productId);
        return couponService.applyCouponToProduct(product, quantity, code);
    }

    @DeleteMapping("/{id}")
    public void deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
    }
}
