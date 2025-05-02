package com.ProjectGraduation.order.dto;

import com.ProjectGraduation.product.dto.ProductDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderItemDTO {
    private Long id;
    private ProductDTO product;
    private String couponCode;
    private int quantity;
    private double unitPrice;
}
