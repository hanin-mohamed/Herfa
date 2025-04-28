package com.ProjectGraduation.following.controller;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.following.service.FollowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/following")
@RequiredArgsConstructor
public class FollowingController {

    private final FollowingService followingService;
    private final UserService userService;
    private final JWTService jwtService;
    @PostMapping("/{merchantId}")
    public ResponseEntity<String> followMerchant(@RequestHeader("Authorization") String token,
                                                 @PathVariable Long merchantId) {
        String username = jwtService.getUsername(token);
        User follower = userService.getUserByUsername(username);

        followingService.followMerchant(follower.getId(), merchantId);

        return ResponseEntity.ok("You are now following this merchant!");
    }

    @DeleteMapping("/{merchantId}")
    public ResponseEntity<String> unfollowMerchant(@RequestHeader("Authorization") String token,
                                                   @PathVariable Long merchantId) {
        String username = jwtService.getUsername(token);
        User follower = userService.getUserByUsername(username);

        followingService.unfollowMerchant(follower.getId(), merchantId);

        return ResponseEntity.ok("Unfollowed merchant!");
    }


    @GetMapping("/followers")
    public ResponseEntity<?> getFollowers(@RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token);
        User merchant = userService.getUserByUsername(username);

        return ResponseEntity.ok(followingService.getFollowers(merchant));
    }
    @GetMapping("/following")
    public ResponseEntity<?> getFollowing(@RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token);
        User follower = userService.getUserByUsername(username);

        return ResponseEntity.ok(followingService.getFollowing(follower));
    }
    @GetMapping("/followers/count/{merchantId}")
    public ResponseEntity<Long> getFollowersCount(@PathVariable Long merchantId) {
        long count = followingService.getFollowerCount(merchantId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/following/count/{followerId}")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long followerId) {
        long count = followingService.getFollowingCount(followerId);
        return ResponseEntity.ok(count);
    }

}
