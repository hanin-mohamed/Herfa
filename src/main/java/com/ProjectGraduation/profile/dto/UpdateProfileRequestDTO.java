package com.ProjectGraduation.profile.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequestDTO {
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{11}$", message = "Phone number must be 11 digits")
    private String phone;

    @NotBlank(message = "Address is required")
    @Size(min = 3, max = 50, message = "Address must be between 3 and 50 characters")
    private String address;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    @NotBlank(message = "bio is required")
    private String bio;
}
