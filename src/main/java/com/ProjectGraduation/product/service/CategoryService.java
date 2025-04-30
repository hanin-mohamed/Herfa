package com.ProjectGraduation.product.service;

import com.ProjectGraduation.product.entity.Category;
import com.ProjectGraduation.product.repo.CategoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {


    private final CategoryRepo categoryRepo;

    public Category addNewCategory (Category category) {
        return categoryRepo.save(category) ;
    }
    public List<Category>getAllCategory(){
        return categoryRepo.findAll() ;
    }
    public Category getCategoryById(Long id){
        return categoryRepo.findById(id).orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
    }
}
