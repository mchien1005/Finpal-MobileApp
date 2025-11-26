package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "thong_bao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_nguoi_dung", nullable = false)
    private Long userId;

    @Column(name = "loai", nullable = false, length = 50)
    private String type;

    @Column(name = "tieu_de", nullable = false)
    private String title;

    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String content;

    @Column(name = "duong_dan_hanh_dong", length = 500)
    private String actionUrl;

    @Column(name = "da_doc")
    private Boolean isRead = false;

    @Column(name = "thoi_gian_doc")
    private LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "do_uu_tien", nullable = false)
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum NotificationPriority {
        LOW, MEDIUM, HIGH, URGENT
    }
}
