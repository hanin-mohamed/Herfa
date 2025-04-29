package com.ProjectGraduation.rating.ProductRating.controller;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.rating.ProductRating.entity.ProductRating;
import com.ProjectGraduation.rating.ProductRating.service.ProductRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class ProductRatingController {

    private final ProductRatingService service ;

    private final JWTService jwtService ;

    private final UserRepo userRepo ;

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
        ProductRating productRating = service.addOrUpdateRating(userId,productId,stars);
        return ResponseEntity.ok(productRating) ;
    }


    @GetMapping()
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getProductAverageRating(@RequestParam Long productId) {
        double averageRating = service.getAverageRating(productId);
        return ResponseEntity.ok(averageRating);
    }
}
