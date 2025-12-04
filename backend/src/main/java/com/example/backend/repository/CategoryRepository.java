package com.example.backend.repository;

import com.example.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByType(Category.CategoryType type);

    List<Category> findByParentIdIsNull();

    List<Category> findByParentId(Long parentId);

    List<Category> findByTypeOrderByDisplayOrderAsc(Category.CategoryType type);

    List<Category> findByIsSystemTrue();

    Optional<Category> findByName(String name);
}
