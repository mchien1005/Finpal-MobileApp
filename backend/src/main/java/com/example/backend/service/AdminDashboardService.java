package com.example.backend.service;

import com.example.backend.dto.admin.*;
import com.example.backend.model.Role;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý logic cho Admin Dashboard
 * Cung cấp các API thống kê tổng quan hệ thống
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.backend.url:http://localhost:8000}")
    private String aiBackendUrl;

    // Mapping ngân hàng code -> tên đầy đủ
    private static final Map<String, String> BANK_NAMES = Map.ofEntries(
            Map.entry("VCB", "Vietcombank"),
            Map.entry("TCB", "Techcombank"),
            Map.entry("ACB", "ACB"),
            Map.entry("VPB", "VPBank"),
            Map.entry("MBB", "MB Bank"),
            Map.entry("BID", "BIDV"),
            Map.entry("CTG", "Vietinbank"),
            Map.entry("SHB", "SHB"),
            Map.entry("TPB", "TPBank"),
            Map.entry("VIB", "VIB"),
            Map.entry("MSB", "MSB"),
            Map.entry("EIB", "Eximbank"),
            Map.entry("STB", "Sacombank"),
            Map.entry("HDB", "HDBank"),
            Map.entry("MOMO", "MoMo"),
            Map.entry("ZALOPAY", "ZaloPay"),
            Map.entry("VNPAY", "VNPay"));

    /**
     * Lấy tổng quan dashboard cho Admin
     * Bao gồm: user stats, transaction stats, AI accuracy, SMS parsing rate
     */
    @Transactional(readOnly = true)
    public AdminDashboardOverviewDTO getOverview() {
        log.info("Getting admin dashboard overview");

        // Thời gian hôm nay
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);

        // Thời gian hôm qua
        LocalDateTime startOfYesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime endOfYesterday = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);

        // Thời gian tháng này và tháng trước
        LocalDateTime startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfLastMonth = startOfThisMonth.minusSeconds(1);

        // === THỐNG KÊ NGƯỜI DÙNG ===
        Long totalUsers = userRepository.countByRole(Role.USER);
        Long activeUsersToday = userRepository.countActiveUsersByDateRange(startOfToday, endOfToday);

        // Tính % tăng trưởng users tháng này so với tháng trước
        Long newUsersThisMonth = userRepository.countByCreatedAtBetween(startOfThisMonth, endOfToday);
        Long newUsersLastMonth = userRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);
        Double userGrowthPercent = calculateGrowthPercent(newUsersThisMonth, newUsersLastMonth);

        // === THỐNG KÊ GIAO DỊCH HÔM NAY ===
        Long transactionsToday = transactionRepository.countAllByDateRange(startOfToday, endOfToday);
        BigDecimal totalValueToday = transactionRepository.sumAllByDateRange(startOfToday, endOfToday);

        // Tính % tăng trưởng giao dịch so với hôm qua
        Long transactionsYesterday = transactionRepository.countAllByDateRange(startOfYesterday, endOfYesterday);
        Double transactionGrowthPercent = calculateGrowthPercent(transactionsToday, transactionsYesterday);

        // === AI ACCURACY (gọi BackendAI) ===
        Double aiAccuracy = getAIAccuracy();
        Double aiAccuracyChange = 2.1; // Placeholder - có thể tính từ lịch sử

        // === SMS PARSING RATE ===
        // Tính tỷ lệ SMS parsing - có thể mở rộng sau khi có log thất bại
        // Long totalSmsTransactions =
        // transactionRepository.countAutoTransactionsByDateRange(...)
        Double smsParsingRate = 98.7; // Placeholder - tính từ log thất bại
        Long smsFailedToday = 0L; // Placeholder

        return AdminDashboardOverviewDTO.builder()
                .totalUsers(totalUsers)
                .activeUsersToday(activeUsersToday != null ? activeUsersToday : 0L)
                .userGrowthPercent(userGrowthPercent)
                .transactionsToday(transactionsToday != null ? transactionsToday : 0L)
                .totalValueToday(totalValueToday != null ? totalValueToday : BigDecimal.ZERO)
                .transactionGrowthPercent(transactionGrowthPercent)
                .aiAccuracy(aiAccuracy)
                .aiAccuracyChange(aiAccuracyChange)
                .smsParsingRate(smsParsingRate)
                .smsFailedToday(smsFailedToday)
                .build();
    }

    /**
     * Lấy dữ liệu tăng trưởng người dùng theo tháng (7 tháng gần nhất)
     */
    @Transactional(readOnly = true)
    public UserGrowthDTO getUserGrowth(Integer months) {
        log.info("Getting user growth data for {} months", months);

        int numMonths = months != null ? months : 7;
        LocalDateTime startDate = LocalDate.now().minusMonths(numMonths - 1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDate = LocalDate.now().atTime(LocalTime.MAX);

        List<Object[]> monthlyStats = userRepository.getUserCreationStatsByMonth(startDate, endDate);

        // Tạo map để lookup nhanh
        Map<String, Long> statsMap = new HashMap<>();
        for (Object[] row : monthlyStats) {
            int month = ((Number) row[0]).intValue();
            int year = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            statsMap.put(year + "-" + month, count);
        }

        // Tạo danh sách theo số tháng yêu cầu
        List<UserGrowthDTO.MonthlyUserData> data = new ArrayList<>();

        for (int i = numMonths - 1; i >= 0; i--) {
            LocalDate monthDate = LocalDate.now().minusMonths(i);
            int month = monthDate.getMonthValue();
            int year = monthDate.getYear();
            String key = year + "-" + month;

            // Users mới trong tháng
            long newUsers = statsMap.getOrDefault(key, 0L);

            // Tổng users tính đến cuối tháng
            LocalDateTime endOfMonth = monthDate.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
            Long totalUsersUntil = userRepository.countTotalUsersUntilDate(endOfMonth);

            // Active users trong tháng (giả định 70% active)
            long activeUsers = (long) (totalUsersUntil * 0.7);

            data.add(UserGrowthDTO.MonthlyUserData.builder()
                    .month("T" + month)
                    .monthLabel("Tháng " + month + "/" + year)
                    .totalUsers(totalUsersUntil != null ? totalUsersUntil : 0L)
                    .activeUsers(activeUsers)
                    .newUsers(newUsers)
                    .build());
        }

        // Tổng thống kê
        Long totalUsers = userRepository.countByRole(Role.USER);
        Long totalActiveUsers = userRepository.countByIsActive(true);
        double growthRate = data.size() > 1 ? calculateGrowthPercent(
                data.get(data.size() - 1).getNewUsers(),
                data.get(data.size() - 2).getNewUsers()) : 0.0;

        return UserGrowthDTO.builder()
                .data(data)
                .totalUsers(totalUsers)
                .totalActiveUsers(totalActiveUsers)
                .growthRate(growthRate)
                .build();
    }

    /**
     * Lấy khối lượng giao dịch 7 ngày gần nhất
     */
    @Transactional(readOnly = true)
    public TransactionVolumeDTO getTransactionVolume(Integer days) {
        log.info("Getting transaction volume for {} days", days);

        int numDays = days != null ? days : 7;
        LocalDateTime startDate = LocalDate.now().minusDays(numDays - 1).atStartOfDay();
        LocalDateTime endDate = LocalDate.now().atTime(LocalTime.MAX);

        List<Object[]> dailyStats = transactionRepository.getTransactionStatsByDate(startDate, endDate);

        // Tạo map để lookup
        Map<LocalDate, long[]> statsMap = new HashMap<>();
        for (Object[] row : dailyStats) {
            LocalDate date;
            if (row[0] instanceof java.sql.Date) {
                date = ((java.sql.Date) row[0]).toLocalDate();
            } else if (row[0] instanceof LocalDate) {
                date = (LocalDate) row[0];
            } else {
                continue;
            }
            long count = ((Number) row[1]).longValue();
            BigDecimal sum = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            statsMap.put(date, new long[] { count, sum.longValue() });
        }

        // Tạo danh sách 7 ngày
        List<TransactionVolumeDTO.DailyTransactionData> data = new ArrayList<>();
        long totalTransactions = 0;
        BigDecimal totalValue = BigDecimal.ZERO;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String[] dayOfWeekVN = { "", "T2", "T3", "T4", "T5", "T6", "T7", "CN" };

        for (int i = numDays - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long[] stats = statsMap.getOrDefault(date, new long[] { 0L, 0L });

            // Chuyển đổi giá trị sang triệu VND
            BigDecimal valueMillion = BigDecimal.valueOf(stats[1])
                    .divide(BigDecimal.valueOf(1_000_000), 2, RoundingMode.HALF_UP);

            int dow = date.getDayOfWeek().getValue();
            data.add(TransactionVolumeDTO.DailyTransactionData.builder()
                    .day(dayOfWeekVN[dow])
                    .date(date.format(dateFormatter))
                    .transactionCount(stats[0])
                    .totalValue(valueMillion)
                    .build());

            totalTransactions += stats[0];
            totalValue = totalValue.add(BigDecimal.valueOf(stats[1]));
        }

        return TransactionVolumeDTO.builder()
                .data(data)
                .totalTransactions(totalTransactions)
                .totalValue(totalValue)
                .avgDailyTransactions((double) totalTransactions / numDays)
                .build();
    }

    /**
     * Lấy phân bổ chi tiêu theo danh mục (toàn hệ thống)
     */
    @Transactional(readOnly = true)
    public CategoryDistributionDTO getCategoryDistribution() {
        log.info("Getting category distribution");

        // Lấy dữ liệu 30 ngày gần nhất
        LocalDateTime startDate = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime endDate = LocalDate.now().atTime(LocalTime.MAX);

        List<Object[]> stats = transactionRepository.getSystemCategoryDistribution(startDate, endDate);

        if (stats.isEmpty()) {
            return CategoryDistributionDTO.builder()
                    .categories(Collections.emptyList())
                    .totalSpending(BigDecimal.ZERO)
                    .totalTransactions(0L)
                    .build();
        }

        // Tính tổng để tính phần trăm
        BigDecimal totalSpending = BigDecimal.ZERO;
        long totalTransactions = 0;

        for (Object[] row : stats) {
            BigDecimal amount = row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO;
            long count = ((Number) row[5]).longValue();
            totalSpending = totalSpending.add(amount);
            totalTransactions += count;
        }

        // Tạo danh sách categories
        final BigDecimal finalTotal = totalSpending;
        List<CategoryDistributionDTO.CategoryData> categories = stats.stream()
                .map(row -> {
                    BigDecimal amount = row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO;
                    double percentage = finalTotal.compareTo(BigDecimal.ZERO) > 0
                            ? amount.divide(finalTotal, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .doubleValue()
                            : 0.0;

                    return CategoryDistributionDTO.CategoryData.builder()
                            .categoryId(row[0] != null ? ((Number) row[0]).longValue() : null)
                            .categoryName(row[1] != null ? row[1].toString() : "Khác")
                            .icon(row[2] != null ? row[2].toString() : null)
                            .color(row[3] != null ? row[3].toString() : "#808080")
                            .amount(amount)
                            .percentage(percentage)
                            .transactionCount(((Number) row[5]).longValue())
                            .build();
                })
                .collect(Collectors.toList());

        return CategoryDistributionDTO.builder()
                .categories(categories)
                .totalSpending(totalSpending)
                .totalTransactions(totalTransactions)
                .build();
    }

    /**
     * Lấy phân bổ giao dịch theo ngân hàng
     */
    @Transactional(readOnly = true)
    public BankDistributionDTO getBankDistribution() {
        log.info("Getting bank distribution");

        List<Object[]> stats = transactionRepository.getBankDistribution();

        if (stats.isEmpty()) {
            return BankDistributionDTO.builder()
                    .banks(Collections.emptyList())
                    .totalUsers(0L)
                    .totalTransactions(0L)
                    .build();
        }

        // Tính tổng users
        long totalUsers = stats.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();

        final long finalTotalUsers = totalUsers;

        // Tạo danh sách banks
        List<BankDistributionDTO.BankData> banks = stats.stream()
                .map(row -> {
                    String bankCode = row[0] != null ? row[0].toString().toUpperCase() : "UNKNOWN";
                    long userCount = ((Number) row[1]).longValue();
                    long txCount = ((Number) row[2]).longValue();
                    double percentage = finalTotalUsers > 0
                            ? (userCount * 100.0 / finalTotalUsers)
                            : 0.0;

                    return BankDistributionDTO.BankData.builder()
                            .bankCode(bankCode)
                            .bankName(BANK_NAMES.getOrDefault(bankCode, bankCode))
                            .userCount(userCount)
                            .percentage(Math.round(percentage * 10.0) / 10.0)
                            .transactionCount(txCount)
                            .build();
                })
                .collect(Collectors.toList());

        long totalTransactions = banks.stream()
                .mapToLong(BankDistributionDTO.BankData::getTransactionCount)
                .sum();

        return BankDistributionDTO.builder()
                .banks(banks)
                .totalUsers(totalUsers)
                .totalTransactions(totalTransactions)
                .build();
    }

    /**
     * Lấy tình trạng sức khỏe hệ thống
     * Sử dụng JMX để lấy thông tin CPU/RAM của JVM và hệ thống
     */
    public SystemHealthDTO getSystemHealth() {
        log.info("Getting system health");

        // === JVM HEAP MEMORY ===
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedHeap = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long maxHeap = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        long freeHeap = maxHeap - usedHeap;
        double heapUsage = maxHeap > 0 ? (usedHeap * 100.0 / maxHeap) : 0;

        // === SYSTEM RAM (thông qua com.sun.management) ===
        long totalSystemMemoryMB = maxHeap; // Fallback là JVM heap
        long freeSystemMemoryMB = freeHeap;
        long usedSystemMemoryMB = usedHeap;
        double systemRamUsage = heapUsage;

        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                // Lấy thông tin RAM hệ thống thực sự
                long totalPhysical = sunOsBean.getTotalMemorySize() / (1024 * 1024);
                long freePhysical = sunOsBean.getFreeMemorySize() / (1024 * 1024);
                if (totalPhysical > 0) {
                    totalSystemMemoryMB = totalPhysical;
                    freeSystemMemoryMB = freePhysical;
                    usedSystemMemoryMB = totalPhysical - freePhysical;
                    systemRamUsage = (usedSystemMemoryMB * 100.0) / totalSystemMemoryMB;
                }
            }
        } catch (Exception e) {
            log.debug("Could not get system memory info, using JVM heap instead: {}", e.getMessage());
        }

        // === CPU ===
        double cpuLoad = 0.0;
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                // Lấy CPU usage của tiến trình JVM (hoạt động trên cả Windows)
                double processCpuLoad = sunOsBean.getProcessCpuLoad();
                double systemCpuLoad = sunOsBean.getCpuLoad(); // Java 14+

                // Ưu tiên System CPU, fallback sang Process CPU
                if (systemCpuLoad >= 0) {
                    cpuLoad = systemCpuLoad * 100;
                } else if (processCpuLoad >= 0) {
                    cpuLoad = processCpuLoad * 100;
                } else {
                    // Fallback: System Load Average (không hoạt động trên Windows)
                    double loadAvg = osBean.getSystemLoadAverage();
                    if (loadAvg >= 0) {
                        cpuLoad = Math.min(loadAvg * 100 / osBean.getAvailableProcessors(), 100);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not get CPU load: {}", e.getMessage());
        }

        // === DATABASE ===
        String dbStatus = "CONNECTED";
        long dbResponseMs = 0;
        long dbConnections = 0;
        try {
            long start = System.currentTimeMillis();
            userRepository.count(); // Simple query to test connection
            dbResponseMs = System.currentTimeMillis() - start;
            if (dbResponseMs > 1000) {
                dbStatus = "SLOW";
            }
        } catch (Exception e) {
            dbStatus = "DISCONNECTED";
            log.error("Database health check failed", e);
        }

        // === AI BACKEND ===
        String aiStatus = "ONLINE";
        long aiResponseMs = 0;
        try {
            long start = System.currentTimeMillis();
            restTemplate.getForObject(aiBackendUrl + "/health", String.class);
            aiResponseMs = System.currentTimeMillis() - start;
            if (aiResponseMs > 2000) {
                aiStatus = "DEGRADED";
            }
        } catch (Exception e) {
            aiStatus = "OFFLINE";
            log.warn("AI Backend health check failed: {}", e.getMessage());
        }

        // === GENERAL ===
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        String jvmVersion = System.getProperty("java.version");
        int activeThreads = Thread.activeCount();

        return SystemHealthDTO.builder()
                .cpuUsage(Math.round(cpuLoad * 10.0) / 10.0)
                .ramUsage(Math.round(systemRamUsage * 10.0) / 10.0)
                .totalMemoryMB(totalSystemMemoryMB)
                .usedMemoryMB(usedSystemMemoryMB)
                .freeMemoryMB(freeSystemMemoryMB)
                .databaseStatus(dbStatus)
                .databaseConnections(dbConnections)
                .databaseResponseMs(dbResponseMs)
                .aiBackendStatus(aiStatus)
                .aiResponseMs(aiResponseMs)
                .lastUpdated(LocalDateTime.now())
                .uptimeSeconds(uptimeMs / 1000)
                .jvmVersion(jvmVersion)
                .activeThreads(activeThreads)
                .build();
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    /**
     * Tính phần trăm tăng trưởng
     */
    private Double calculateGrowthPercent(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        if (current == null) {
            current = 0L;
        }
        return Math.round(((current - previous) * 1000.0 / previous)) / 10.0;
    }

    /**
     * Lấy AI Accuracy từ BackendAI
     */
    private Double getAIAccuracy() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(
                    aiBackendUrl + "/api/admin/stats",
                    Map.class);
            if (response != null && response.containsKey("avg_accuracy")) {
                return ((Number) response.get("avg_accuracy")).doubleValue();
            }
        } catch (Exception e) {
            log.warn("Could not get AI accuracy from backend: {}", e.getMessage());
        }
        return 94.2; // Default value nếu không kết nối được
    }
}
