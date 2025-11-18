package com.example.backend.repository;

import com.example.backend.model.CategoryRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRuleRepository extends JpaRepository<CategoryRule, Long> {

    /**
     * Tìm tất cả rules đang active, sắp xếp theo priority giảm dần
     */
    List<CategoryRule> findByIsActiveTrueOrderByPriorityDesc();

    /**
     * Tìm rules theo categoryId
     */
    List<CategoryRule> findByCategoryId(Long categoryId);

    /**
     * Tìm rules khớp với merchant text (CONTAINS)
     */
    @Query("SELECT cr FROM CategoryRule cr WHERE cr.isActive = true " +
            "AND cr.matchType = 'CONTAINS' " +
            "AND UPPER(:merchant) LIKE CONCAT('%', UPPER(cr.keyword), '%') " +
            "ORDER BY cr.priority DESC")
    List<CategoryRule> findMatchingRulesForMerchant(@Param("merchant") String merchant);

    /**
     * Tìm rules khớp chính xác (EXACT)
     */
    @Query("SELECT cr FROM CategoryRule cr WHERE cr.isActive = true " +
            "AND cr.matchType = 'EXACT' " +
            "AND UPPER(cr.keyword) = UPPER(:merchant) " +
            "ORDER BY cr.priority DESC")
    List<CategoryRule> findExactMatchRulesForMerchant(@Param("merchant") String merchant);

    /**
     * Tìm rules bắt đầu bằng keyword (STARTS_WITH)
     */
    @Query("SELECT cr FROM CategoryRule cr WHERE cr.isActive = true " +
            "AND cr.matchType = 'STARTS_WITH' " +
            "AND UPPER(:merchant) LIKE CONCAT(UPPER(cr.keyword), '%') " +
            "ORDER BY cr.priority DESC")
    List<CategoryRule> findStartsWithRulesForMerchant(@Param("merchant") String merchant);
}
