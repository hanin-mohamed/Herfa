package com.ProjectGraduation.category.controller;

import com.ProjectGraduation.auth.api.model.ApiResponse;
import com.ProjectGraduation.category.entity.Category;
import com.ProjectGraduation.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse> addNewCategory(@RequestBody Category category) {
        Category saved = categoryService.addNewCategory(category);
        return ResponseEntity.ok(new ApiResponse(true, "Category added successfully", saved));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCategory() {
        List<Category> categories = categoryService.getAllCategory();
        return ResponseEntity.ok(new ApiResponse(true, "All categories fetched successfully", categories));
    }
}
