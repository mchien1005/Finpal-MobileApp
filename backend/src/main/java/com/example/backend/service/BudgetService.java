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
 * Service quản lý Ngân sách (Budget Management) - FR3.2
 * Chức năng: Tạo/sửa/xóa ngân sách, theo dõi tiến độ sử dụng, cảnh báo khi vượt
 * ngưỡng
 */
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Lấy tất cả ngân sách của user
     * 
     * @param username Tên đăng nhập của user
     * @param isActive Lọc theo trạng thái hoạt động (true/false, nếu null thì lấy
     *                 tất cả)
     * @return List<BudgetResponse> chứa danh sách ngân sách và tiến độ sử dụng
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
     * Lấy các ngân sách đang hoạt động cho một ngày cụ thể
     * 
     * @param username Tên đăng nhập của user
     * @param date     Ngày cần kiểm tra (nếu null thì lấy ngày hôm nay)
     * @return List<BudgetResponse> chứa danh sách ngân sách đang hoạt động
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
     * Lấy chi tiết ngân sách theo ID
     * 
     * @param username Tên đăng nhập của user (dùng để kiểm tra quyền sở hữu)
     * @param budgetId ID của ngân sách
     * @return BudgetResponse chứa thông tin chi tiết ngân sách và tiến độ
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
     * Tạo ngân sách mới
     * 
     * @param username Tên đăng nhập của user
     * @param request  Dữ liệu ngân sách (name, amount, period, categoryId,
     *                 startDate, endDate, alertThreshold)
     * @return BudgetResponse chứa thông tin ngân sách vừa tạo
     */
    @Transactional
    public BudgetResponse createBudget(String username, BudgetRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra category hợp lệ (nếu có)
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
     * Cập nhật ngân sách
     * 
     * @param username Tên đăng nhập của user
     * @param budgetId ID của ngân sách cần cập nhật
     * @param request  Dữ liệu mới (name, amount, period, categoryId, startDate,
     *                 endDate, alertThreshold)
     * @return BudgetResponse chứa thông tin ngân sách sau khi cập nhật
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
     * Xóa ngân sách (soft delete - chỉ set isActive = false)
     * 
     * @param username Tên đăng nhập của user
     * @param budgetId ID của ngân sách cần xóa
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
     * Chuyển Budget entity sang BudgetResponse DTO với tiến độ sử dụng
     * Tính toán: số tiền đã dùng, còn lại, %, trạng thái (OK/WARNING/EXCEEDED)
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

        // Lấy thông tin category (nếu có)
        if (budget.getCategoryId() != null) {
            categoryRepository.findById(budget.getCategoryId()).ifPresent(category -> {
                response.setCategoryName(category.getName());
                response.setCategoryIcon(category.getIcon());
                response.setCategoryColor(category.getColor());
            });
        }

        // Tính toán tiến độ ngân sách
        calculateBudgetProgress(budget, response);

        return response;
    }

    /**
     * Tính toán tiến độ ngân sách
     * - Số tiền đã chi (spentAmount)
     * - Số tiền còn lại (remainingAmount)
     * - Phần trăm sử dụng (usagePercentage)
     * - Trạng thái (OK/WARNING/EXCEEDED)
     * - Số ngày còn lại
     */
    private void calculateBudgetProgress(Budget budget, BudgetResponse response) {
        LocalDateTime start = budget.getStartDate().atStartOfDay();
        LocalDateTime end = budget.getEndDate().atTime(23, 59, 59);

        // Tính số tiền đã chi
        BigDecimal spentAmount;
        if (budget.getCategoryId() != null) {
            // Ngân sách cho category cụ thể
            spentAmount = transactionRepository.sumByUserIdAndCategoryIdAndTypeAndDateRange(
                    budget.getUserId(),
                    budget.getCategoryId(),
                    Transaction.TransactionType.EXPENSE,
                    start,
                    end);
        } else {
            // Ngân sách tổng (tất cả chi tiêu)
            spentAmount = transactionRepository.sumByUserIdAndTypeAndDateRange(
                    budget.getUserId(),
                    Transaction.TransactionType.EXPENSE,
                    start,
                    end);
        }

        spentAmount = (spentAmount != null) ? spentAmount : BigDecimal.ZERO;

        response.setSpentAmount(spentAmount);
        response.setRemainingAmount(budget.getAmount().subtract(spentAmount));

        // Tính phần trăm sử dụng
        double usagePercentage = 0.0;
        if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            usagePercentage = spentAmount.divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
        response.setUsagePercentage(usagePercentage);

        // Xác định trạng thái (EXCEEDED/WARNING/OK)
        String status;
        if (usagePercentage >= 100) {
            status = "EXCEEDED";
        } else if (usagePercentage >= budget.getAlertThreshold()) {
            status = "WARNING";
        } else {
            status = "OK";
        }
        response.setStatus(status);

        // Tính số ngày còn lại
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
