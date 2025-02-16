package com.ProjectGraduation.auth.service;

import com.ProjectGraduation.auth.api.model.LoginBody;
import com.ProjectGraduation.auth.api.model.RegistrationBody;
import com.ProjectGraduation.auth.entity.Merchant;
import com.ProjectGraduation.auth.entity.repo.MerchantRepo;
import com.ProjectGraduation.auth.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MerchantService {

    @Autowired
    private MerchantRepo merchantRepo;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JWTService jwtService;

    public Merchant registerMerchant(RegistrationBody registrationBody) throws UserAlreadyExistsException {

        if (merchantRepo.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
                || merchantRepo.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        Merchant merchant = new Merchant();
        merchant.setFirstName(registrationBody.getFirstName());
        merchant.setLastName(registrationBody.getLastName());
        merchant.setUsername(registrationBody.getUsername());
        merchant.setEmail(registrationBody.getEmail());
        merchant.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        merchant.setRole("MERCHANT");

        merchantRepo.save(merchant);

        // Send OTP after registration
        authService.generateAndSendOtp(merchant.getEmail());

        return merchant;
    }

    public String loginMerchant(LoginBody loginBody) {
        Optional<Merchant> opMerchant = merchantRepo.findByUsernameIgnoreCase(loginBody.getUsername());

        if (opMerchant.isPresent()) {
            Merchant merchant = opMerchant.get();
            if (encryptionService.verifyPassword(loginBody.getPassword(), merchant.getPassword())) {
                return jwtService.generateJWT(merchant);
            }
        }
        return null;
    }

    public Merchant getMerchantByUsername(String username) {
        return merchantRepo.findByUsernameIgnoreCase(username).orElse(null);
    }

}
