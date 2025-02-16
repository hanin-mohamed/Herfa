package com.ProjectGraduation.auth.api.controllerAuth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/demo1")
    @PreAuthorize("hasAuthority('ROLE_USER')") // Access allowed for users with USER role
    public String test1() {
        return "demo1";
    }

    @GetMapping("/demo2")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')") // Access allowed for users with MERCHANT role
    public String test2() {
        return "demo2";
    }
}
