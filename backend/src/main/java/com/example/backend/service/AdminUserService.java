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
    private final LoginHistoryRepository loginHistoryRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final PasswordEncoder passwordEncoder;

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // Lấy thống kê giao dịch
        BigDecimal totalIncome = transactionRepository.sumByUserIdAndType(userId, Transaction.TransactionType.INCOME);
        BigDecimal totalExpense = transactionRepository.sumByUserIdAndType(userId, Transaction.TransactionType.EXPENSE);
        
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        // Đếm tổng số giao dịch
        long totalTransactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId).size();

        // Tính trung bình mỗi giao dịch
        BigDecimal averagePerTransaction = BigDecimal.ZERO;
        if (totalTransactions > 0) {
            BigDecimal totalAmount = totalIncome.add(totalExpense);
            averagePerTransaction = totalAmount.divide(BigDecimal.valueOf(totalTransactions), 0, RoundingMode.HALF_UP);
        }

        // Lấy ngân hàng chính (nguồn giao dịch phổ biến nhất)
        String primaryBank = getPrimaryBank(userId);

        // Đếm ngân sách và mục tiêu
        long activeBudgets = budgetRepository.countByUserIdAndIsActiveTrue(userId);
        long activeSavingsGoals = savingsGoalRepository.countByUserIdAndStatus(userId, "ACTIVE");

        // Lấy lịch sử đăng nhập
        List<LoginHistory> loginHistories = loginHistoryRepository.findTop10ByUserIdOrderByLoginTimeDesc(userId);
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

        user.setIsActive(!user.getIsActive());
        userRepository.save(user);

        log.info("Admin {} đã {} tài khoản {}", 
                adminUsername, 
                user.getIsActive() ? "kích hoạt" : "vô hiệu hóa", 
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

        // Không cho phép xóa tài khoản admin
        if (user.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Không thể xóa tài khoản Admin");
        }

        // Không cho phép xóa chính mình
        if (user.getUsername().equals(adminUsername)) {
            throw new RuntimeException("Không thể xóa tài khoản của chính bạn");
        }

        // Soft delete: vô hiệu hóa và đánh dấu
        user.setIsActive(false);
        user.setEmail(user.getEmail() + "_deleted_" + System.currentTimeMillis());
        user.setUsername(user.getUsername() + "_deleted_" + System.currentTimeMillis());
        userRepository.save(user);

        log.info("Admin {} đã xóa (soft delete) tài khoản {}", adminUsername, userId);
    }

    /**
     * Lấy ngân hàng chính của user (nguồn giao dịch phổ biến nhất)
     */
    private String getPrimaryBank(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
        if (transactions.isEmpty()) return null;

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
