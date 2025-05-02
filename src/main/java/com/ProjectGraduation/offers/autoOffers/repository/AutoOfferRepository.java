package com.ProjectGraduation.offers.autoOffers.repository;

import com.ProjectGraduation.offers.autoOffers.entity.AutoOffer;
import com.ProjectGraduation.offers.autoOffers.utils.AutoOfferType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AutoOfferRepository extends JpaRepository<AutoOffer, Long> {

    @Query("SELECT o FROM AutoOffer o WHERE o.type = :type AND o.active = true AND o.startDate <= :now AND (o.endDate IS NULL OR o.endDate >= :now)")
    List<AutoOffer> findValidOffersByType(@Param("type") AutoOfferType type, @Param("now") LocalDateTime now);

    @Query("SELECT o FROM AutoOffer o WHERE o.type = :type AND o.active = true AND o.startDate <= :now AND (o.endDate IS NULL OR o.endDate >= :now) AND o.firstOrderOnly = true ORDER BY o.discount DESC, o.maxDiscount DESC")
    Optional<AutoOffer> findBestFirstOrderOffer(@Param("type") AutoOfferType type, @Param("now") LocalDateTime now);

    @Query("SELECT o FROM AutoOffer o WHERE o.type = :type AND o.active = true AND o.startDate <= :now AND (o.endDate IS NULL OR o.endDate >= :now) AND o.minOrderAmount <= :amount AND (o.product IS NULL OR o.product.id = :productId OR :categoryId IN (SELECT c.id FROM o.categories c)) ORDER BY o.discount DESC, o.maxDiscount DESC")
    Optional<AutoOffer> findBestMinOrderOffer(@Param("type") AutoOfferType type, @Param("now") LocalDateTime now, @Param("amount") double amount, @Param("productId") Long productId, @Param("categoryId") Long categoryId);

    @Query("SELECT o FROM AutoOffer o WHERE o.type = :type AND o.active = true AND o.startDate <= :now AND (o.endDate IS NULL OR o.endDate >= :now) AND o.requiredPoints <= :points AND (o.product IS NULL OR o.product.id = :productId OR :categoryId IN (SELECT c.id FROM o.categories c)) ORDER BY o.discount DESC, o.maxDiscount DESC")
    Optional<AutoOffer> findBestLoyaltyPointsOffer(@Param("type") AutoOfferType type, @Param("now") LocalDateTime now, @Param("points") int points, @Param("productId") Long productId, @Param("categoryId") Long categoryId);

    @Query("SELECT o FROM AutoOffer o WHERE o.type = :type AND o.active = true AND o.startDate <= :now AND (o.endDate IS NULL OR o.endDate >= :now) AND o.buyQuantity <= :quantity AND (o.product IS NULL OR o.product.id = :productId OR :categoryId IN (SELECT c.id FROM o.categories c)) ORDER BY o.discount DESC, o.maxDiscount DESC")
    Optional<AutoOffer> findBestBuyXGetYOffer(@Param("type") AutoOfferType type, @Param("now") LocalDateTime now, @Param("quantity") int quantity, @Param("productId") Long productId, @Param("categoryId") Long categoryId);
}