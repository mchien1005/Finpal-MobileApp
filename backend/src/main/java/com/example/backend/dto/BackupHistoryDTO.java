package com.example.backend.dto;

import com.example.backend.model.BackupHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupHistoryDTO {

    private Long id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileSizeFormatted; // "2.5 MB"
    private String backupType;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Long createdBy;
    private String createdByUsername;
    private String note;
    private Long durationSeconds; // Thời gian backup (giây)

    public static BackupHistoryDTO fromEntity(BackupHistory backup) {
        BackupHistoryDTO dto = new BackupHistoryDTO();
        dto.setId(backup.getId());
        dto.setFileName(backup.getFileName());
        dto.setFilePath(backup.getFilePath());
        dto.setFileSize(backup.getFileSize());
        dto.setFileSizeFormatted(formatFileSize(backup.getFileSize()));
        dto.setBackupType(backup.getBackupType() != null ? backup.getBackupType().name() : null);
        dto.setStatus(backup.getStatus() != null ? backup.getStatus().name() : null);
        dto.setErrorMessage(backup.getErrorMessage());
        dto.setCreatedAt(backup.getCreatedAt());
        dto.setCompletedAt(backup.getCompletedAt());
        dto.setCreatedBy(backup.getCreatedBy());
        dto.setNote(backup.getNote());

        // Tính duration
        if (backup.getCreatedAt() != null && backup.getCompletedAt() != null) {
            dto.setDurationSeconds(
                java.time.Duration.between(backup.getCreatedAt(), backup.getCompletedAt()).getSeconds()
            );
        }

        return dto;
    }

    private static String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
