package com.ProjectGraduation.following.controller;

import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.following.service.FollowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowingController {

    private final FollowingService followingService;
    private final UserService userService;
    private final JWTService jwtService;

    @PostMapping("/{merchantId}")
    public ResponseEntity<ApiResponse> followMerchant(@RequestHeader("Authorization") String token,
                                                      @PathVariable Long merchantId) {
        try {
            String username = jwtService.getUsername(token);
            User follower = userService.getUserByUsername(username);

            followingService.followMerchant(follower.getId(), merchantId);
            return ResponseEntity.ok(new ApiResponse(true, "You are now following this merchant!", null));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to follow merchant: " + ex.getMessage(), null));
        }
    }

    @DeleteMapping("/{merchantId}")
    public ResponseEntity<ApiResponse> unfollowMerchant(@RequestHeader("Authorization") String token,
                                                        @PathVariable Long merchantId) {
        try {
            String username = jwtService.getUsername(token);
            User follower = userService.getUserByUsername(username);

            followingService.unfollowMerchant(follower.getId(), merchantId);
            return ResponseEntity.ok(new ApiResponse(true, "Unfollowed merchant!", null));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to unfollow merchant: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/followers")
    public ResponseEntity<ApiResponse> getFollowers(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.getUsername(token);
            User merchant = userService.getUserByUsername(username);

            return ResponseEntity.ok(new ApiResponse(true, "Followers fetched successfully", followingService.getFollowers(merchant)));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to fetch followers: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/followings")
    public ResponseEntity<ApiResponse> getFollowing(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.getUsername(token);
            User follower = userService.getUserByUsername(username);

            return ResponseEntity.ok(new ApiResponse(true, "Followings fetched successfully", followingService.getFollowing(follower)));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to fetch followings: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/followers/count/{merchantId}")
    public ResponseEntity<ApiResponse> getFollowersCount(@PathVariable Long merchantId) {
        try {
            long count = followingService.getFollowerCount(merchantId);
            return ResponseEntity.ok(new ApiResponse(true, "Followers count fetched successfully", count));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to fetch followers count: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/following/count/{followerId}")
    public ResponseEntity<ApiResponse> getFollowingCount(@PathVariable Long followerId) {
        try {
            long count = followingService.getFollowingCount(followerId);
            return ResponseEntity.ok(new ApiResponse(true, "Following count fetched successfully", count));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to fetch following count: " + ex.getMessage(), null));
        }
    }
}
