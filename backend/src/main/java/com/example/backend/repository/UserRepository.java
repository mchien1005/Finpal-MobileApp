package com.example.backend.repository;

import com.example.backend.model.Role;
import com.example.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    // Tìm tất cả users theo role
    List<User> findByRole(Role role);

    // Tìm users theo role và trạng thái hoạt động
    List<User> findByRoleAndIsActive(Role role, Boolean isActive);

    // Tìm kiếm users theo keyword (username, email, fullName, phone)
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE %:keyword% OR " +
            "LOWER(u.email) LIKE %:keyword% OR " +
            "LOWER(u.fullName) LIKE %:keyword% OR " +
            "u.phone LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    // Đếm số users theo trạng thái
    Long countByIsActive(Boolean isActive);

    // Đếm số users theo role
    Long countByRole(Role role);

    // ============================================================================
    // ADMIN DASHBOARD QUERIES
    // ============================================================================

    /**
     * Đếm users được tạo trong khoảng thời gian
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Long countByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm users hoạt động trong khoảng thời gian (dựa trên lastActiveAt)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastActiveAt BETWEEN :startDate AND :endDate")
    Long countActiveUsersByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Thống kê users theo tháng (cho biểu đồ tăng trưởng)
     * Trả về: [month (1-12), year, totalUsersCreated]
     */
    @Query(value = "SELECT MONTH(ngay_tao) as month, YEAR(ngay_tao) as year, COUNT(*) " +
            "FROM nguoi_dung " +
            "WHERE ngay_tao BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(ngay_tao), MONTH(ngay_tao) " +
            "ORDER BY YEAR(ngay_tao), MONTH(ngay_tao)", nativeQuery = true)
    List<Object[]> getUserCreationStatsByMonth(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm tổng users tính đến một thời điểm
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt <= :date AND u.role = 'USER'")
    Long countTotalUsersUntilDate(@Param("date") LocalDateTime date);
}
