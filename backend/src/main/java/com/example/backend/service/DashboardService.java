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
 * Service quản lý Bảng điều khiển (Dashboard) - FR2: Module "Bảng điều khiển
 * Trực quan"
 * Chức năng: Hiển thị tổng quan dòng tiền, biểu đồ phân loại, xu hướng theo
 * tháng
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

        private final TransactionRepository transactionRepository;
        private final UserRepository userRepository;

        /**
         * FR2.1: Tổng quan Dòng tiền
         * Hiển thị: Tổng Thu nhập (Tháng) - Tổng Chi tiêu (Tháng) = Còn lại
         * 
         * @param username Tên đăng nhập của user
         * @param month    Tháng cần xem (nếu null thì lấy tháng hiện tại)
         * @return CashFlowDTO chứa thông tin dòng tiền (thu nhập, chi tiêu, tiết kiệm,
         *         tỷ lệ tiết kiệm, số dư, số giao dịch, ...)
         */
        @Transactional(readOnly = true)
        public CashFlowDTO getCashFlow(String username, LocalDate month) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Mặc định lấy tháng hiện tại nếu không chỉ định
                LocalDate targetMonth = (month != null) ? month : LocalDate.now();
                LocalDate startOfMonth = targetMonth.withDayOfMonth(1);
                LocalDate endOfMonth = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

                LocalDateTime start = startOfMonth.atStartOfDay();
                LocalDateTime end = endOfMonth.atTime(23, 59, 59);

                // Tính tổng thu nhập và chi tiêu trong tháng
                BigDecimal monthlyIncome = transactionRepository
                                .sumByUserIdAndTypeAndDateRange(user.getId(), Transaction.TransactionType.INCOME, start,
                                                end);
                BigDecimal monthlyExpense = transactionRepository
                                .sumByUserIdAndTypeAndDateRange(user.getId(), Transaction.TransactionType.EXPENSE,
                                                start, end);

                monthlyIncome = (monthlyIncome != null) ? monthlyIncome : BigDecimal.ZERO;
                monthlyExpense = (monthlyExpense != null) ? monthlyExpense : BigDecimal.ZERO;

                // Tiết kiệm ròng = Thu nhập - Chi tiêu
                BigDecimal netSavings = monthlyIncome.subtract(monthlyExpense);

                // Tính tỷ lệ tiết kiệm (savings rate %)
                BigDecimal savingsRate = BigDecimal.ZERO;
                if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
                        savingsRate = netSavings.divide(monthlyIncome, 4, RoundingMode.HALF_UP)
                                        .multiply(BigDecimal.valueOf(100));
                }

                // Tính tổng số dư = Tổng thu nhập - Tổng chi tiêu (all time)
                BigDecimal totalIncome = transactionRepository.sumByUserIdAndType(user.getId(),
                                Transaction.TransactionType.INCOME);
                BigDecimal totalExpense = transactionRepository.sumByUserIdAndType(user.getId(),
                                Transaction.TransactionType.EXPENSE);
                totalIncome = (totalIncome != null) ? totalIncome : BigDecimal.ZERO;
                totalExpense = (totalExpense != null) ? totalExpense : BigDecimal.ZERO;
                BigDecimal totalBalance = totalIncome.subtract(totalExpense);

                // So sánh với tháng trước
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

                // Đếm số giao dịch trong tháng
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
         * Hiển thị chi tiêu theo category (dữ liệu cho pie chart)
         * 
         * @param username  Tên đăng nhập của user
         * @param startDate Ngày bắt đầu (nếu null thì lấy ngày đầu tháng hiện tại)
         * @param endDate   Ngày kết thúc (nếu null thì lấy ngày hôm nay)
         * @return List<SpendingByCategoryDTO> chứa thông tin chi tiêu theo từng
         *         category (tên, icon, color, số tiền, %, số giao dịch)
         */
        @Transactional(readOnly = true)
        public List<SpendingByCategoryDTO> getSpendingByCategory(String username, LocalDate startDate,
                        LocalDate endDate) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Mặc định lấy tháng hiện tại nếu không chỉ định
                LocalDate start = (startDate != null) ? startDate : LocalDate.now().withDayOfMonth(1);
                LocalDate end = (endDate != null) ? endDate : LocalDate.now();

                LocalDateTime startDateTime = start.atStartOfDay();
                LocalDateTime endDateTime = end.atTime(23, 59, 59);

                // Lấy chi tiêu theo category (chỉ EXPENSE)
                List<Object[]> results = transactionRepository
                                .getSpendingByCategory(user.getId(), startDateTime, endDateTime,
                                                Transaction.TransactionType.EXPENSE);

                // Tính tổng để tính phần trăm
                BigDecimal total = results.stream()
                                .map(arr -> (BigDecimal) arr[4])
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Xây dựng DTOs và sắp xếp theo số tiền giảm dần
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
         * Tổng quan Dashboard - Tổng hợp tất cả thông tin dashboard cho màn hình chính
         * Bao gồm: Cash flow, top categories, số giao dịch (ngày/tuần/tháng)
         */
        @Transactional(readOnly = true)
        public DashboardSummaryDTO getDashboardSummary(String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                LocalDate now = LocalDate.now();
                LocalDate startOfMonth = now.withDayOfMonth(1);
                LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

                // Lấy thông tin cash flow
                CashFlowDTO cashFlow = getCashFlow(username, now);

                // Lấy top 5 categories chi tiêu nhiều nhất
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

                // Đếm số giao dịch hôm nay, giao dịch tuần
                Long todayCount = transactionRepository.countByUserIdAndDateRange(
                                user.getId(), now.atStartOfDay(), now.atTime(23, 59, 59));
                summary.setTodayTransactions((todayCount != null) ? todayCount.intValue() : 0);
                Long weekCount = transactionRepository.countByUserIdAndDateRange(
                                user.getId(), now.minusDays(7).atStartOfDay(), now.atTime(23, 59, 59));
                summary.setWeekTransactions((weekCount != null) ? weekCount.intValue() : 0);

                return summary;
        }

        /**
         * Xu hướng Thu chi theo tháng (Monthly Trend)
         * Hiển thị biểu đồ line/bar chart cho thu nhập và chi tiêu theo từng tháng
         * 
         * @param username Tên đăng nhập của user
         * @param months   Số tháng cần xem (mặc định 6 tháng)
         * @return List<MonthlyTrendDTO> chứa thông tin thu chi theo từng tháng
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
                                        .sumByUserIdAndTypeAndDateRange(user.getId(),
                                                        Transaction.TransactionType.INCOME,
                                                        startDateTime, endDateTime);
                        BigDecimal expense = transactionRepository
                                        .sumByUserIdAndTypeAndDateRange(user.getId(),
                                                        Transaction.TransactionType.EXPENSE,
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
         * Helper: Tính phần trăm thay đổi giữa 2 giá trị
         * 
         * @param oldValue Giá trị cũ (tháng trước)
         * @param newValue Giá trị mới (tháng hiện tại)
         * @return Phần trăm thay đổi (ví dụ: 15.5 nghĩa là tăng 15.5%)
         */
        private BigDecimal calculateChangePercent(BigDecimal oldValue, BigDecimal newValue) {
                if (oldValue == null || oldValue.compareTo(BigDecimal.ZERO) == 0) {
                        return BigDecimal.ZERO;
                }
                return newValue.subtract(oldValue)
                                .divide(oldValue, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
        }

        private final BudgetService budgetService;

        /**
         * Lấy danh sách ngân sách theo danh mục cho Dashboard
         * 
         * @param username Tên đăng nhập
         * @return List<CategoryBudgetDTO>
         */
        @Transactional(readOnly = true)
        public List<CategoryBudgetDTO> getCategoryBudgets(String username) {
                // Lấy các ngân sách đang hoạt động
                List<BudgetResponse> activeBudgets = budgetService.getActiveBudgets(username, LocalDate.now());

                return activeBudgets.stream()
                                .filter(b -> b.getCategoryId() != null) // Chỉ lấy ngân sách cho category cụ thể
                                .map(b -> CategoryBudgetDTO.builder()
                                                .categoryId(b.getCategoryId())
                                                .categoryName(b.getCategoryName())
                                                .budgetAmount(b.getAmount())
                                                .spentAmount(b.getSpentAmount())
                                                .percentage(b.getUsagePercentage())
                                                .startDate(b.getStartDate())
                                                .endDate(b.getEndDate())
                                                .status(b.getStatus())
                                                .build())
                                .collect(Collectors.toList());
        }
}
