package com.ProjectGraduation.coupons.dto;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.coupons.utils.DiscountType;
import com.ProjectGraduation.coupons.utils.UserSegment;
import com.ProjectGraduation.product.entity.Category;
import com.ProjectGraduation.product.entity.Product;
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
    private Double minOrderAmount;
    private UserSegment userSegment;
    private Product product;
    private Category category;
    private User createdBy;
}
