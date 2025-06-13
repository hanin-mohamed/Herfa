package com.ProjectGraduation.offers.deal.dto;


import lombok.Data;

@Data
public class CounterOfferRequest {
    private Long dealId;
    private double counterPrice;
    private int counterQuantity;
}
