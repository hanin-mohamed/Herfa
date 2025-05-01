package com.ProjectGraduation.offers.productoffer.repository;

import com.ProjectGraduation.offers.productoffer.entity.ProductOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ProductOfferRepository extends JpaRepository<ProductOffer,Long> {

    Optional<ProductOffer> findByProducts_IdAndActiveTrueAndStartDateBeforeAndEndDateAfter(Long productId, LocalDateTime now1, LocalDateTime now2);
}
