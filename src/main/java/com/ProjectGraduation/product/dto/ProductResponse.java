package com.ProjectGraduation.product.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String shortDescription;
    private String longDescription;
    private double price;
    private int quantity;
    private String media;
    private String userUsername;
    private String userFirstName;
    private String userLastName;
    private Boolean active;
    private Long categoryId; // Changed from Category to categoryId
    private String categoryName; // Added category name
    private List<String> colors = new ArrayList<>();
    private double discountedPrice;
}