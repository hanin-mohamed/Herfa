package com.ProjectGraduation.offers.coupons.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.offers.coupons.dto.CouponRequest;
import com.ProjectGraduation.offers.coupons.entity.Coupon;
import com.ProjectGraduation.offers.coupons.repository.CouponRepository;
import com.ProjectGraduation.offers.coupons.utils.DiscountType;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final ProductService productService;
    @Transactional
    public double applyCouponToProduct(Product product, int quantity, String couponCode) {
        if (couponCode == null || couponCode.isEmpty()) {
            return 0.0;
        }

        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        double unitPriceAfterOffer = product.getDiscountedPrice();
        double totalAfterOffer = unitPriceAfterOffer * quantity;

        validateCoupon(coupon, totalAfterOffer);

        if (coupon.getProduct() != null && !coupon.getProduct().getId().equals(product.getId())) {
            throw new RuntimeException("Coupon is not valid for this product");
        }

        return calculateDiscount(coupon, unitPriceAfterOffer, quantity);
    }

    @Transactional
    public void confirmCouponUsage(String couponCode) {
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        validateCoupon(coupon, null);

        if (coupon.getAvailableQuantity() != null && coupon.getAvailableQuantity() > 0) {
            coupon.setAvailableQuantity(coupon.getAvailableQuantity() - 1);
            couponRepository.save(coupon);
        } else {
            throw new RuntimeException("Coupon usage limit exceeded");
        }
    }

    public Coupon createCoupon(CouponRequest request, User merchant) {
        validateCouponRequest(request);
        Product product = productService.getById(request.getProduct().getId());

        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscount(request.getDiscount());
        coupon.setMaxDiscount(request.getMaxDiscount());
        coupon.setFixedPrice(request.getFixedPrice());
        coupon.setAvailableQuantity(request.getAvailableQuantity());
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setActive(request.getIsActive() != null ? request.getIsActive() : true);
        coupon.setProduct(product);
        coupon.setCreatedBy(merchant);

        return couponRepository.save(coupon);
    }

    public Coupon updateCoupon(Long id, CouponRequest request, User currentUser) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (!coupon.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to update this coupon");
        }

        if (request.getCode() != null) coupon.setCode(request.getCode());
        if (request.getDiscountType() != null) coupon.setDiscountType(request.getDiscountType());
        if (request.getDiscount() != null) coupon.setDiscount(request.getDiscount());
        if (request.getMaxDiscount() != null) coupon.setMaxDiscount(request.getMaxDiscount());
        if (request.getFixedPrice() != null) coupon.setFixedPrice(request.getFixedPrice());
        if (request.getAvailableQuantity() != null) coupon.setAvailableQuantity(request.getAvailableQuantity());
        if (request.getExpiryDate() != null) coupon.setExpiryDate(request.getExpiryDate());
        if (request.getIsActive() != null) coupon.setActive(request.getIsActive());
        if (request.getProduct() != null) coupon.setProduct(request.getProduct());
        return couponRepository.save(coupon);
    }

    public double calculateDiscount(Coupon coupon, double unitPriceAfterOffer, int quantity) {
        double discountAmount = 0.0;

        switch (coupon.getDiscountType()) {
            case DiscountType.FIXED_PRICE -> {
                discountAmount = (unitPriceAfterOffer - coupon.getFixedPrice()) * quantity;
                if (discountAmount < 0) discountAmount = 0;
            }
            case DiscountType.PERCENTAGE -> {
                double singleUnitDiscount = unitPriceAfterOffer * (coupon.getDiscount() / 100);
                if (coupon.getMaxDiscount() != null && singleUnitDiscount > coupon.getMaxDiscount()) {
                    singleUnitDiscount = coupon.getMaxDiscount();
                }
                discountAmount = singleUnitDiscount * quantity;
            }
            case DiscountType.AMOUNT -> {
                discountAmount = coupon.getDiscount() * quantity;
            }
        }

        return discountAmount;
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

    private void validateCoupon(Coupon coupon, Double itemTotal) {
        if (!coupon.isActive()) throw new RuntimeException("Coupon is not active");
        if (coupon.getExpiryDate() != null && LocalDateTime.now().isAfter(coupon.getExpiryDate()))
            throw new RuntimeException("Coupon has expired");
        if (coupon.getAvailableQuantity() != null && coupon.getAvailableQuantity() <= 0)
            throw new RuntimeException("Coupon usage limit exceeded");
        if (itemTotal != null && coupon.getMinOrderAmount() != null && itemTotal < coupon.getMinOrderAmount())
            throw new RuntimeException("Order total doesn't meet coupon minimum requirement");
    }

    private void validateCouponRequest(CouponRequest request) {
        if (request.getCode() == null || request.getCode().isEmpty())
            throw new RuntimeException("Coupon code is required");
        if (request.getDiscountType() == null)
            throw new RuntimeException("Discount type is required");

        switch (request.getDiscountType()) {
            case FIXED_PRICE -> {
                if (request.getFixedPrice() == null)
                    throw new RuntimeException("Fixed price is required");
            }
            case PERCENTAGE -> {
                if (request.getDiscount() == null)
                    throw new RuntimeException("Percentage is required");
                if (request.getMaxDiscount() == null)
                    throw new RuntimeException("Max discount is required");
            }
            case AMOUNT -> {
                if (request.getDiscount() == null)
                    throw new RuntimeException("Discount amount is required");
            }
        }

        if (request.getProduct() == null)
            throw new RuntimeException("Coupon must be linked to a product");
    }
}
