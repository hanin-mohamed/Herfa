package com.ProjectGraduation.order.helper;

import com.ProjectGraduation.offers.coupon.entity.Coupon;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CouponUsageRecord {
    public final Coupon coupon;
    public final String couponCode;
    public final double discount;

}
