package com.ProjectGraduation.auth.service;

import com.ProjectGraduation.auth.api.model.LoginBody;
import com.ProjectGraduation.auth.api.model.RegistrationBody;
import com.ProjectGraduation.auth.entity.Merchant;
import com.ProjectGraduation.auth.entity.Role;
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


    public Merchant saveMerchant(RegistrationBody body)throws UserAlreadyExistsException{
        if (merchantRepo.findByEmailIgnoreCase(body.getEmail()).isPresent()
                || merchantRepo.findByUsernameIgnoreCase(body.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        Merchant merchant = new Merchant();
        merchant.setFirstName(body.getFirstName());
        merchant.setLastName(body.getLastName());
        merchant.setUsername(body.getUsername());
        merchant.setEmail(body.getEmail());
        merchant.setPassword(encryptionService.encryptPassword(body.getPassword()));
        merchant.setRole(Role.MERCHANT);

        return merchantRepo.save(merchant);

    }

    public Merchant registerMerchant(RegistrationBody registrationBody) throws UserAlreadyExistsException {


        Merchant merchant = saveMerchant(registrationBody);

        try {
            authService.generateAndSendOtp(merchant.getEmail());

        }catch (Exception e){
            merchantRepo.delete(merchant);
            throw new RuntimeException("Failed to send OTP. Merchant registration rolled back.", e);
        }


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
