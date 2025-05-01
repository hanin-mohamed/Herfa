package com.ProjectGraduation.offers.autoOffers.repository;

import com.ProjectGraduation.offers.autoOffers.entity.AutoOffer;
import com.ProjectGraduation.offers.autoOffers.utils.AutoOfferType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

public interface AutoOfferRepository extends JpaRepository<AutoOffer, Long> {
    @Query("SELECT a FROM AutoOffer a WHERE a.active = true AND (a.startDate IS NULL OR a.startDate <= :now) AND (a.endDate IS NULL OR a.endDate >= :now)")
    List<AutoOffer> findAllValid(LocalDateTime now);


    @Query("SELECT a FROM AutoOffer a WHERE a.active = true AND a.type = :type AND (a.startDate IS NULL OR a.startDate <= :now) AND (a.endDate IS NULL OR a.endDate >= :now)")
    List<AutoOffer> findValidOffersByType(@Param("type")AutoOfferType type, @Param("now") LocalDateTime now);
}
