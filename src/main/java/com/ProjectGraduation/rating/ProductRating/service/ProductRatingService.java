package com.ProjectGraduation.rating.ProductRating.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.auth.service.JWTService;
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

    private final ProductRatingRepo repo;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final JWTService jwtService;

    public ProductRating addOrUpdateRating(String token, Long productId, int stars) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));

        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars");
        }

        ProductRating productRating = repo.findByUserAndProduct(user, product).orElse(new ProductRating());
        productRating.setUser(user);
        productRating.setProduct(product);
        productRating.setStars(stars);
        return repo.save(productRating);
    }

    public double getAverageRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));

        List<ProductRating> ratings = repo.findAllByProduct(product);
        return ratings.stream().mapToInt(ProductRating::getStars).average().orElse(0.0);
    }
}
