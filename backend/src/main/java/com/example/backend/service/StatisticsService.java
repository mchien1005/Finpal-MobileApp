package com.example.backend.service;

import com.example.backend.dto.IncomeExpenseComparisonDTO;
import com.example.backend.dto.SpendingInsightDTO;
import com.example.backend.model.Transaction;
import com.example.backend.model.User;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Thống kê & Phân tích (Statistics & Analytics)
 * Chức năng: So sánh thu/chi, phân tích insight chi tiêu, phát hiện giao dịch
 * bất thường (anomaly)
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {

        private final TransactionRepository transactionRepository;
        private final UserRepository userRepository;

        /**
         * So sánh Thu nhập vs Chi tiêu cho một khoảng thời gian
         * 
         * @param username    Tên đăng nhập của user
         * @param startDate   Ngày bắt đầu
         * @param endDate     Ngày kết thúc
         * @param periodLabel Nhãn thời gian (ví dụ: "Tháng 11/2025", "Q4 2025")
         * @return IncomeExpenseComparisonDTO chứa tổng thu, tổng chi, tiết kiệm ròng,
         *         tỷ lệ tiết kiệm, trung bình
         */
        @Transactional(readOnly = true)
        @SuppressWarnings("null")
        public IncomeExpenseComparisonDTO getIncomeExpenseComparison(String username, LocalDate startDate,
                        LocalDate endDate, String periodLabel) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                LocalDateTime start = startDate.atStartOfDay();
                LocalDateTime end = endDate.atTime(23, 59, 59);

                BigDecimal totalIncome = transactionRepository
                                .sumByUserIdAndTypeAndDateRange(user.getId(), Transaction.TransactionType.INCOME, start,
                                                end);
                BigDecimal totalExpense = transactionRepository
                                .sumByUserIdAndTypeAndDateRange(user.getId(), Transaction.TransactionType.EXPENSE,
                                                start, end);

                Long incomeCount = transactionRepository
                                .countByUserIdAndTypeAndDateRange(user.getId(), Transaction.TransactionType.INCOME,
                                                start, end);
                Long expenseCount = transactionRepository
                                .countByUserIdAndTypeAndDateRange(user.getId(), Transaction.TransactionType.EXPENSE,
                                                start, end);

                IncomeExpenseComparisonDTO dto = new IncomeExpenseComparisonDTO();
                dto.setPeriod(periodLabel != null ? periodLabel : "Custom Period");
                dto.setTotalIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO);
                dto.setTotalExpense(totalExpense != null ? totalExpense : BigDecimal.ZERO);
                dto.setNetSavings(dto.getTotalIncome().subtract(dto.getTotalExpense()));

                dto.setIncomeTransactionCount(incomeCount);
                dto.setExpenseTransactionCount(expenseCount);

                // Tính tỷ lệ tiết kiệm (%) = (Thu - Chi) / Thu * 100
                if (dto.getTotalIncome().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal savingsRate = dto.getNetSavings()
                                        .divide(dto.getTotalIncome(), 4, RoundingMode.HALF_UP)
                                        .multiply(BigDecimal.valueOf(100));
                        dto.setSavingsRate(savingsRate.doubleValue());
                } else {
                        dto.setSavingsRate(0.0);
                }

                // Tính trung bình thu nhập/chi tiêu trên 1 giao dịch
                if (incomeCount > 0) {
                        dto.setAverageIncome(dto.getTotalIncome().divide(BigDecimal.valueOf(incomeCount), 2,
                                        RoundingMode.HALF_UP));
                } else {
                        dto.setAverageIncome(BigDecimal.ZERO);
                }

                if (expenseCount > 0) {
                        dto.setAverageExpense(
                                        dto.getTotalExpense().divide(BigDecimal.valueOf(expenseCount), 2,
                                                        RoundingMode.HALF_UP));
                } else {
                        dto.setAverageExpense(BigDecimal.ZERO);
                }

                return dto;
        }

        /**
         * Phân tích Insight & Pattern chi tiêu (rule-based, không dùng AI)
         * - Insight 1: Merchant xuất hiện thường xuyên (>= 3 lần/tháng)
         * - Insight 2: Category chiếm tỷ lệ cao (> 30% tổng chi tiêu)
         * - Insight 3: Giao dịch lớn bất thường (> 2x trung bình)
         * 
         * @param username Tên đăng nhập của user
         * @return List<SpendingInsightDTO> chứa các insight chi tiêu
         */
        @Transactional(readOnly = true)
        @SuppressWarnings("null")
        public List<SpendingInsightDTO> getSpendingInsights(String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<SpendingInsightDTO> insights = new ArrayList<>();

                LocalDate now = LocalDate.now();
                LocalDate startOfMonth = now.withDayOfMonth(1);
                LocalDateTime monthStart = startOfMonth.atStartOfDay();
                LocalDateTime monthEnd = now.atTime(23, 59, 59);

                // Lấy tất cả giao dịch trong tháng hiện tại
                List<Transaction> transactions = transactionRepository
                                .findByUserIdAndTransactionDateBetween(user.getId(), monthStart, monthEnd);

                // Insight 1: Merchant chi tiêu thường xuyên (>= 3 lần)
                Map<String, Long> merchantFrequency = transactions.stream()
                                .filter(t -> t.getMerchant() != null && !t.getMerchant().trim().isEmpty())
                                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                                .collect(Collectors.groupingBy(Transaction::getMerchant, Collectors.counting()));

                merchantFrequency.entrySet().stream()
                                .filter(e -> e.getValue() >= 3) // Xuất hiện >= 3 lần
                                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                .limit(3)
                                .forEach(entry -> {
                                        SpendingInsightDTO insight = new SpendingInsightDTO();
                                        insight.setInsightType("FREQUENT_MERCHANT");
                                        insight.setTitle("Frequent Spending");
                                        insight.setMerchantName(entry.getKey());
                                        insight.setFrequency(entry.getValue().intValue());
                                        insight.setDescription(
                                                        "You've spent at " + entry.getKey() + " " + entry.getValue()
                                                                        + " times this month");
                                        insight.setSeverity("INFO");
                                        insight.setSuggestion("Consider setting a budget for frequent merchants");
                                        insights.add(insight);
                                });

                // Insight 2: Category chi tiêu cao (> 30% tổng chi tiêu)
                Map<String, BigDecimal> categorySpending = transactions.stream()
                                .filter(t -> t.getCategory() != null)
                                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                                .collect(Collectors.groupingBy(
                                                t -> t.getCategory().getName(),
                                                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount,
                                                                BigDecimal::add)));

                BigDecimal totalExpense = categorySpending.values().stream()
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                categorySpending.entrySet().stream()
                                .filter(e -> totalExpense.compareTo(BigDecimal.ZERO) > 0)
                                .filter(e -> {
                                        BigDecimal percentage = e.getValue()
                                                        .divide(totalExpense, 4, RoundingMode.HALF_UP)
                                                        .multiply(BigDecimal.valueOf(100));
                                        return percentage.compareTo(BigDecimal.valueOf(30)) > 0; // > 30% of total
                                })
                                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                                .limit(2)
                                .forEach(entry -> {
                                        BigDecimal percentage = entry.getValue()
                                                        .divide(totalExpense, 4, RoundingMode.HALF_UP)
                                                        .multiply(BigDecimal.valueOf(100));

                                        SpendingInsightDTO insight = new SpendingInsightDTO();
                                        insight.setInsightType("HIGH_SPENDING_CATEGORY");
                                        insight.setTitle("High Category Spending");
                                        insight.setCategoryName(entry.getKey());
                                        insight.setAmount(entry.getValue());
                                        insight.setDescription(entry.getKey() + " accounts for "
                                                        + percentage.setScale(1, RoundingMode.HALF_UP)
                                                        + "% of your spending");
                                        insight.setSeverity("WARNING");
                                        insight.setSuggestion("Consider reducing spending in this category");
                                        insights.add(insight);
                                });

                // Insight 3: Large transactions (> 2x average)
                BigDecimal avgExpense = transactions.stream()
                                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                                .map(Transaction::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                long expenseCount = transactions.stream()
                                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                                .count();

                if (expenseCount > 0) {
                        BigDecimal averageAmount = avgExpense.divide(BigDecimal.valueOf(expenseCount), 2,
                                        RoundingMode.HALF_UP);
                        BigDecimal threshold = averageAmount.multiply(BigDecimal.valueOf(2));

                        transactions.stream()
                                        .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                                        .filter(t -> t.getAmount().compareTo(threshold) > 0)
                                        .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                                        .limit(2)
                                        .forEach(t -> {
                                                SpendingInsightDTO insight = new SpendingInsightDTO();
                                                insight.setInsightType("UNUSUAL_TRANSACTION");
                                                insight.setTitle("Large Transaction Detected");
                                                insight.setAmount(t.getAmount());
                                                insight.setMerchantName(t.getMerchant());
                                                insight.setCategoryName(
                                                                t.getCategory() != null ? t.getCategory().getName()
                                                                                : "Uncategorized");
                                                insight.setDescription(
                                                                "Transaction of " + t.getAmount()
                                                                                + " VND is significantly above your average");
                                                insight.setSeverity("ALERT");
                                                insight.setSuggestion("Verify this transaction is correct");
                                                insights.add(insight);
                                        });
                }

                return insights;
        }

        /**
         * Lấy danh sách giao dịch bất thường (được AI đánh dấu isAnomaly = true)
         * 
         * @param username  Tên đăng nhập của user
         * @param startDate Ngày bắt đầu
         * @param endDate   Ngày kết thúc
         * @return List<Transaction> chứa các giao dịch bất thường
         */
        @Transactional(readOnly = true)
        @SuppressWarnings("null")
        public List<Transaction> getAnomalyTransactions(String username, LocalDate startDate, LocalDate endDate) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                LocalDateTime start = startDate.atStartOfDay();
                LocalDateTime end = endDate.atTime(23, 59, 59);

                return transactionRepository
                                .findByUserIdAndTransactionDateBetween(user.getId(), start, end)
                                .stream()
                                .filter(t -> t.getIsAnomaly() != null && t.getIsAnomaly())
                                .collect(Collectors.toList());
        }
}
