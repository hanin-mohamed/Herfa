package com.ProjectGraduation.product.service;

import com.ProjectGraduation.product.entity.Category;
import com.ProjectGraduation.product.repo.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepo repo ;

    public Category addNewCategory (Category category) {
        return repo.save(category) ;
    }
    public List<Category>getAllCategory(){
        return repo.findAll() ;
    }

}
