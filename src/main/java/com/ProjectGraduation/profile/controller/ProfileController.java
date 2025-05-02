package com.ProjectGraduation.profile.controller;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.profile.dto.ProfileWithProductsDTO;
import com.ProjectGraduation.profile.dto.UpdateProfileRequestDTO;
import com.ProjectGraduation.profile.entity.Profile;
import com.ProjectGraduation.profile.service.ProfileService;
import com.ProjectGraduation.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final JWTService jwtService;
    private final UserService userService;

    @Value("${project.poster}")
    private String basePath;

    @GetMapping("my-profile")
    public ResponseEntity<ApiResponse> getMyProfile(@RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token);
        User user = userService.getUserByUsername(username);
        Profile profile = profileService.getProfile(user);
        return ResponseEntity.ok(new ApiResponse(true, "Profile fetched successfully", profile));
    }

    @PutMapping
    public ResponseEntity<ApiResponse> updateMyProfile(@RequestHeader("Authorization") String token,
                                                       @RequestBody UpdateProfileRequestDTO updateRequest) {
        String username = jwtService.getUsername(token);
        User user = userService.getUserByUsername(username);
        Profile updatedProfile = profileService.updateProfile(user, updateRequest);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully", updatedProfile));
    }

    @PostMapping("/picture")
    public ResponseEntity<ApiResponse> uploadProfilePicture(@RequestHeader("Authorization") String token,
                                                            @RequestParam("file") MultipartFile file) {
        String username = jwtService.getUsername(token);
        User user = userService.getUserByUsername(username);
        profileService.updateProfilePic(user, file);
        return ResponseEntity.ok(new ApiResponse(true, "Profile picture updated successfully", null));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> viewProfileById(@PathVariable Long userId) {
        Profile profile = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(new ApiResponse(true, "Profile fetched successfully", profile));
    }

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<ApiResponse> viewMerchantProfileById(@PathVariable Long merchantId) {
        ProfileWithProductsDTO dto = profileService.getProfileWithProducts(merchantId);
        return ResponseEntity.ok(new ApiResponse(true, "Merchant profile fetched successfully", dto));
    }

    @GetMapping("/image/user/{userId}")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long userId) throws IOException {
        Profile profile = profileService.getProfileByUserId(userId);
        if (profile == null || profile.getProfilePictureUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        String relativePath = profile.getProfilePictureUrl().replaceFirst(".*/profiles/image/", "");
        File imageFile = new File(basePath + File.separator + relativePath.replace("/", File.separator));

        if (!imageFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        return ResponseEntity.ok()
                .header("Content-Type", Files.probeContentType(imageFile.toPath()))
                .body(imageBytes);
    }

    @GetMapping("/image/**")
    public ResponseEntity<byte[]> serveImage(HttpServletRequest request) throws IOException {
        String fullPath = request.getRequestURI().replace("/profiles/image/", "");
        File file = new File(basePath + File.separator + fullPath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        byte[] imageBytes = Files.readAllBytes(file.toPath());
        return ResponseEntity.ok()
                .header("Content-Type", Files.probeContentType(file.toPath()))
                .body(imageBytes);
    }
}
