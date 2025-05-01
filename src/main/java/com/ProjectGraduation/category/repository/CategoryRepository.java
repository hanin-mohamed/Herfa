package com.ProjectGraduation.category.repository;


 import com.ProjectGraduation.category.entity.Category;
 import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
}
