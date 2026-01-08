package com.example.backend.repository;

import com.example.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByType(Category.CategoryType type);

    List<Category> findByTypeOrderByDisplayOrderAsc(Category.CategoryType type);

    Optional<Category> findByName(String name);

    // Tìm danh mục theo tên VÀ loại (dùng cho fallback "Khác" đúng loại)
    Optional<Category> findByNameAndType(String name, Category.CategoryType type);
}
