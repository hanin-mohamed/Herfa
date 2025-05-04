package com.ProjectGraduation.auth.service;

import com.ProjectGraduation.auth.dto.LoginBody;
import com.ProjectGraduation.auth.dto.RegistrationBody;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.auth.exception.InvalidCredentialsException;
import com.ProjectGraduation.auth.exception.UserAlreadyExistsException;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import com.ProjectGraduation.auth.exception.UserNotVerifiedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final JWTService jwtService;
    private final AuthService authService;

    public User registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException {
        if (userRepository.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists: " + registrationBody.getEmail());
        }
        if (userRepository.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + registrationBody.getUsername());
        }

        User user = new User();
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setUsername(registrationBody.getUsername());
        user.setEmail(registrationBody.getEmail());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        user.setRole(registrationBody.getRole());

        userRepository.save(user);
        authService.generateAndSendOtp(user.getEmail());

        return user;
    }

    public String loginUser(LoginBody loginUserBody) {

        User user = userRepository.findByUsernameIgnoreCase(loginUserBody.getUsername())
                .orElseGet(() -> userRepository.findByEmailIgnoreCase(loginUserBody.getUsername())
                        .orElseThrow(() -> new InvalidCredentialsException("Invalid username or email")));

        if (!encryptionService.verifyPassword(loginUserBody.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        if (!user.isVerified()) {
            throw new UserNotVerifiedException("User email is not verified");
        }

        return jwtService.generateJWTForUser(user);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }
}