package com.example.backend.service;

import com.example.backend.dto.CategoryResponse;
import com.example.backend.model.Category;
import com.example.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> CategoryResponse.fromEntity(category, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByType(String type) {
        Category.CategoryType categoryType = Category.CategoryType.valueOf(type.toUpperCase());
        return categoryRepository.findByType(categoryType)
                .stream()
                .map(category -> CategoryResponse.fromEntity(category, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getParentCategories() {
        return categoryRepository.findByParentIdIsNull()
                .stream()
                .map(category -> CategoryResponse.fromEntity(category, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId)
                .stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return CategoryResponse.fromEntity(category, true);
    }
}
