package com.ProjectGraduation.bundle.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BundleProductDTO {
    private Long productId;
    private String productName;
    private int quantity;
}
