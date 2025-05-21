package com.ProjectGraduation.event.helper;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.event.dto.EventDto;
import com.ProjectGraduation.event.entity.Event;
import com.ProjectGraduation.file.CloudinaryService;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.exception.FileUploadException;
import com.ProjectGraduation.product.exception.UnauthorizedMerchantException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@Service
@RequiredArgsConstructor
public class EventHelper {
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final CloudinaryService cloudinaryService;

    public User validateMerchant(String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        System.out.println("Validating merchant with username: " + username); // Debug

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("Merchant not found with username: " + username));

         if (!userRepository.existsById(user.getId())) {
            throw new UserNotFoundException("Merchant ID " + user.getId() + " not found in merchant table");
        }

        if (!user.getRole().toString().equals("MERCHANT")) {
            throw new UnauthorizedMerchantException("You are not a merchant.");
        }

        System.out.println("Validated merchant with ID: " + user.getId()); // Debug
        return user;
    }

    public String uploadEventMedia(MultipartFile file, Long userId) throws FileUploadException, IOException {
        try {
            return cloudinaryService.uploadImage(file, "event", userId);
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload media file: " + e.getMessage());
        }
    }

    public Event buildEvent(EventDto eventDto, String media, User user) {
        Event event = new Event();
        event.setName(eventDto.getName());
        event.setDescription(eventDto.getDescription());
        event.setMedia(media);
        event.setStartTime(eventDto.getStartTime());
        event.setEndTime(eventDto.getEndTime());
        event.setPrice(eventDto.getPrice());
        event.setUser(user);
        return event;
    }

    public User getUserFromToken(String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public void validateEventOwnership(Event event, User merchant) {
        if (!event.getUser().getId().equals(merchant.getId())) {
            throw new UnauthorizedMerchantException("You are not the owner of this event");
        }
    }

    public void validateProductOwnership(Product product, User merchant) {
        if (!product.getUser().getId().equals(merchant.getId())) {
            throw new UnauthorizedMerchantException("You are not the owner of this product");
        }
    }
}