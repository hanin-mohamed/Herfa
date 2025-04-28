package com.ProjectGraduation.profile.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.profile.dto.UpdateProfileRequestDTO;
import com.ProjectGraduation.profile.entity.Profile;
import com.ProjectGraduation.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepo;

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
}
