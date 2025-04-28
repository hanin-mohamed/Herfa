package com.ProjectGraduation.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequestDTO {
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String bio;
}
