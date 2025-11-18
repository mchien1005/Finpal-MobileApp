package com.example.backend.controller;

import com.example.backend.dto.CategoryResponse;
import com.example.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @RequestParam(required = false) String type) {
        List<CategoryResponse> categories;
        if (type != null && !type.isEmpty()) {
            categories = categoryService.getCategoriesByType(type);
        } else {
            categories = categoryService.getParentCategories();
        }
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubCategories(@PathVariable Long id) {
        List<CategoryResponse> subCategories = categoryService.getSubCategories(id);
        return ResponseEntity.ok(subCategories);
    }
}
