package com.ProjectGraduation.offers.deal.dto;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class DealRequest {
    private long productId;
    private int requestedQuantity;
    private double proposedPrice;
}
