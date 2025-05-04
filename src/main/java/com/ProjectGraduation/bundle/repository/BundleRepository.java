package com.ProjectGraduation.bundle.repository;

import com.ProjectGraduation.bundle.entity.Bundle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BundleRepository extends JpaRepository<Bundle, Long> {
    List<Bundle> findByMerchant_Username(String username);
}
