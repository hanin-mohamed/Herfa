package com.ProjectGraduation.offers.deal.dto;

import com.ProjectGraduation.offers.deal.utils.DealStatus;
import com.ProjectGraduation.product.dto.ProductDTO;
import com.ProjectGraduation.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DealResponse {
    private Long id;
    private String buyerUsername;
    private Product product;
    private int requestedQuantity;
    private double proposedPrice;
    private Double counterPrice;
    private Integer counterQuantity;
    private DealStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
