package com.ProjectGraduation.order.helper;

import com.ProjectGraduation.offers.productoffers.entity.ProductOffer;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductOfferUsageRecord {
    public final ProductOffer offer;
    public final double discount;

}
