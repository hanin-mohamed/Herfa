package com.ProjectGraduation.product.service;

import com.ProjectGraduation.product.entity.Category;
import com.ProjectGraduation.product.repo.CategoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {


    private final CategoryRepo repo ;

    public Category addNewCategory (Category category) {
        return repo.save(category) ;
    }
    public List<Category>getAllCategory(){
        return repo.findAll() ;
    }

}
