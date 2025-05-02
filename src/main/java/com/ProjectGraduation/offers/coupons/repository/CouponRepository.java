package com.ProjectGraduation.offers.coupons.repository;

import com.ProjectGraduation.offers.coupons.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    @Query("SELECT c FROM Coupon c WHERE c.code = :code AND c.active = true " +
            "AND (c.expiryDate IS NULL OR c.expiryDate >= :now)")
    Optional<Coupon> findByCodeAndActiveTrueAndStartDateBeforeAndEndDateAfter(
            @Param("code") String code, @Param("now") LocalDateTime now, @Param("now2") LocalDateTime now2);
}