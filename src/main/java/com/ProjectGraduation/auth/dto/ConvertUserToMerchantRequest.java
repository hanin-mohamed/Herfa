package com.ProjectGraduation.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConvertUserToMerchantRequest {
    private String username;
    private String password ;

}
