package com.example.backend.service;

import com.example.backend.dto.CategoryResponse;
import com.example.backend.model.Category;
import com.example.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý Danh mục (Category)
 * Chức năng: Lấy danh sách category (tất cả, theo type, parent/sub categories)
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Lấy tất cả categories (bao gồm cả subcategories)
     * 
     * @return List<CategoryResponse> chứa tất cả categories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> CategoryResponse.fromEntity(category, true))
                .collect(Collectors.toList());
    }

    /**
     * Lấy categories theo loại (INCOME hoặc EXPENSE)
     * 
     * @param type Loại category ("INCOME" hoặc "EXPENSE")
     * @return List<CategoryResponse> chứa categories thuộc loại đã chọn
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByType(String type) {
        Category.CategoryType categoryType = Category.CategoryType.valueOf(type.toUpperCase());
        return categoryRepository.findByType(categoryType)
                .stream()
                .map(category -> CategoryResponse.fromEntity(category, true))
                .collect(Collectors.toList());
    }

    /**
     * Lấy các parent categories (categories không có parent)
     * 
     * @return List<CategoryResponse> chứa các parent categories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getParentCategories() {
        return categoryRepository.findByParentIdIsNull()
                .stream()
                .map(category -> CategoryResponse.fromEntity(category, true))
                .collect(Collectors.toList());
    }

    /**
     * Lấy các subcategories của một parent category
     * 
     * @param parentId ID của parent category
     * @return List<CategoryResponse> chứa các subcategories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId)
                .stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết category theo ID (bao gồm subcategories nếu có)
     * 
     * @param id ID của category
     * @return CategoryResponse chứa thông tin chi tiết category
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return CategoryResponse.fromEntity(category, true);
    }
}
