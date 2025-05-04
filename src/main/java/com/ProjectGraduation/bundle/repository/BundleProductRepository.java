package com.ProjectGraduation.bundle.repository;

import com.ProjectGraduation.bundle.entity.BundleProduct;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BundleProductRepository extends JpaRepository<BundleProduct, Long> {
}
