package com.example.backend.repository;

import com.example.backend.model.AdminActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminActivityLogRepository extends JpaRepository<AdminActivityLog, Long> {

    // Lấy lịch sử hoạt động của 1 admin, sắp xếp theo thời gian giảm dần
    List<AdminActivityLog> findByAdminUserIdOrderByTimestampDesc(Long adminUserId);

    // Lấy N hoạt động gần nhất của admin
    List<AdminActivityLog> findTop10ByAdminUserIdOrderByTimestampDesc(Long adminUserId);

    // Lấy với phân trang
    Page<AdminActivityLog> findByAdminUserIdOrderByTimestampDesc(Long adminUserId, Pageable pageable);

    // Đếm tổng số hoạt động của admin
    Long countByAdminUserId(Long adminUserId);

    // Lấy hoạt động theo loại đối tượng
    List<AdminActivityLog> findByAdminUserIdAndEntityTypeOrderByTimestampDesc(Long adminUserId, String entityType);

    // Lấy hoạt động trong khoảng thời gian
    @Query("SELECT a FROM AdminActivityLog a WHERE a.adminUser.id = :adminUserId AND a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<AdminActivityLog> findByAdminUserIdAndDateRange(
            @Param("adminUserId") Long adminUserId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Lấy tất cả hoạt động gần đây (cho audit log)
    List<AdminActivityLog> findTop100ByOrderByTimestampDesc();

    // Đếm hoạt động theo ngày
    @Query("SELECT DATE(a.timestamp), COUNT(a) FROM AdminActivityLog a WHERE a.adminUser.id = :adminUserId GROUP BY DATE(a.timestamp) ORDER BY DATE(a.timestamp) DESC")
    List<Object[]> countActivitiesByDate(@Param("adminUserId") Long adminUserId);
}
