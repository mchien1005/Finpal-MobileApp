package com.example.backend.dto;

import com.example.backend.model.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String type; // INCOME, EXPENSE
    private String icon;
    private String color;
    private Long parentId;
    private String parentName;
    private Boolean isSystem;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CategoryResponse> subCategories;

    public static CategoryResponse fromEntity(Category category) {
        return fromEntity(category, false);
    }

    public static CategoryResponse fromEntity(Category category, boolean includeSubCategories) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setType(category.getType() != null ? category.getType().name() : null);
        response.setParentId(category.getParentId());
        response.setIsSystem(category.getIsSystem());
        response.setDisplayOrder(category.getDisplayOrder());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        if (category.getParent() != null) {
            response.setParentName(category.getParent().getName());
        }

        if (includeSubCategories && category.getSubCategories() != null) {
            response.setSubCategories(
                    category.getSubCategories().stream()
                            .map(CategoryResponse::fromEntity)
                            .collect(Collectors.toList()));
        }

        return response;
    }
}
