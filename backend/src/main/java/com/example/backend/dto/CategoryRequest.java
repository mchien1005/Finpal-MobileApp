package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để tạo hoặc cập nhật Category
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    @NotNull(message = "Loại danh mục không được để trống")
    private String type; // INCOME hoặc EXPENSE

    private String icon;

    private String color;

    private Long parentId; // Null nếu là parent category

    private Boolean isSystem = false;

    private Integer displayOrder = 0;
}
