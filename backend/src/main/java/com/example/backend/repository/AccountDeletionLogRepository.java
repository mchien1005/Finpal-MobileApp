package com.example.backend.repository;

import com.example.backend.model.AccountDeletionLog;
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
public interface AccountDeletionLogRepository extends JpaRepository<AccountDeletionLog, Long> {

    // Tìm theo ID người dùng bị xóa
    Optional<AccountDeletionLog> findByDeletedUserId(Long deletedUserId);

    // Tìm theo username
    Optional<AccountDeletionLog> findByUsername(String username);

    // Tìm theo email
    Optional<AccountDeletionLog> findByEmail(String email);

    // Lấy danh sách tất cả lịch sử xóa với phân trang
    Page<AccountDeletionLog> findAllByOrderByDeletedAtDesc(Pageable pageable);

    // Lấy danh sách theo admin đã duyệt
    Page<AccountDeletionLog> findByApprovedByAdminIdOrderByDeletedAtDesc(Long adminId, Pageable pageable);

    // Tìm trong khoảng thời gian
    @Query("SELECT adl FROM AccountDeletionLog adl " +
            "WHERE adl.deletedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY adl.deletedAt DESC")
    List<AccountDeletionLog> findByDeletedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Đếm số tài khoản đã xóa
    long count();

    // Đếm số tài khoản đã xóa theo admin
    long countByApprovedByAdminId(Long adminId);

    // Tìm kiếm theo username hoặc email
    @Query("SELECT adl FROM AccountDeletionLog adl " +
            "WHERE LOWER(adl.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(adl.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY adl.deletedAt DESC")
    Page<AccountDeletionLog> searchByUsernameOrEmail(
            @Param("keyword") String keyword,
            Pageable pageable);
}
