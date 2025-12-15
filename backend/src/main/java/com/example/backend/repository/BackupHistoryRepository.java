package com.example.backend.repository;

import com.example.backend.model.BackupHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BackupHistoryRepository extends JpaRepository<BackupHistory, Long> {

    /**
     * Tìm backup history theo status
     */
    Page<BackupHistory> findByStatus(BackupHistory.BackupStatus status, Pageable pageable);

    /**
     * Tìm backup history theo type
     */
    Page<BackupHistory> findByBackupType(BackupHistory.BackupType backupType, Pageable pageable);

    /**
     * Tìm các backup thành công, sắp xếp theo ngày tạo mới nhất
     */
    List<BackupHistory> findByStatusOrderByCreatedAtDesc(BackupHistory.BackupStatus status);

    /**
     * Tìm các backup cũ hơn ngày cụ thể (để cleanup)
     */
    List<BackupHistory> findByCreatedAtBeforeAndStatus(
            LocalDateTime cutoffDate,
            BackupHistory.BackupStatus status
    );

    /**
     * Đếm số lượng backup thành công
     */
    long countByStatus(BackupHistory.BackupStatus status);

    /**
     * Lấy backup mới nhất
     */
    @Query("SELECT b FROM BackupHistory b WHERE b.status = 'COMPLETED' ORDER BY b.createdAt DESC LIMIT 1")
    BackupHistory findLatestSuccessfulBackup();
}
