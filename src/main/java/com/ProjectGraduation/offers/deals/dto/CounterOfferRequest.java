package com.ProjectGraduation.offers.deals.dto;


import lombok.Data;

@Data
public class CounterOfferRequest {
    private Long dealId;
    private double counterPrice;
}
