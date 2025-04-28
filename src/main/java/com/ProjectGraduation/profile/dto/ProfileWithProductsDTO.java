package com.ProjectGraduation.profile.dto;

import com.ProjectGraduation.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileWithProductsDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String bio;
    private String phone;
    private String address;
    private String profilePictureUrl;
    private Double averageRating;
    private Integer numberOfRatings;
    private List<Product> products;
}
