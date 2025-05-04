package com.ProjectGraduation.bundle.dto;

import lombok.Data;

import java.util.List;

@Data
public class BundleRequest {
    private String name;
    private String description;
    private double bundlePrice;
    private List<BundleProductRequest> products;
}
