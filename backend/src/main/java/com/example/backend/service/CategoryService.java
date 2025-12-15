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

        // Validate parent category nếu có
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category không tồn tại"));
            
            // Parent category phải cùng type
            if (!parent.getType().equals(categoryType)) {
                throw new RuntimeException("Parent category phải cùng loại với category con");
            }
        }

        // Tạo category mới
        Category category = new Category();
        category.setName(request.getName());
        category.setType(categoryType);
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setParentId(request.getParentId());
        category.setIsSystem(request.getIsSystem() != null ? request.getIsSystem() : false);
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);

        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.fromEntity(savedCategory, true);
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

        // Không cho phép cập nhật category hệ thống
        if (category.getIsSystem()) {
            throw new RuntimeException("Không thể cập nhật category hệ thống");
        }

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

        // Validate parent category nếu có
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category không tồn tại"));
            
            // Không cho phép set parent là chính nó
            if (parent.getId().equals(id)) {
                throw new RuntimeException("Category không thể là parent của chính nó");
            }
            
            // Parent category phải cùng type
            if (!parent.getType().equals(category.getType())) {
                throw new RuntimeException("Parent category phải cùng loại với category con");
            }
            
            category.setParentId(request.getParentId());
        } else if (request.getParentId() == null && request.getName() != null) {
            // Cho phép set parentId = null (chuyển thành parent category)
            category.setParentId(null);
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
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        Category updatedCategory = categoryRepository.save(category);
        return CategoryResponse.fromEntity(updatedCategory, true);
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

        // Không cho phép xóa category hệ thống
        if (category.getIsSystem()) {
            throw new RuntimeException("Không thể xóa category hệ thống");
        }

        // Kiểm tra xem có subcategories không
        List<Category> subCategories = categoryRepository.findByParentId(id);
        if (!subCategories.isEmpty()) {
            throw new RuntimeException("Không thể xóa category có subcategories. Vui lòng xóa subcategories trước");
        }

        // TODO: Kiểm tra xem có transactions sử dụng category này không
        // Nếu có, cân nhắc soft delete hoặc không cho phép xóa

        categoryRepository.deleteById(id);
    }
}

