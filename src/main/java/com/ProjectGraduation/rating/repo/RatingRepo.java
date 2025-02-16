package com.ProjectGraduation.rating.repo;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.rating.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepo extends JpaRepository<Rating,Long> {

    Optional<Rating>findByUserAndProduct(User user , Product product) ;
    List<Rating>findAllByProduct(Product product) ;
}
