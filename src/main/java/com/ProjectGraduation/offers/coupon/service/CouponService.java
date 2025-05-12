package com.ProjectGraduation.offers.coupon.service;

import com.ProjectGraduation.offers.coupon.dto.CouponRequest;
import com.ProjectGraduation.offers.coupon.entity.Coupon;
import com.ProjectGraduation.offers.coupon.entity.CouponUsage;
import com.ProjectGraduation.offers.coupon.repository.CouponRepository;
import com.ProjectGraduation.offers.coupon.repository.CouponUsageRepository;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.order.entity.Order;import com.ProjectGraduation.product.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final ProductRepository productRepository;

    @Transactional
    public double applyCouponToProduct(Product product, int quantity, String couponCode) {
        Optional<Coupon> couponOpt = couponRepository.findByCode(couponCode);
        if (couponOpt.isEmpty()) {
            throw new RuntimeException("Coupon not found");
        }
        Coupon coupon = couponOpt.get();

        double unitPrice = product.getDiscountedPrice() > 0 ? product.getDiscountedPrice() : product.getPrice();
        double totalPrice = unitPrice * quantity;

        validate(coupon, totalPrice);

        if (coupon.getProduct() != null && !coupon.getProduct().getId().equals(product.getId())) {
            throw new RuntimeException("Coupon not applicable to this product");
        }

        double discountAmount = calculate(coupon, unitPrice, quantity);

        double newDiscountedPrice = unitPrice - (discountAmount / quantity);
        product.setDiscountedPrice(newDiscountedPrice);

        return discountAmount;
    }

    private double calculate(Coupon coupon, double unitPrice, int quantity) {
        switch (coupon.getDiscountType()) {
            case FIXED_PRICE:
                double diff = unitPrice - coupon.getFixedPrice();
                return Math.max(diff * quantity, 0);
            case PERCENTAGE:
                double single = unitPrice * (coupon.getDiscount() / 100);
                if (coupon.getMaxDiscount() != null && single > coupon.getMaxDiscount()) {
                    single = coupon.getMaxDiscount();
                }
                return single * quantity;
            case AMOUNT:
                return coupon.getDiscount() * quantity;
            default:
                return 0;
        }
    }

    private double calculateDiscountedPrice(Coupon coupon, double unitPrice) {
        switch (coupon.getDiscountType()) {
            case FIXED_PRICE:
                return coupon.getFixedPrice() != null ? coupon.getFixedPrice() : unitPrice;
            case PERCENTAGE:
                double discount = unitPrice * (coupon.getDiscount() / 100);
                if (coupon.getMaxDiscount() != null && discount > coupon.getMaxDiscount()) {
                    discount = coupon.getMaxDiscount();
                }
                return unitPrice - discount;
            case AMOUNT:
                return unitPrice - coupon.getDiscount();
            default:
                return unitPrice;
        }
    }

    private void validate(Coupon coupon, Double total) {
        if (!coupon.isActive()) {
            throw new RuntimeException("Coupon is inactive");
        }
        if (coupon.getExpiryDate() != null && LocalDateTime.now().isAfter(coupon.getExpiryDate())) {
            throw new RuntimeException("Coupon has expired");
        }
        if (coupon.getAvailableQuantity() != null && coupon.getAvailableQuantity() <= 0) {
            throw new RuntimeException("Coupon usage limit exceeded");
        }
        if (total != null && coupon.getMinOrderAmount() != null && total < coupon.getMinOrderAmount()) {
            throw new RuntimeException("Product total below minimum required");
        }
    }

    @Transactional
    public void confirmCouponUsage(String couponCode) {
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        if (coupon.getAvailableQuantity() != null && coupon.getAvailableQuantity() > 0) {
            coupon.setAvailableQuantity(coupon.getAvailableQuantity() - 1);
            if (coupon.getAvailableQuantity() == 0) {
                coupon.setActive(false);
            }
            couponRepository.save(coupon);
        } else {
            throw new RuntimeException("Coupon usage limit exceeded");
        }
    }

    @Transactional
    public void recordCouponUsage(Coupon coupon, User user, Order order, double discount) {
        CouponUsage usage = CouponUsage.builder()
                .coupon(coupon)
                .user(user)
                .order(order)
                .discountApplied(discount)
                .usageDate(LocalDateTime.now())
                .build();
        couponUsageRepository.save(usage);
    }

    @Transactional
    public Optional<Coupon> getCouponByCode(String code) {
        return couponRepository.findByCode(code);
    }

    @Transactional
    public Coupon createCoupon(CouponRequest request, User creator) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .discountType(request.getDiscountType())
                .discount(request.getDiscount() != null ? request.getDiscount() : 0)
                .maxDiscount(request.getMaxDiscount())
                .fixedPrice(request.getFixedPrice())
                .availableQuantity(request.getAvailableQuantity())
                .expiryDate(request.getExpiryDate())
                .active(request.getIsActive() != null ? request.getIsActive() : true)
                .product(product)
                .createdBy(creator)
                .build();

        double discountedPrice = calculateDiscountedPrice(coupon, product.getPrice());
        product.setDiscountedPrice(discountedPrice);

        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon updateCoupon(Long id, CouponRequest request, User creator) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        if (!coupon.getCreatedBy().getId().equals(creator.getId())) {
            throw new RuntimeException("Coupon does not belong to this merchant");
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        coupon.setCode(request.getCode());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscount(request.getDiscount() != null ? request.getDiscount() : coupon.getDiscount());
        coupon.setMaxDiscount(request.getMaxDiscount());
        coupon.setFixedPrice(request.getFixedPrice());
        coupon.setAvailableQuantity(request.getAvailableQuantity());
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setActive(request.getIsActive() != null ? request.getIsActive() : coupon.isActive());
        coupon.setProduct(product);

        double discountedPrice = calculateDiscountedPrice(coupon, product.getPrice());
        product.setDiscountedPrice(discountedPrice);

        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon toggleCouponStatus(Long id, boolean isActive) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        coupon.setActive(isActive);
        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon getCouponById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
    }

    @Transactional
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new RuntimeException("Coupon not found");
        }
        couponRepository.deleteById(id);
    }
}