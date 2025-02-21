package com.ProjectGraduation.auth.service;

import com.ProjectGraduation.auth.api.model.LoginUserBody;
import com.ProjectGraduation.auth.api.model.RegistrationBody;
import com.ProjectGraduation.auth.entity.Role;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.auth.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo ;
    @Autowired
    private EncryptionService encryptionService ;
    @Autowired
    private JWTService jwtService ;


    public User registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException {
        if (userRepo.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
        || userRepo.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()
        ){
            throw new UserAlreadyExistsException();
        }
        User user = new User() ;
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setUsername(registrationBody.getUsername());
        user.setEmail(registrationBody.getEmail());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        user.setRole(Role.USER);
        return userRepo.save(user) ;
    }

    public String loginUser(LoginUserBody loginUserBody) {

        Optional <User> opUser = userRepo.findByUsernameIgnoreCase(loginUserBody.getUsername()) ;

        if (opUser.isPresent()){
            User user = opUser.get();
            if (encryptionService.verifyPassword(loginUserBody.getPassword() , user.getPassword())){
                return jwtService.generateJWTForUser(user) ;
            }
        }
        return null ;

    }


}
