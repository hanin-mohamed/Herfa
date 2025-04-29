package com.ProjectGraduation.coupons.dto;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.coupons.utils.DiscountType;
import com.ProjectGraduation.product.entity.Product;
import lombok.*;

import java.time.LocalDateTime;


@Setter
@Getter
public class CouponRequest {
    private String code;
    private DiscountType discountType;
    private Double discount;
    private LocalDateTime expiryDate;
    private Boolean isActive;
    private Product product;
    private Integer availableQuantity;
    private Double maxDiscount;
    private User createdBy;
}
