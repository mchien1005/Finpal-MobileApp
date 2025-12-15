package com.example.backend.repository;

import com.example.backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    Long countByUserIdAndIsReadFalse(Long userId);

    Long countByUserIdAndIsRead(Long userId, Boolean isRead);

    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);

    boolean existsByUserIdAndTypeAndActionUrl(Long userId, String type, String actionUrl);

    int deleteByIsReadAndCreatedAtBefore(Boolean isRead, LocalDateTime createdAt);

    // Tìm notifications theo user, type và thời gian tạo (để tránh gửi trùng lặp)
    List<Notification> findByUserIdAndTypeAndCreatedAtAfter(Long userId, String type, LocalDateTime createdAt);
}
