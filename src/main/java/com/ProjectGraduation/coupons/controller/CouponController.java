package com.ProjectGraduation.coupons.controller;

import com.ProjectGraduation.coupons.dto.CouponRequest;
import com.ProjectGraduation.coupons.entity.Coupon;
import com.ProjectGraduation.coupons.service.CouponService;
import com.ProjectGraduation.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;

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

    @PostMapping("/apply")
    public double applyCoupon(@RequestParam Product product, @RequestParam int quantity, @RequestParam String code) {
        return couponService.applyCouponToProduct(product, quantity, code);
    }
    @DeleteMapping("/{id}")
    public void deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
    }
}
