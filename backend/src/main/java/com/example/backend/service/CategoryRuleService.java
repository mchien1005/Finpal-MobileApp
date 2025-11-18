package com.example.backend.service;

import com.example.backend.model.CategoryRule;
import com.example.backend.repository.CategoryRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryRuleService {

    private final CategoryRuleRepository categoryRuleRepository;

    /**
     * Tự động phân loại category dựa trên merchant name
     * 
     * @param merchant Tên merchant (ví dụ: "GRAB VIETNAM", "SHOPEE")
     * @return categoryId phù hợp nhất, hoặc null nếu không tìm thấy
     */
    public Long suggestCategoryByMerchant(String merchant) {
        if (merchant == null || merchant.trim().isEmpty()) {
            return null;
        }

        String merchantUpper = merchant.trim().toUpperCase();
        log.debug("Auto-categorizing merchant: {}", merchantUpper);

        // 1. Ưu tiên: Tìm EXACT match trước (priority cao nhất)
        List<CategoryRule> exactRules = categoryRuleRepository.findExactMatchRulesForMerchant(merchantUpper);
        if (!exactRules.isEmpty()) {
            CategoryRule rule = exactRules.get(0); // Lấy rule có priority cao nhất
            log.info("Found EXACT match rule: {} -> categoryId: {}", rule.getKeyword(), rule.getCategoryId());
            return rule.getCategoryId();
        }

        // 2. Tìm STARTS_WITH match
        List<CategoryRule> startsWithRules = categoryRuleRepository.findStartsWithRulesForMerchant(merchantUpper);
        if (!startsWithRules.isEmpty()) {
            CategoryRule rule = startsWithRules.get(0);
            log.info("Found STARTS_WITH match rule: {} -> categoryId: {}", rule.getKeyword(), rule.getCategoryId());
            return rule.getCategoryId();
        }

        // 3. Tìm CONTAINS match
        List<CategoryRule> containsRules = categoryRuleRepository.findMatchingRulesForMerchant(merchantUpper);
        if (!containsRules.isEmpty()) {
            CategoryRule rule = containsRules.get(0);
            log.info("Found CONTAINS match rule: {} -> categoryId: {}", rule.getKeyword(), rule.getCategoryId());
            return rule.getCategoryId();
        }

        // 4. Tìm REGEX match (nếu có)
        Optional<CategoryRule> regexMatch = findRegexMatch(merchantUpper);
        if (regexMatch.isPresent()) {
            CategoryRule rule = regexMatch.get();
            log.info("Found REGEX match rule: {} -> categoryId: {}", rule.getKeyword(), rule.getCategoryId());
            return rule.getCategoryId();
        }

        log.debug("No matching rule found for merchant: {}", merchant);
        return null;
    }

    /**
     * Tìm rule khớp với regex pattern
     */
    private Optional<CategoryRule> findRegexMatch(String merchant) {
        List<CategoryRule> allRules = categoryRuleRepository.findByIsActiveTrueOrderByPriorityDesc();

        return allRules.stream()
                .filter(rule -> rule.getMatchType() == CategoryRule.MatchType.REGEX)
                .filter(rule -> {
                    try {
                        Pattern pattern = Pattern.compile(rule.getKeyword(), Pattern.CASE_INSENSITIVE);
                        return pattern.matcher(merchant).find();
                    } catch (Exception e) {
                        log.warn("Invalid regex pattern in rule {}: {}", rule.getId(), rule.getKeyword());
                        return false;
                    }
                })
                .findFirst();
    }

    /**
     * Lấy tất cả rules đang active
     */
    public List<CategoryRule> getAllActiveRules() {
        return categoryRuleRepository.findByIsActiveTrueOrderByPriorityDesc();
    }

    /**
     * Lấy rules theo categoryId
     */
    public List<CategoryRule> getRulesByCategory(Long categoryId) {
        return categoryRuleRepository.findByCategoryId(categoryId);
    }

    /**
     * Tạo rule mới
     */
    public CategoryRule createRule(CategoryRule rule) {
        return categoryRuleRepository.save(rule);
    }

    /**
     * Cập nhật rule
     */
    public CategoryRule updateRule(Long id, CategoryRule updatedRule) {
        CategoryRule existing = categoryRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category rule not found"));

        existing.setKeyword(updatedRule.getKeyword());
        existing.setCategoryId(updatedRule.getCategoryId());
        existing.setMatchType(updatedRule.getMatchType());
        existing.setPriority(updatedRule.getPriority());
        existing.setIsActive(updatedRule.getIsActive());

        return categoryRuleRepository.save(existing);
    }

    /**
     * Xóa rule
     */
    public void deleteRule(Long id) {
        categoryRuleRepository.deleteById(id);
    }
}
