package com.example.backend.service;

import com.example.backend.dto.*;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard Service - FR2: Module "Bảng điều khiển Trực quan"
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    /**
     * FR2.1: Tổng quan Dòng tiền
     * Hiển thị: Tổng Thu nhập (Tháng) - Tổng Chi tiêu (Tháng) = Còn lại
     */
    @Transactional(readOnly = true)
    public CashFlowDTO getCashFlow(String username, LocalDate month) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Default to current month if not specified
        LocalDate targetMonth = (month != null) ? month : LocalDate.now();
        LocalDate startOfMonth = targetMonth.withDayOfMonth(1);
        LocalDate endOfMonth = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

        LocalDateTime start = startOfMonth.atStartOfDay();
        LocalDateTime end = endOfMonth.atTime(23, 59, 59);

        // Calculate income and expense for the month
        BigDecimal monthlyIncome = transactionRepository
                .sumByUserIdAndTypeAndDateRange(user.getId(), Transaction.TransactionType.INCOME, start, end);
        BigDecimal monthlyExpense = transactionRepository
                .sumByUserIdAndTypeAndDateRange(user.getId(), Transaction.TransactionType.EXPENSE, start, end);

        monthlyIncome = (monthlyIncome != null) ? monthlyIncome : BigDecimal.ZERO;
        monthlyExpense = (monthlyExpense != null) ? monthlyExpense : BigDecimal.ZERO;

        BigDecimal netSavings = monthlyIncome.subtract(monthlyExpense);

        // Calculate savings rate
        BigDecimal savingsRate = BigDecimal.ZERO;
        if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netSavings.divide(monthlyIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Get total balance from all active accounts
        List<Object[]> accountBalances = accountRepository.findAccountBalancesByUserId(user.getId());
        BigDecimal totalBalance = accountBalances.stream()
                .map(arr -> (BigDecimal) arr[0])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Compare with previous month
        LocalDate prevMonth = targetMonth.minusMonths(1);
        LocalDate prevStart = prevMonth.withDayOfMonth(1);
        LocalDate prevEnd = prevMonth.withDayOfMonth(prevMonth.lengthOfMonth());

        BigDecimal prevIncome = transactionRepository
                .sumByUserIdAndTypeAndDateRange(user.getId(), Transaction.TransactionType.INCOME,
                        prevStart.atStartOfDay(), prevEnd.atTime(23, 59, 59));
        BigDecimal prevExpense = transactionRepository
                .sumByUserIdAndTypeAndDateRange(user.getId(), Transaction.TransactionType.EXPENSE,
                        prevStart.atStartOfDay(), prevEnd.atTime(23, 59, 59));

        prevIncome = (prevIncome != null) ? prevIncome : BigDecimal.ZERO;
        prevExpense = (prevExpense != null) ? prevExpense : BigDecimal.ZERO;

        BigDecimal incomeChange = monthlyIncome.subtract(prevIncome);
        BigDecimal expenseChange = monthlyExpense.subtract(prevExpense);

        // Get transaction count
        Long count = transactionRepository.countByUserIdAndDateRange(user.getId(), start, end);
        Integer transactionCount = (count != null) ? count.intValue() : 0;

        return CashFlowDTO.builder()
                .monthlyIncome(monthlyIncome)
                .monthlyExpense(monthlyExpense)
                .netSavings(netSavings)
                .savingsRate(savingsRate)
                .currentMonth(targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .totalBalance(totalBalance)
                .incomeChange(incomeChange)
                .expenseChange(expenseChange)
                .transactionCount(transactionCount)
                .build();
    }

    /**
     * FR2.2: Biểu đồ Phân loại
     * Hiển thị chi tiêu theo category (data cho pie chart)
     */
    @Transactional(readOnly = true)
    public List<SpendingByCategoryDTO> getSpendingByCategory(String username, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Default to current month if not specified
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);

        // Get spending by category (EXPENSE only)
        List<Object[]> results = transactionRepository
                .getSpendingByCategory(user.getId(), startDateTime, endDateTime, Transaction.TransactionType.EXPENSE);

        // Calculate total for percentage
        BigDecimal total = results.stream()
                .map(arr -> (BigDecimal) arr[4])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build DTOs
        return results.stream()
                .map(arr -> {
                    Long categoryId = (Long) arr[0];
                    String categoryName = (String) arr[1];
                    String icon = (String) arr[2];
                    String color = (String) arr[3];
                    BigDecimal amount = (BigDecimal) arr[4];
                    Long count = (Long) arr[5];

                    Double percentage = 0.0;
                    if (total.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = amount.divide(total, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)).doubleValue();
                    }

                    SpendingByCategoryDTO dto = new SpendingByCategoryDTO();
                    dto.setCategoryId(categoryId);
                    dto.setCategoryName(categoryName);
                    dto.setCategoryIcon(icon);
                    dto.setCategoryColor(color);
                    dto.setTotalAmount(amount);
                    dto.setPercentage(percentage);
                    dto.setTransactionCount(count);
                    return dto;
                })
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .collect(Collectors.toList());
    }

    /**
     * Dashboard Summary - Tổng quan tổng hợp
     */
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        // Get cash flow
        CashFlowDTO cashFlow = getCashFlow(username, now);

        // Get top 5 expense categories
        List<SpendingByCategoryDTO> topCategories = getSpendingByCategory(username, startOfMonth, endOfMonth)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        // Build summary
        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setTotalBalance(cashFlow.getTotalBalance());
        summary.setMonthlyIncome(cashFlow.getMonthlyIncome());
        summary.setMonthlyExpense(cashFlow.getMonthlyExpense());
        summary.setNetSavings(cashFlow.getNetSavings());
        summary.setIncomeChangePercent(calculateChangePercent(
                cashFlow.getMonthlyIncome().subtract(cashFlow.getIncomeChange()),
                cashFlow.getMonthlyIncome()));
        summary.setExpenseChangePercent(calculateChangePercent(
                cashFlow.getMonthlyExpense().subtract(cashFlow.getExpenseChange()),
                cashFlow.getMonthlyExpense()));
        summary.setMonthlyTransactions(cashFlow.getTransactionCount());
        summary.setTopExpenseCategories(topCategories);

        // Counts
        summary.setTotalAccounts((int) accountRepository.findByUserIdAndIsActiveTrue(user.getId()).size());
        Long todayCount = transactionRepository.countByUserIdAndDateRange(
                user.getId(), now.atStartOfDay(), now.atTime(23, 59, 59));
        summary.setTodayTransactions((todayCount != null) ? todayCount.intValue() : 0);
        Long weekCount = transactionRepository.countByUserIdAndDateRange(
                user.getId(), now.minusDays(7).atStartOfDay(), now.atTime(23, 59, 59));
        summary.setWeekTransactions((weekCount != null) ? weekCount.intValue() : 0);

        return summary;
    }

    /**
     * Monthly Trend - Xu hướng thu chi theo tháng
     */
    @Transactional(readOnly = true)
    public List<MonthlyTrendDTO> getMonthlyTrend(String username, Integer months) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int monthCount = (months != null && months > 0) ? months : 6;
        List<MonthlyTrendDTO> trends = new ArrayList<>();

        YearMonth currentMonth = YearMonth.now();

        for (int i = monthCount - 1; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            LocalDate start = targetMonth.atDay(1);
            LocalDate end = targetMonth.atEndOfMonth();

            LocalDateTime startDateTime = start.atStartOfDay();
            LocalDateTime endDateTime = end.atTime(23, 59, 59);

            BigDecimal income = transactionRepository
                    .sumByUserIdAndTypeAndDateRange(user.getId(), Transaction.TransactionType.INCOME,
                            startDateTime, endDateTime);
            BigDecimal expense = transactionRepository
                    .sumByUserIdAndTypeAndDateRange(user.getId(), Transaction.TransactionType.EXPENSE,
                            startDateTime, endDateTime);

            income = (income != null) ? income : BigDecimal.ZERO;
            expense = (expense != null) ? expense : BigDecimal.ZERO;

            MonthlyTrendDTO dto = new MonthlyTrendDTO();
            dto.setYear(targetMonth.getYear());
            dto.setMonth(targetMonth.getMonthValue());
            dto.setMonthName(targetMonth.getMonth().toString());
            dto.setTotalIncome(income);
            dto.setTotalExpense(expense);
            dto.setNetSavings(income.subtract(expense));
            dto.setIncomeCount(transactionRepository.countByUserIdAndTypeAndDateRange(
                    user.getId(), Transaction.TransactionType.INCOME, startDateTime, endDateTime));
            dto.setExpenseCount(transactionRepository.countByUserIdAndTypeAndDateRange(
                    user.getId(), Transaction.TransactionType.EXPENSE, startDateTime, endDateTime));

            trends.add(dto);
        }

        return trends;
    }

    /**
     * Helper: Calculate percentage change
     */
    private BigDecimal calculateChangePercent(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue == null || oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return newValue.subtract(oldValue)
                .divide(oldValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
