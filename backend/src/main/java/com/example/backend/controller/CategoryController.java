package com.example.backend.controller;

import com.example.backend.dto.CategoryResponse;
import com.example.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý Danh mục (Categories)
 *
 * Chức năng:
 * - Lấy danh sách categories (theo type hoặc parent categories)
 * - Lấy chi tiết 1 category
 * - Lấy danh sách subcategories của 1 category
 *
 * Notes:
 * - Categories là các loại chi tiêu/thu nhập (Ăn uống, Di chuyển, Thu nhập...)
 * - Thường dùng cho hiển thị trên Dashboard và để phân loại giao dịch
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/categories
     * Nếu truyền `type` (INCOME/EXPENSE) sẽ trả về categories theo type
     * Ngược lại trả về parent categories (cấp cha)
     */
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

    /**
     * GET /api/categories/{id}
     * Lấy chi tiết một category theo id
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * GET /api/categories/{id}/subcategories
     * Lấy danh sách subcategories thuộc category {id}
     */
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubCategories(@PathVariable Long id) {
        List<CategoryResponse> subCategories = categoryService.getSubCategories(id);
        return ResponseEntity.ok(subCategories);
    }
}
