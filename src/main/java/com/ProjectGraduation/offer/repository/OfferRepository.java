package com.ProjectGraduation.offer.repository;

import com.ProjectGraduation.offer.entity.Offer;
import com.ProjectGraduation.product.entity.Category;
import com.ProjectGraduation.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer,Long> {
    List<Offer> findByProductAndActiveTrue(Product product);
    List<Offer> findByCategoryAndActiveTrue(Category category);
}
