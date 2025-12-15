package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity lưu lịch sử sao lưu database
 */
@Entity
@Table(name = "lich_su_sao_luu")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_file", nullable = false)
    private String fileName;

    @Column(name = "duong_dan_file", nullable = false)
    private String filePath;

    @Column(name = "kich_thuoc_file") // bytes
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_sao_luu", nullable = false)
    private BackupType backupType;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private BackupStatus status = BackupStatus.IN_PROGRESS;

    @Column(name = "thong_bao_loi", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "hoan_thanh_luc")
    private LocalDateTime completedAt;

    @Column(name = "id_nguoi_tao")
    private Long createdBy; // Admin user ID

    @Column(name = "ghi_chu")
    private String note;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum BackupType {
        FULL,      // Backup toàn bộ database
        MANUAL,    // Backup thủ công bởi admin
        SCHEDULED  // Backup tự động theo lịch
    }

    public enum BackupStatus {
        IN_PROGRESS,  // Đang sao lưu
        COMPLETED,    // Hoàn thành
        FAILED        // Thất bại
    }
}
