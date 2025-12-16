package com.example.backend.repository;

import com.example.backend.model.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    // Lấy lịch sử đăng nhập của user, sắp xếp theo thời gian giảm dần
    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId);

    // Lấy lịch sử đăng nhập với phân trang
    Page<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId, Pageable pageable);

    // Lấy N lịch sử đăng nhập gần nhất của user
    List<LoginHistory> findTop10ByUserIdOrderByLoginTimeDesc(Long userId);

    // Đếm số lần đăng nhập thất bại gần đây (trong 1 giờ)
    @Query("SELECT COUNT(l) FROM LoginHistory l WHERE l.user.id = :userId AND l.status = 'FAILED' AND l.loginTime >= :since")
    Long countRecentFailedLogins(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // Lấy lần đăng nhập thành công gần nhất
    @Query("SELECT l FROM LoginHistory l WHERE l.user.id = :userId AND l.status = 'SUCCESS' ORDER BY l.loginTime DESC LIMIT 1")
    LoginHistory findLastSuccessfulLogin(@Param("userId") Long userId);

    // Đếm tổng số lần đăng nhập của user
    Long countByUserId(Long userId);

    // Đếm số lần đăng nhập theo trạng thái
    Long countByUserIdAndStatus(Long userId, LoginHistory.LoginStatus status);
}
