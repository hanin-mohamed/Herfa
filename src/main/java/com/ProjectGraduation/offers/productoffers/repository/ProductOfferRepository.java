package com.ProjectGraduation.offers.productoffers.repository;

import com.ProjectGraduation.offers.productoffers.entity.ProductOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ProductOfferRepository extends JpaRepository<ProductOffer, Long> {

    @Query("SELECT po FROM ProductOffer po JOIN po.products p WHERE p.id = :productId " +
            "AND po.active = true AND po.startDate <= :now AND po.endDate >= :now")
    Optional<ProductOffer> findByProducts_IdAndActiveTrueAndStartDateBeforeAndEndDateAfter(
            @Param("productId") Long productId, @Param("now") LocalDateTime now, @Param("now2") LocalDateTime now2);

    @Query("SELECT po FROM ProductOffer po JOIN po.products p WHERE (p.id = :productId OR p.category.id = :categoryId) " +
            "AND po.active = true AND po.startDate <= :now AND po.endDate >= :now")
    Optional<ProductOffer> findByProductIdOrCategoryId(
            @Param("productId") Long productId, @Param("categoryId") Long categoryId, @Param("now") LocalDateTime now);
}