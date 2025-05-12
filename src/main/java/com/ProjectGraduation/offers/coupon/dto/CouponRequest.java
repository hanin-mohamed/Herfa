package com.ProjectGraduation.offers.coupon.dto;

import com.ProjectGraduation.offers.coupon.utils.DiscountType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CouponRequest {
    private String code;
    private DiscountType discountType;
    private Double discount;
    private Double maxDiscount;
    private Double fixedPrice;
    private Integer availableQuantity;
    private LocalDateTime expiryDate;
    private Boolean isActive;
    private Long productId;
}
