package com.ProjectGraduation.coupons.service;

import com.ProjectGraduation.coupons.dto.CouponRequest;
import com.ProjectGraduation.coupons.entity.Coupon;
import com.ProjectGraduation.coupons.repository.CouponRepository;
import com.ProjectGraduation.coupons.utils.DiscountType;
import com.ProjectGraduation.product.entity.Product;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public double applyCouponToProduct(Product product, int quantity, String couponCode) {
        if (couponCode == null || couponCode.isEmpty()) {
            return 0.0;
        }

        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        validateCoupon(coupon);

        if (coupon.getProduct() != null && !coupon.getProduct().getId().equals(product.getId())) {
            throw new RuntimeException("Coupon is not valid for this product");
        }

        double totalProductPrice = product.getPrice() * quantity;
        double discountAmount = 0.0;

        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = totalProductPrice * (coupon.getDiscount() / 100);

            if (coupon.getMaxDiscount() > 0 && discountAmount > coupon.getMaxDiscount()) {
                discountAmount = coupon.getMaxDiscount();
            }
        } else if (coupon.getDiscountType() == DiscountType.AMOUNT) {
            discountAmount = coupon.getDiscount();
        }
        return discountAmount;
    }

    @Transactional
    public void confirmCouponUsage(String couponCode) {
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        validateCoupon(coupon);

        if (coupon.getAvailableQuantity() != null && coupon.getAvailableQuantity() > 0) {
            coupon.setAvailableQuantity(coupon.getAvailableQuantity() - 1);
            couponRepository.save(coupon);
        } else {
            throw new RuntimeException("Coupon usage limit exceeded");
        }
    }

    public Coupon createCoupon(CouponRequest request) {
        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscount(request.getDiscount());
        coupon.setMaxDiscount(request.getMaxDiscount());
        coupon.setAvailableQuantity(request.getAvailableQuantity());
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setActive(true);
        coupon.setProduct(request.getProduct());
        coupon.setCreatedBy(request.getCreatedBy());
        return couponRepository.save(coupon);
    }

    public Coupon updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (request.getCode() != null) {
            coupon.setCode(request.getCode());
        }
        if (request.getDiscountType() != null) {
            coupon.setDiscountType(request.getDiscountType());
        }
        if (request.getDiscount() != null) {
            coupon.setDiscount(request.getDiscount());
        }
        if (request.getMaxDiscount() != null) {
            coupon.setMaxDiscount(request.getMaxDiscount());
        }
        if (request.getAvailableQuantity() != null) {
            coupon.setAvailableQuantity(request.getAvailableQuantity());
        }
        if (request.getExpiryDate() != null) {
            coupon.setExpiryDate(request.getExpiryDate());
        }
        if (request.getIsActive() != null) {
            coupon.setActive(request.getIsActive());
        }

        return couponRepository.save(coupon);
    }

    public Coupon toggleCouponStatus(Long id, boolean isActive) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        coupon.setActive(isActive);
        return couponRepository.save(coupon);
    }

    public Coupon getCouponById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
    }
    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
    }
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new RuntimeException("Coupon not found");
        }
        couponRepository.deleteById(id);
    }

    private void validateCoupon(Coupon coupon) {
        if (!coupon.isActive()) {
            throw new RuntimeException("Coupon is not active");
        }

        if (coupon.getExpiryDate() != null && LocalDateTime.now().isAfter(coupon.getExpiryDate())) {
            throw new RuntimeException("Coupon has expired");
        }

        if (coupon.getAvailableQuantity() != null && coupon.getAvailableQuantity() <= 0) {
            throw new RuntimeException("Coupon usage limit exceeded");
        }
    }


}
