package com.ProjectGraduation.auth.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
}