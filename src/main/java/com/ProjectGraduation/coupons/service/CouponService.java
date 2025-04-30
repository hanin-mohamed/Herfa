package com.ProjectGraduation.coupons.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.coupons.dto.CouponRequest;
import com.ProjectGraduation.coupons.entity.Coupon;
import com.ProjectGraduation.coupons.repository.CouponRepository;
import com.ProjectGraduation.coupons.utils.DiscountType;
import com.ProjectGraduation.coupons.utils.UserSegment;
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

        if (coupon.getDiscountType() == DiscountType.FIXED_PRICE && coupon.getFixedPrice() != null) {
            discountAmount = (product.getPrice() - coupon.getFixedPrice()) * quantity;
            if (discountAmount < 0) discountAmount = 0;
        } else if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = totalProductPrice * (coupon.getDiscount() / 100);
            if (coupon.getMaxDiscount() != null && discountAmount > coupon.getMaxDiscount()) {
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
        validateCouponRequest(request);
        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscount(request.getDiscount());
        coupon.setMaxDiscount(request.getMaxDiscount());
        coupon.setFixedPrice(request.getFixedPrice());
        coupon.setAvailableQuantity(request.getAvailableQuantity());
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setActive(request.getIsActive() != null ? request.getIsActive() : true);
        coupon.setProduct(request.getProduct());
        coupon.setCategory(request.getCategory());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setUserSegment(request.getUserSegment());
        coupon.setCreatedBy(request.getCreatedBy());
        return couponRepository.save(coupon);
    }

    public Coupon updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (request.getCode() != null) coupon.setCode(request.getCode());
        if (request.getDiscountType() != null) coupon.setDiscountType(request.getDiscountType());
        if (request.getDiscount() != null) coupon.setDiscount(request.getDiscount());
        if (request.getMaxDiscount() != null) coupon.setMaxDiscount(request.getMaxDiscount());
        if (request.getFixedPrice() != null) coupon.setFixedPrice(request.getFixedPrice());
        if (request.getAvailableQuantity() != null) coupon.setAvailableQuantity(request.getAvailableQuantity());
        if (request.getExpiryDate() != null) coupon.setExpiryDate(request.getExpiryDate());
        if (request.getIsActive() != null) coupon.setActive(request.getIsActive());
        if (request.getProduct() != null) coupon.setProduct(request.getProduct());
        if (request.getCategory() != null) coupon.setCategory(request.getCategory());
        if (request.getMinOrderAmount() != null) coupon.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getUserSegment() != null) coupon.setUserSegment(request.getUserSegment());
        return couponRepository.save(coupon);
    }

    public void validateCoupon(Coupon coupon, double itemTotal, User user) {
        if (!coupon.isActive()) throw new RuntimeException("Coupon is not active");
        if (coupon.getExpiryDate() != null && LocalDateTime.now().isAfter(coupon.getExpiryDate()))
            throw new RuntimeException("Coupon has expired");
        if (coupon.getAvailableQuantity() != null && coupon.getAvailableQuantity() <= 0)
            throw new RuntimeException("Coupon usage limit exceeded");
        if (coupon.getMinOrderAmount() != null && itemTotal < coupon.getMinOrderAmount())
            throw new RuntimeException("Order total doesn't meet coupon minimum requirement");
        if (coupon.getUserSegment() != null && !isUserEligible(coupon.getUserSegment()))
            throw new RuntimeException("User not eligible for this coupon");
    }

    public double calculateDiscount(Coupon coupon, Product product, int quantity) {
        double totalProductPrice = product.getPrice() * quantity;
        double discountAmount = 0.0;

        if (coupon.getDiscountType() == DiscountType.FIXED_PRICE && coupon.getFixedPrice() != null) {
            discountAmount = (product.getPrice() - coupon.getFixedPrice()) * quantity;
            if (discountAmount < 0) discountAmount = 0;
        } else if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = totalProductPrice * (coupon.getDiscount() / 100);
            if (coupon.getMaxDiscount() != null && discountAmount > coupon.getMaxDiscount()) {
                discountAmount = coupon.getMaxDiscount();
            }
        } else if (coupon.getDiscountType() == DiscountType.AMOUNT) {
            discountAmount = coupon.getDiscount();
        }

        return discountAmount;
    }

    private boolean isUserEligible(UserSegment segment) {
        return segment == UserSegment.ALL;
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

    private void validateCouponRequest(CouponRequest request) {
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new RuntimeException("Coupon code is required");
        }

        if (request.getDiscountType() == null) {
            throw new RuntimeException("Discount type is required");
        }

        switch (request.getDiscountType()) {
            case FIXED_PRICE:
                if (request.getFixedPrice() == null) {
                    throw new RuntimeException("Fixed price is required for FIXED_PRICE discount type");
                }
                break;
            case PERCENTAGE:
                if (request.getDiscount() == null) {
                    throw new RuntimeException("Discount percentage is required for PERCENTAGE discount type");
                }
                break;
            case AMOUNT:
                if (request.getDiscount() == null) {
                    throw new RuntimeException("Discount amount is required for AMOUNT discount type");
                }
                break;
            default:
                throw new RuntimeException("Unsupported discount type");
        }

        if (request.getProduct() == null && request.getCategory() == null) {
            throw new RuntimeException("Coupon must be linked to either a product or a category");
        }

        if (request.getCreatedBy() == null) {
            throw new RuntimeException("Coupon must have a creator");
        }
    }
}
