package com.ProjectGraduation.rating.ProfileRating.controller;

import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.rating.ProfileRating.dto.ProfileRatingDTO;
import com.ProjectGraduation.rating.ProfileRating.service.ProfileRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profile-rating")
@RequiredArgsConstructor
public class ProfileRatingController {

    private final ProfileRatingService profileRatingService;
    private final JWTService jwtService;
    private final UserService userService;
    @PostMapping("/{ratedUserId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> rateUser(@RequestHeader("Authorization") String token,
                                           @PathVariable Long ratedUserId,
                                           @RequestParam int stars,
                                           @RequestParam(required = false) String comment) {

        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        Long raterId = userService.getUserByUsername(username).getId();

        profileRatingService.rateUser(raterId, ratedUserId, stars, comment);

        return ResponseEntity.ok("Rating submitted successfully!");
    }

    @GetMapping("/average/{ratedUserId}")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long ratedUserId) {
        double avgRating = profileRatingService.getAverageRating(ratedUserId);
        return ResponseEntity.ok(avgRating);
    }

    @GetMapping("/all/{ratedUserId}")
    public ResponseEntity<List<ProfileRatingDTO>> getAllRatingsForUser(@PathVariable Long ratedUserId) {
        return ResponseEntity.ok(profileRatingService.getAllRatings(ratedUserId));
    }
}
