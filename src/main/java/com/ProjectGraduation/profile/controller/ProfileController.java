package com.ProjectGraduation.profile.controller;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.profile.dto.ProfileWithProductsDTO;
import com.ProjectGraduation.profile.dto.UpdateProfileRequestDTO;
import com.ProjectGraduation.profile.entity.Profile;
import com.ProjectGraduation.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final JWTService jwtService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Profile> getMyProfile(@RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token);
        User user = userService.getUserByUsername(username);
        Profile profile = profileService.getProfile(user);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping
    public ResponseEntity<Profile> updateMyProfile(@RequestHeader("Authorization") String token,
                                                   @RequestBody UpdateProfileRequestDTO updateRequest) {
        String username = jwtService.getUsername(token);
        User user = userService.getUserByUsername(username);
        Profile updatedProfile = profileService.updateProfile(user, updateRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/upload-picture")
    public ResponseEntity<String> uploadProfilePicture(@RequestHeader("Authorization") String token,
                                                       @RequestParam("file") MultipartFile file) {
        String username = jwtService.getUsername(token);
        User user = userService.getUserByUsername(username);
        profileService.updateProfilePic(user, file);
        return ResponseEntity.ok("Profile picture updated successfully");
    }
    @GetMapping("/{userId}")
    public ResponseEntity<Profile> viewProfileById(@PathVariable Long userId) {
        Profile profile = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileWithProductsDTO> viewMerchantProfileById(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfileWithProducts(userId));
    }


}
