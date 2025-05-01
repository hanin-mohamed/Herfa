package com.ProjectGraduation.rating.ProductRating.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.repo.ProductRepository;
import com.ProjectGraduation.rating.ProductRating.entity.ProductRating;
import com.ProjectGraduation.rating.ProductRating.repo.ProductRatingRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRatingService {

    private final ProductRatingRepo repo ;
    private final UserRepo userRepo ;
    private final ProductRepository productRepository;

    public ProductRating addOrUpdateRating (Long userId , Long productId , int stars){
        User user = userRepo.findById(userId).orElseThrow(
                ()->new RuntimeException("User Not Found")) ;
        Product product = productRepository.findById(productId).orElseThrow(
                ()->new RuntimeException("Product Not Found"));

        if (stars<1 || stars>5){
            throw new IllegalArgumentException("Rating Must Be Between 1 and 5 stars");
        }
        ProductRating productRating = repo.findByUserAndProduct(user,product).orElse(new ProductRating());
        productRating.setUser(user);
        productRating.setProduct(product);
        productRating.setStars(stars);
        return repo.save(productRating) ;
    }

    public double getAverageRating(Long productId){
        Product product = productRepository.findById(productId).orElseThrow(
                ()->new RuntimeException("Product Not Found"));
        List<ProductRating> productRatings = repo.findAllByProduct(product);
        return productRatings.stream().mapToInt(ProductRating::getStars).average().orElse(0.0);
    }
}
