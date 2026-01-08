package com.example.backend.controller;

import com.example.backend.dto.CategoryRequest;
import com.example.backend.dto.CategoryResponse;
import com.example.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý Danh mục (Categories)
 *
 * Chức năng:
 * - Lấy danh sách categories (tất cả hoặc theo type)
 * - Lấy chi tiết 1 category
 * - Tạo, cập nhật, xóa category
 *
 * Notes:
 * - Categories là các loại chi tiêu/thu nhập (Ăn uống, Di chuyển, Thu nhập...)
 * - Thường dùng cho hiển thị trên Dashboard và để phân loại giao dịch
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "API quản lý danh mục thu chi (Categories)")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/categories
     * Nếu truyền `type` (INCOME/EXPENSE) sẽ trả về categories theo type
     * Ngược lại trả về tất cả categories
     */
    @GetMapping
    @Operation(
            summary = "Lấy danh sách danh mục",
            description = "Lấy tất cả danh mục hoặc lọc theo loại (INCOME/EXPENSE)"
    )
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @RequestParam(required = false) String type) {
        List<CategoryResponse> categories;
        if (type != null && !type.isEmpty()) {
            categories = categoryService.getCategoriesByType(type);
        } else {
            categories = categoryService.getAllCategories();
        }
        return ResponseEntity.ok(categories);
    }

    /**
     * GET /api/categories/{id}
     * Lấy chi tiết một category theo id
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy chi tiết danh mục",
            description = "Lấy thông tin chi tiết của một danh mục theo ID"
    )
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * POST /api/categories
     * Tạo category mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Tạo danh mục mới",
            description = "Tạo một danh mục thu chi mới. Chỉ ADMIN mới có quyền tạo."
    )
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    /**
     * PUT /api/categories/{id}
     * Cập nhật category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cập nhật danh mục",
            description = "Cập nhật thông tin danh mục. Chỉ ADMIN mới có quyền cập nhật."
    )
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    /**
     * DELETE /api/categories/{id}
     * Xóa category
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Xóa danh mục",
            description = "Xóa một danh mục. Chỉ ADMIN mới có quyền xóa."
    )
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
