package com.ProjectGraduation.rating.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.repo.ProductRepo;
import com.ProjectGraduation.rating.entity.Rating;
import com.ProjectGraduation.rating.repo.RatingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingService {

    @Autowired
    private RatingRepo repo ;
    @Autowired
    private UserRepo userRepo ;
    @Autowired
    private ProductRepo productRepo ;
    public Rating addOrUpdateRating (Long userId , Long productId , int stars){
        User user = userRepo.findById(userId).orElseThrow(
                ()->new RuntimeException("User Not Found")) ;
        Product product = productRepo.findById(productId).orElseThrow(
                ()->new RuntimeException("Product Not Found"));

        if (stars<1 || stars>5){
            throw new IllegalArgumentException("Rating Must Be Between 1 and 5 stars");
        }
        Rating rating = repo.findByUserAndProduct(user,product).orElse(new Rating());
        rating.setUser(user);
        rating.setProduct(product);
        rating.setStars(stars);
        return repo.save(rating) ;
    }

    public double getAverageRating(Long productId){
        Product product = productRepo.findById(productId).orElseThrow(
                ()->new RuntimeException("Product Not Found"));
        List<Rating>ratings = repo.findAllByProduct(product);
        return ratings.stream().mapToInt(Rating::getStars).average().orElse(0.0);
    }
}
