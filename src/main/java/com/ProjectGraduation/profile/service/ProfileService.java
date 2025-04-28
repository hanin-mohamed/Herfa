package com.ProjectGraduation.profile.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.ProductService;
import com.ProjectGraduation.profile.dto.ProfileWithProductsDTO;
import com.ProjectGraduation.profile.dto.UpdateProfileRequestDTO;
import com.ProjectGraduation.profile.entity.Profile;
import com.ProjectGraduation.profile.repository.ProfileRepository;
import com.ProjectGraduation.rating.ProfileRating.service.ProfileRatingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepo;
    private final UserRepo userRepo;
    private final ProductService productService;
    private final ProfileRatingService profileRatingService;
    public Profile getProfile(User user) {
        return profileRepo.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Profile not found for this user"));
    }

    public Profile updateProfile(User user, UpdateProfileRequestDTO updateRequest) {
        Profile profile = profileRepo.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Profile not found for this user"));

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
                .orElseThrow(() -> new IllegalStateException("Profile not found for this user"));
        String fileName = file.getOriginalFilename();
        profile.setProfilePictureUrl(fileName);
        profileRepo.save(profile);
    }
    public Profile getProfileByUserId(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return profileRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found for this user"));
    }
    @Transactional
    public ProfileWithProductsDTO getProfileWithProducts(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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
