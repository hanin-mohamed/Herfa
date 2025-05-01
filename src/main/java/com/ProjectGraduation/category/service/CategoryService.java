package com.ProjectGraduation.category.service;

import com.ProjectGraduation.category.entity.Category;
import com.ProjectGraduation.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {


    private final CategoryRepository categoryRepository;

    public Category addNewCategory (Category category) {
        return categoryRepository.save(category) ;
    }
    public List<Category>getAllCategory(){
        return categoryRepository.findAll() ;
    }
    public Category getCategoryById(Long id){
        return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
    }
}
