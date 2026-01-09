package com.example.backend.service;

import com.example.backend.dto.AdminUserDetailResponse;
import com.example.backend.dto.AdminUserListResponse;
import com.example.backend.dto.PageResponse;
import com.example.backend.model.LoginHistory;
import com.example.backend.model.Transaction;
import com.example.backend.model.User;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý người dùng cho Admin
 * Chức năng: Xem danh sách, chi tiết, vô hiệu hóa, xóa, reset password
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AdminUserRepository adminUserRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminActivityService activityService;

    /**
     * Lấy danh sách người dùng với phân trang và tìm kiếm
     */
    @Transactional(readOnly = true)
    public PageResponse<AdminUserListResponse> getAllUsers(
            String keyword,
            Boolean isActive,
            String role,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        Sort sort = "DESC".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage;

        // Tìm kiếm nếu có keyword
        if (keyword != null && !keyword.isBlank()) {
            userPage = userRepository.searchUsers(keyword.toLowerCase(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<AdminUserListResponse> content = userPage.getContent().stream()
                .filter(u -> isActive == null || u.getIsActive().equals(isActive))
                .filter(u -> role == null || u.getRole().name().equalsIgnoreCase(role))
                .map(this::toListResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminUserListResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();
    }

    /**
     * Lấy chi tiết người dùng
     */
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(Long userId) {
        log.info("Bắt đầu lấy thông tin chi tiết user ID: {}", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

            // Lấy thống kê giao dịch
            log.debug("Đang lấy thống kê transaction...");
            BigDecimal totalIncome = transactionRepository.sumByUserIdAndType(userId,
                    Transaction.TransactionType.INCOME);
            BigDecimal totalExpense = transactionRepository.sumByUserIdAndType(userId,
                    Transaction.TransactionType.EXPENSE);

            if (totalIncome == null)
                totalIncome = BigDecimal.ZERO;
            if (totalExpense == null)
                totalExpense = BigDecimal.ZERO;

            // Đếm tổng số giao dịch
            long totalTransactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId).size();

            // Tính trung bình mỗi giao dịch
            BigDecimal averagePerTransaction = BigDecimal.ZERO;
            if (totalTransactions > 0) {
                BigDecimal totalAmount = totalIncome.add(totalExpense);
                averagePerTransaction = totalAmount.divide(BigDecimal.valueOf(totalTransactions), 0,
                        RoundingMode.HALF_UP);
            }

            // Lấy ngân hàng chính
            log.debug("Đang lấy primary bank...");
            String primaryBank = getPrimaryBank(userId);

            // Đếm ngân sách và mục tiêu
            log.debug("Đang đếm budget và savings goal...");
            long activeBudgets = budgetRepository.countByUserIdAndIsActiveTrue(userId);

            // Xử lý status enum cho SavingsGoalRepository
            long activeSavingsGoals = 0;
            try {
                // Thử truyền String
                activeSavingsGoals = savingsGoalRepository.countByUserIdAndStatus(userId, "ACTIVE");
            } catch (Exception e) {
                log.warn("Lỗi khi đếm savings goal (String): {}", e.getMessage());
                // Fallback nếu cần hoặc swallow error
            }

            // Lấy lịch sử đăng nhập
            log.debug("Đang lấy login history...");
            List<LoginHistory> loginHistories;
            try {
                loginHistories = loginHistoryRepository.findTop10ByUserIdOrderByLoginTimeDesc(userId);
            } catch (Exception e) {
                log.error("Lỗi khi lấy login history list: {}", e.getMessage());
                loginHistories = List.of();
            }

            long totalLogins = loginHistoryRepository.countByUserId(userId);
            long failedLogins = loginHistoryRepository.countByUserIdAndStatus(userId, LoginHistory.LoginStatus.FAILED);

            return AdminUserDetailResponse.builder()
                    .id(user.getId())
                    .userCode(String.format("USR%03d", user.getId()))
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .fullName(user.getFullName())
                    .avatarUrl(user.getAvatarUrl())
                    .isActive(user.getIsActive())
                    .role(user.getRole().name())
                    .createdAt(user.getCreatedAt())
                    .lastLoginAt(user.getLastLoginAt())
                    .lastActiveAt(user.getLastActiveAt())
                    .primaryBank(primaryBank)
                    .totalTransactions(totalTransactions)
                    .totalIncome(totalIncome)
                    .totalExpense(totalExpense)
                    .averagePerTransaction(averagePerTransaction)
                    .activeBudgets(activeBudgets)
                    .activeSavingsGoals(activeSavingsGoals)
                    .loginHistory(loginHistories.stream()
                            .map(AdminUserDetailResponse.LoginHistoryItem::fromEntity)
                            .collect(Collectors.toList()))
                    .totalLogins(totalLogins)
                    .failedLogins(failedLogins)
                    .build();
        } catch (Exception e) {
            log.error("CRITICAL ERROR in getUserDetail: ", e);
            throw e;
        }
    }

    /**
     * Vô hiệu hóa/Kích hoạt tài khoản
     */
    @Transactional
    public void toggleUserStatus(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Không cho phép vô hiệu hóa chính mình
        if (user.getUsername().equals(adminUsername)) {
            throw new RuntimeException("Không thể vô hiệu hóa tài khoản của chính bạn");
        }

        boolean newStatus = !user.getIsActive();
        user.setIsActive(newStatus);
        userRepository.save(user);

        // Ghi audit log
        String action = newStatus ? "ACTIVATE_USER" : "DEACTIVATE_USER";
        String description = String.format("%s tài khoản user '%s' (ID: %d)",
                newStatus ? "Kích hoạt" : "Vô hiệu hóa",
                user.getUsername(),
                user.getId());
        activityService.logActivity(adminUsername, action, "USER", user.getId(),
                description, null, null);

        log.info("Admin {} đã {} tài khoản {}",
                adminUsername,
                newStatus ? "kích hoạt" : "vô hiệu hóa",
                user.getUsername());
    }

    /**
     * Reset mật khẩu người dùng
     */
    @Transactional
    public String resetUserPassword(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Tạo mật khẩu mới ngẫu nhiên
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Ghi audit log
        String description = String.format("Reset mật khẩu cho user '%s' (ID: %d)",
                user.getUsername(),
                user.getId());
        activityService.logActivity(adminUsername, "RESET_PASSWORD", "USER", user.getId(),
                description, null, null);

        log.info("Admin {} đã reset mật khẩu cho tài khoản {}", adminUsername, user.getUsername());

        return newPassword;
    }

    /**
     * Xóa tài khoản người dùng (soft delete - chỉ vô hiệu hóa)
     */
    @Transactional
    public void deleteUser(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Không cho phép xóa chính mình
        if (user.getUsername().equals(adminUsername)) {
            throw new RuntimeException("Không thể xóa tài khoản của chính bạn");
        }

        // Nếu là Admin, kiểm tra thêm logic
        if (user.getRole().name().equals("ADMIN")) {
            // Tìm thông tin AdminUser
            var adminUserOptional = adminUserRepository.findByUserId(userId);
            if (adminUserOptional.isPresent()) {
                var adminUser = adminUserOptional.get();
                // Không cho phép xóa Super Admin
                if (adminUser.getAdminRole() != null && "SUPER_ADMIN".equals(adminUser.getAdminRole().getRoleCode())) {
                    throw new RuntimeException("Không thể xóa tài khoản Super Admin");
                }
                // Vô hiệu hóa AdminUser
                adminUser.setIsActive(false);
                adminUserRepository.save(adminUser);
            }
        }

        // Soft delete: vô hiệu hóa và đánh dấu
        String originalUsername = user.getUsername();
        String originalEmail = user.getEmail();
        user.setIsActive(false);
        // Rename để giải phóng unique constraint cho email/username (cho phép tạo lại
        // user cùng tên sau này nếu cần)
        user.setEmail(user.getEmail() + "_deleted_" + System.currentTimeMillis());
        user.setUsername(user.getUsername() + "_deleted_" + System.currentTimeMillis());
        userRepository.save(user);

        // Ghi audit log
        String description = String.format("Xóa user '%s' (Email: %s, ID: %d)",
                originalUsername,
                originalEmail,
                userId);
        activityService.logActivity(adminUsername, "DELETE_USER", "USER", userId,
                description, null, null);

        log.info("Admin {} đã xóa (soft delete) tài khoản {}", adminUsername, userId);
    }

    /**
     * Lấy ngân hàng chính của user (nguồn giao dịch phổ biến nhất)
     */
    private String getPrimaryBank(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
        if (transactions.isEmpty())
            return null;

        return transactions.stream()
                .filter(t -> t.getTransactionSource() != null)
                .collect(Collectors.groupingBy(Transaction::getTransactionSource, Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Tạo mật khẩu ngẫu nhiên
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Convert User to AdminUserListResponse
     */
    private AdminUserListResponse toListResponse(User user) {
        long totalTransactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId()).size();

        return AdminUserListResponse.builder()
                .id(user.getId())
                .userCode(String.format("USR%03d", user.getId()))
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .totalTransactions(totalTransactions)
                .build();
    }
}
