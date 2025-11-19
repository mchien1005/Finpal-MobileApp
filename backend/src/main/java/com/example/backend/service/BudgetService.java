package com.example.backend.service;

import com.example.backend.dto.BudgetRequest;
import com.example.backend.dto.BudgetResponse;
import com.example.backend.model.Budget;
import com.example.backend.model.Category;
import com.example.backend.model.Transaction;
import com.example.backend.model.User;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Budget Management (FR3.2)
 */
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Lấy tất cả budgets của user
     */
    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets(String username, Boolean isActive) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Budget> budgets;
        if (isActive != null) {
            budgets = budgetRepository.findByUserIdAndIsActive(user.getId(), isActive);
        } else {
            budgets = budgetRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }

        return budgets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy budgets đang active cho một ngày cụ thể
     */
    @Transactional(readOnly = true)
    public List<BudgetResponse> getActiveBudgets(String username, LocalDate date) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<Budget> budgets = budgetRepository.findActiveBudgetsForDate(user.getId(), targetDate);

        return budgets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết budget theo ID
     */
    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(String username, Long budgetId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        // Check ownership
        if (!budget.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to budget");
        }

        return convertToResponse(budget);
    }

    /**
     * Tạo budget mới
     */
    @Transactional
    public BudgetResponse createBudget(String username, BudgetRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate category if provided
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        Budget budget = new Budget();
        budget.setUserId(user.getId());
        budget.setCategoryId(request.getCategoryId());
        budget.setName(request.getName());
        budget.setAmount(request.getAmount());
        budget.setPeriod(Budget.BudgetPeriod.valueOf(request.getPeriod()));
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setAlertThreshold(request.getAlertThreshold());
        budget.setIsActive(true);

        Budget savedBudget = budgetRepository.save(budget);
        return convertToResponse(savedBudget);
    }

    /**
     * Cập nhật budget
     */
    @Transactional
    public BudgetResponse updateBudget(String username, Long budgetId, BudgetRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        // Check ownership
        if (!budget.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to budget");
        }

        // Validate category if provided
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        budget.setCategoryId(request.getCategoryId());
        budget.setName(request.getName());
        budget.setAmount(request.getAmount());
        budget.setPeriod(Budget.BudgetPeriod.valueOf(request.getPeriod()));
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setAlertThreshold(request.getAlertThreshold());

        Budget updatedBudget = budgetRepository.save(budget);
        return convertToResponse(updatedBudget);
    }

    /**
     * Xóa budget (soft delete)
     */
    @Transactional
    public void deleteBudget(String username, Long budgetId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        // Check ownership
        if (!budget.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to budget");
        }

        budget.setIsActive(false);
        budgetRepository.save(budget);
    }

    /**
     * Convert Budget entity to BudgetResponse DTO với progress tracking
     */
    private BudgetResponse convertToResponse(Budget budget) {
        BudgetResponse response = new BudgetResponse();
        response.setId(budget.getId());
        response.setUserId(budget.getUserId());
        response.setCategoryId(budget.getCategoryId());
        response.setName(budget.getName());
        response.setAmount(budget.getAmount());
        response.setPeriod(budget.getPeriod().toString());
        response.setStartDate(budget.getStartDate());
        response.setEndDate(budget.getEndDate());
        response.setIsActive(budget.getIsActive());
        response.setAlertThreshold(budget.getAlertThreshold());
        response.setCreatedAt(budget.getCreatedAt());
        response.setUpdatedAt(budget.getUpdatedAt());

        // Get category details if available
        if (budget.getCategoryId() != null) {
            categoryRepository.findById(budget.getCategoryId()).ifPresent(category -> {
                response.setCategoryName(category.getName());
                response.setCategoryIcon(category.getIcon());
                response.setCategoryColor(category.getColor());
            });
        }

        // Calculate progress
        calculateBudgetProgress(budget, response);

        return response;
    }

    /**
     * Tính toán tiến độ ngân sách
     */
    private void calculateBudgetProgress(Budget budget, BudgetResponse response) {
        LocalDateTime start = budget.getStartDate().atStartOfDay();
        LocalDateTime end = budget.getEndDate().atTime(23, 59, 59);

        // Calculate spent amount
        BigDecimal spentAmount;
        if (budget.getCategoryId() != null) {
            // Budget for specific category
            spentAmount = transactionRepository.sumByUserIdAndCategoryIdAndTypeAndDateRange(
                    budget.getUserId(),
                    budget.getCategoryId(),
                    Transaction.TransactionType.EXPENSE,
                    start,
                    end);
        } else {
            // Total budget (all expenses)
            spentAmount = transactionRepository.sumByUserIdAndTypeAndDateRange(
                    budget.getUserId(),
                    Transaction.TransactionType.EXPENSE,
                    start,
                    end);
        }

        spentAmount = (spentAmount != null) ? spentAmount : BigDecimal.ZERO;

        response.setSpentAmount(spentAmount);
        response.setRemainingAmount(budget.getAmount().subtract(spentAmount));

        // Calculate usage percentage
        double usagePercentage = 0.0;
        if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            usagePercentage = spentAmount.divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
        response.setUsagePercentage(usagePercentage);

        // Determine status
        String status;
        if (usagePercentage >= 100) {
            status = "EXCEEDED";
        } else if (usagePercentage >= budget.getAlertThreshold()) {
            status = "WARNING";
        } else {
            status = "OK";
        }
        response.setStatus(status);

        // Calculate days remaining
        LocalDate today = LocalDate.now();
        if (today.isAfter(budget.getEndDate())) {
            response.setDaysRemaining(0);
        } else if (today.isBefore(budget.getStartDate())) {
            response.setDaysRemaining((int) ChronoUnit.DAYS.between(budget.getStartDate(), budget.getEndDate()) + 1);
        } else {
            response.setDaysRemaining((int) ChronoUnit.DAYS.between(today, budget.getEndDate()) + 1);
        }
    }
}
