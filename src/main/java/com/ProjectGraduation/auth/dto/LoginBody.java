package com.ProjectGraduation.auth.dto;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginBody {
    private String username;
    private String password ;
    private String fcmToken;


}
