package com.ProjectGraduation.profile.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.FileService;
import com.ProjectGraduation.product.service.ProductService;
import com.ProjectGraduation.profile.dto.ProfileWithProductsDTO;
import com.ProjectGraduation.profile.dto.UpdateProfileRequestDTO;
import com.ProjectGraduation.profile.entity.Profile;
import com.ProjectGraduation.profile.repository.ProfileRepository;
import com.ProjectGraduation.rating.ProfileRating.service.ProfileRatingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepo;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final ProfileRatingService profileRatingService;
    private final  FileService fileService;

    @Value("${project.poster}")
    private String basePath;

    @Value("${base.url}")
    private String baseUrl;



    public Profile getProfile(User user) {
        return profileRepo.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Profile not found for this user"));
    }

    public Profile updateProfile(User user, UpdateProfileRequestDTO updateRequest) {
        Profile profile = profileRepo.findByUser(user)
                .orElseGet(() -> {
                    Profile newProfile = new Profile();
                    newProfile.setUser(user);
                    return profileRepo.save(newProfile);
                });

        if (updateRequest.getFirstName() != null) {
            profile.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            profile.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getPhone() != null) {
            profile.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getAddress() != null) {
            profile.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getBio() != null) {
            profile.setBio(updateRequest.getBio());
        }

        return profileRepo.save(profile);
    }


    public void updateProfilePic(User user, MultipartFile file) {
        Profile profile = profileRepo.findByUser(user)
                .orElseGet(() -> {
                    Profile newProfile = new Profile();
                    newProfile.setUser(user);
                    return profileRepo.save(newProfile);
                });

        try {
            String storedPath = fileService.uploadFile(basePath, file, user.getId(), "profile", user.getUsername());
            String imageUrl = baseUrl + "profiles/image/" + storedPath.replace(" ", "%20").replace("\\", "/");
            profile.setProfilePictureUrl(imageUrl);
            profileRepo.save(profile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile picture", e);
        }
    }


    public Profile getProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return profileRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found for this user"));
    }



    @Transactional
    public ProfileWithProductsDTO getProfileWithProducts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!"MERCHANT".equals(user.getRole().toString())) {
            throw new IllegalStateException("User is not a merchant");
        }

        Profile profile = profileRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        List<Product> products = productService.getMerchantProducts(user);
        double averageRating = profileRatingService.getAverageRating(userId);
        int numberOfRatings = profileRatingService.getRatingsForUser(userId).size();
        ProfileWithProductsDTO dto = new ProfileWithProductsDTO();
        dto.setUserId(user.getId());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setBio(profile.getBio());
        dto.setPhone(profile.getPhone());
        dto.setAddress(profile.getAddress());
        dto.setProfilePictureUrl(profile.getProfilePictureUrl());
        dto.setProducts(products);
        dto.setAverageRating(averageRating);
        dto.setNumberOfRatings(numberOfRatings);
        return dto;
    }

}
