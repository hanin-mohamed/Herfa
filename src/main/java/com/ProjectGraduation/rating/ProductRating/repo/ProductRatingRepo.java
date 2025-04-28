package com.ProjectGraduation.rating.ProductRating.repo;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.rating.ProductRating.entity.ProductRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRatingRepo extends JpaRepository<ProductRating,Long> {

    Optional<ProductRating>findByUserAndProduct(User user , Product product) ;
    List<ProductRating>findAllByProduct(Product product) ;
}
