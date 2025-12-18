package com.example.backend.service;

import com.example.backend.dto.CategoryRequest;
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
 * Chức năng: Lấy danh sách category, tạo, sửa, xóa category
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Lấy tất cả categories
     * 
     * @return List<CategoryResponse> chứa tất cả categories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::fromEntity)
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
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết category theo ID
     * 
     * @param id ID của category
     * @return CategoryResponse chứa thông tin chi tiết category
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return CategoryResponse.fromEntity(category);
    }

    /**
     * Tạo category mới
     * 
     * @param request CategoryRequest chứa thông tin category mới
     * @return CategoryResponse chứa thông tin category đã tạo
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // Validate type
        Category.CategoryType categoryType;
        try {
            categoryType = Category.CategoryType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Loại danh mục không hợp lệ. Chỉ chấp nhận INCOME hoặc EXPENSE");
        }

        // Tạo category mới
        Category category = new Category();
        category.setName(request.getName());
        category.setType(categoryType);
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);

        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.fromEntity(savedCategory);
    }

    /**
     * Cập nhật category
     * 
     * @param id ID của category cần cập nhật
     * @param request CategoryRequest chứa thông tin mới
     * @return CategoryResponse chứa thông tin category đã cập nhật
     */
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category không tồn tại"));

        // Validate type nếu có thay đổi
        if (request.getType() != null) {
            Category.CategoryType categoryType;
            try {
                categoryType = Category.CategoryType.valueOf(request.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Loại danh mục không hợp lệ. Chỉ chấp nhận INCOME hoặc EXPENSE");
            }
            category.setType(categoryType);
        }

        // Cập nhật các trường khác
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        Category updatedCategory = categoryRepository.save(category);
        return CategoryResponse.fromEntity(updatedCategory);
    }

    /**
     * Xóa category
     * 
     * @param id ID của category cần xóa
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category không tồn tại"));

        // TODO: Kiểm tra xem có transactions sử dụng category này không
        // Nếu có, cân nhắc soft delete hoặc không cho phép xóa

        categoryRepository.deleteById(id);
    }
}
