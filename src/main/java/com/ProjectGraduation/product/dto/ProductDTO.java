package com.ProjectGraduation.product.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Short description is required")
    @Size(min = 10, max = 200, message = "Short description must be between 10 and 200 characters")
    private String shortDescription;

    @NotBlank(message = "Long description is required")
    @Size(min = 20, max = 2000, message = "Long description must be between 20 and 2000 characters")
    private String longDescription;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @DecimalMax(value = "1000000.00", message = "Price cannot exceed 1,000,000.00")
    private Double price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = 100000, message = "Quantity cannot exceed 100,000")
    private Integer quantity;

    @NotNull(message = "Active status is required")
    private Boolean active;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Colors are required")
    @Size(min = 1, message = "At least one color must be specified")
    private List<String> colors;

    @NotEmpty(message = "Image file is required")
    private MultipartFile file;

    private Double discountedPrice;


}