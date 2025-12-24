package com.example.backend.repository;

import com.example.backend.model.NotificationSettings;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho cài đặt thông báo của người dùng
 */
@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {

    /**
     * Tìm cài đặt thông báo theo user
     */
    Optional<NotificationSettings> findByUser(User user);

    /**
     * Tìm cài đặt thông báo theo userId
     */
    Optional<NotificationSettings> findByUserId(Long userId);

    /**
     * Kiểm tra user đã có cài đặt chưa
     */
    boolean existsByUserId(Long userId);
}
