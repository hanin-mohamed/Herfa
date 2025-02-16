package com.ProjectGraduation.auth.api.controllerAuth;

import com.ProjectGraduation.auth.api.model.*;
import com.ProjectGraduation.auth.entity.Merchant;
import com.ProjectGraduation.auth.service.MerchantService;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.exception.UserAlreadyExistsException;
import com.ProjectGraduation.auth.service.AuthService;
import com.ProjectGraduation.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService ;
    @Autowired
    private MerchantService merchantService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegistrationBody registrationBody) {
        try {
            merchantService.registerMerchant(registrationBody);
            return ResponseEntity.ok("Registration successful! Please check your email for the OTP.");
        } catch (Exception e) {
            e.printStackTrace();  // Add this to check the real issue
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed.");
        }
    }

    @PostMapping("/verify/otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        if (authService.verifyOtp(email, otp)) {
            return ResponseEntity.ok("OTP verified successfully! Your email is now verified.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP.");
        }
    }

    @PostMapping("/login/merchant")
    public ResponseEntity<String> loginMerchant( @RequestBody LoginBody loginBody) {
        String jwt = merchantService.loginMerchant(loginBody);
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials.");
        }

        Merchant merchant = merchantService.getMerchantByUsername(loginBody.getUsername());
        if (!merchant.isVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Please verify your email first.");
        }

        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            authService.sendPasswordResetOtp(email);
            return ResponseEntity.ok("OTP has been sent to your email.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/verify/reset-otp")
    public ResponseEntity<String> verifyResetOtp(@RequestParam String email, @RequestParam String otp) {
        if (authService.verifyResetOtp(email, otp)) {
            return ResponseEntity.ok("OTP is valid.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP.");
        }
    }
    @PostMapping("/reset/password")

    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (authService.updatePasswordWithOtp(request.getEmail(), request.getOtp(), request.getNewPassword())) {
            return ResponseEntity.ok("Password reset successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP or expired.");
        }
    }


    @PostMapping("/resend/otp")
    public ResponseEntity<String> resendOtp(@RequestParam String email) {
        try {
            authService.regenerateOtp(email);
            return ResponseEntity.ok("A new OTP has been sent to your email.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate a new OTP.");
        }
    }


    //////////////////USER///////////
    @PostMapping("/register/user")
    public ResponseEntity registerUser (@Valid @RequestBody RegistrationBody registrationBody){
        try {
            userService.registerUser(registrationBody);
            return ResponseEntity.ok(registrationBody);
        } catch (UserAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @PostMapping("/login/user")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginUserBody loginUserBody) {
        String jwt = userService.loginUser(loginUserBody);
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            LoginResponse response = new LoginResponse();
            response.setJwt(jwt);
            return ResponseEntity.ok(response);
        }
    }
    @GetMapping("me/user")
    public User getUSerProfile(@AuthenticationPrincipal User user){
        return user;
    }



}
