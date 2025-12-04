package com.example.backend.repository;

import com.example.backend.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

        List<Budget> findByUserId(Long userId);

        List<Budget> findByUserIdAndIsActiveTrue(Long userId);

        List<Budget> findByUserIdAndIsActive(Long userId, Boolean isActive);

        List<Budget> findByUserIdOrderByCreatedAtDesc(Long userId);

        Optional<Budget> findByIdAndUserId(Long id, Long userId);

        List<Budget> findByUserIdAndCategoryId(Long userId, Long categoryId);

        @Query("SELECT b FROM Budget b WHERE b.userId = :userId AND b.startDate <= :date AND b.endDate >= :date AND b.isActive = true")
        List<Budget> findActiveBudgetsByUserIdAndDate(
                        @Param("userId") Long userId,
                        @Param("date") LocalDate date);

        @Query("SELECT b FROM Budget b WHERE b.userId = :userId AND b.startDate <= :date AND b.endDate >= :date AND b.isActive = true")
        List<Budget> findActiveBudgetsForDate(
                        @Param("userId") Long userId,
                        @Param("date") LocalDate date);

        @Query("SELECT b FROM Budget b WHERE b.userId = :userId AND b.categoryId = :categoryId AND b.startDate <= :date AND b.endDate >= :date AND b.isActive = true")
        Optional<Budget> findActiveBudgetByUserIdAndCategoryIdAndDate(
                        @Param("userId") Long userId,
                        @Param("categoryId") Long categoryId,
                        @Param("date") LocalDate date);
}
