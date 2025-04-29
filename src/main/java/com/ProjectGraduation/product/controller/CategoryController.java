package com.ProjectGraduation.product.controller;
import com.ProjectGraduation.product.entity.Category;
import com.ProjectGraduation.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service ;

    @PostMapping()
    public Category addNewCategory (@RequestPart("name")  String name) throws IOException {

        Category category = new Category();
        category.setName( name);
        return service.addNewCategory(category) ;

    }

    @GetMapping
    public List<Category> getAllCategory (){
        return service.getAllCategory() ;
    }

}
