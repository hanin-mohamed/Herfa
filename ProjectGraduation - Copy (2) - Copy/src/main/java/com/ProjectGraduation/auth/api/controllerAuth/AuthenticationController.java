package com.ProjectGraduation.auth.api.controllerAuth;

import com.ProjectGraduation.auth.api.model.LoginBody;
import com.ProjectGraduation.auth.api.model.LoginResponse;
import com.ProjectGraduation.auth.api.model.LoginUserBody;
import com.ProjectGraduation.auth.api.model.RegistrationBody;
import com.ProjectGraduation.auth.entity.Merchant;
import com.ProjectGraduation.auth.service.MerchantService;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.exception.UserAlreadyExistsException;
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
    private MerchantService merchantService ;
    @Autowired
    private UserService userService ;

    @PostMapping("/register/merchant")
    public ResponseEntity registerMerchant (@Valid @RequestBody RegistrationBody registrationBody){
        try {
            merchantService.registerMerchant(registrationBody);
            return ResponseEntity.ok(registrationBody);
        } catch (UserAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @PostMapping("/login/merchant")
    public ResponseEntity<LoginResponse> loginmerchant(@Valid @RequestBody LoginBody loginBody) {
        String jwt = merchantService.loginMerchant(loginBody);
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            LoginResponse response = new LoginResponse();
            response.setJwt(jwt);
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("me/merchant")
    public Merchant getMerchantProfile(@AuthenticationPrincipal Merchant merchant){

        return merchant;
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
