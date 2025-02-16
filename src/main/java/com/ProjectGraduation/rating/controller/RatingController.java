package com.ProjectGraduation.rating.controller;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.rating.service.RatingService;
import com.ProjectGraduation.rating.entity.Rating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rating")
public class RatingController {
    @Autowired
    private RatingService service ;
    @Autowired
    private JWTService jwtService ;
    @Autowired
    private UserRepo userRepo ;

    @PostMapping()
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?>rateProduct(
            @RequestHeader("Authorization") String token,
            @RequestParam Long productId,
            @RequestParam int stars
    ){
        String userName = jwtService.getUsername(token.replace("Bearer ",""));
        User user = userRepo.findByUsernameIgnoreCase(userName).orElseThrow(
                ()->new RuntimeException("User Not Found"));

        Long userId = user.getId();
        Rating rating = service.addOrUpdateRating(userId,productId,stars);
        return ResponseEntity.ok(rating) ;
    }


    @GetMapping()
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getProductAverageRating(@RequestParam Long productId) {
        double averageRating = service.getAverageRating(productId);
        return ResponseEntity.ok(averageRating);
    }
}
