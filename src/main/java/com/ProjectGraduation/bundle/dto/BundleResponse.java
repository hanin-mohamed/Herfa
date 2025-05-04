package com.ProjectGraduation.bundle.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BundleResponse {
    private Long id;
    private String name;
    private String description;
    private double bundlePrice;
    private boolean active;
    private LocalDateTime createdAt;
    private List<BundleProductDTO> products;
}
