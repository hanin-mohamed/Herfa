package com.ProjectGraduation.offers.deal.dto;

import com.ProjectGraduation.offers.deal.utils.DealStatus;
import com.ProjectGraduation.product.dto.ProductDTO;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class DealResponse {
    private Long id;
    private String buyerUsername;
    private ProductDTO product;
    private int requestedQuantity;
    private double proposedPrice;
    private Double counterPrice;
    private DealStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
