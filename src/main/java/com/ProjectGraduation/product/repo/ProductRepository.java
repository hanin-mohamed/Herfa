package com.ProjectGraduation.product.repo;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.active = :active")
    List<Product> findActiveProducts(@Param("active") boolean active);

    List<Product> findByCategoryId(Long categoryId);
    List<Product> findAllByUser(User user);
}
