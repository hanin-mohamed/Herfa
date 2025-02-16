package com.ProjectGraduation.product.repo;


 import com.ProjectGraduation.product.entity.Category;
 import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepo extends JpaRepository<Category,Long> {
}
