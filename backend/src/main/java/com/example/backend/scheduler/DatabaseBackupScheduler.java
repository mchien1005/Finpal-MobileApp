package com.example.backend.scheduler;

import com.example.backend.service.DatabaseBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks cho database backup
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "backup.scheduled.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class DatabaseBackupScheduler {

    private final DatabaseBackupService backupService;

    /**
     * Tự động backup database hàng ngày lúc 2:00 AM
     */
    @Scheduled(cron = "${backup.scheduled.cron:0 0 2 * * *}")
    public void scheduledDatabaseBackup() {
        log.info("=== Bắt đầu scheduled database backup ===");
        
        try {
            backupService.createScheduledBackup();
            log.info("Scheduled backup completed successfully");
        } catch (Exception e) {
            log.error("Lỗi khi thực hiện scheduled backup: ", e);
        }
    }

    /**
     * Tự động cleanup backup cũ - chạy hàng tuần vào Chủ nhật lúc 3:00 AM
     */
    @Scheduled(cron = "${backup.cleanup.cron:0 0 3 * * SUN}")
    public void cleanupOldBackups() {
        log.info("=== Bắt đầu cleanup old backups ===");
        
        try {
            backupService.cleanupOldBackups();
            log.info("Cleanup old backups completed successfully");
        } catch (Exception e) {
            log.error("Lỗi khi cleanup old backups: ", e);
        }
    }
}
