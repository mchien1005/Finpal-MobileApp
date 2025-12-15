package com.example.backend.service;

import com.example.backend.dto.BackupHistoryDTO;
import com.example.backend.dto.BackupStatisticsDTO;
import com.example.backend.model.BackupHistory;
import com.example.backend.model.User;
import com.example.backend.repository.BackupHistoryRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý sao lưu và khôi phục database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseBackupService {

    private final BackupHistoryRepository backupHistoryRepository;
    private final UserRepository userRepository;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${backup.directory:/tmp/finpal-backups}")
    private String backupDirectory;

    @Value("${backup.retention.days:30}")
    private int retentionDays;

    @Value("${backup.max.count:50}")
    private int maxBackupCount;

    /**
     * Tạo backup database (manual)
     */
    @Async
    @Transactional
    public void createBackup(String username, String note) {
        log.info("Bắt đầu backup database được yêu cầu bởi: {}", username);

        // Tìm user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Tạo backup history record
        BackupHistory backup = new BackupHistory();
        backup.setBackupType(BackupHistory.BackupType.MANUAL);
        backup.setStatus(BackupHistory.BackupStatus.IN_PROGRESS);
        backup.setCreatedBy(user.getId());
        backup.setNote(note);

        try {
            // Extract database name từ URL
            String dbName = extractDatabaseName(databaseUrl);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("finpal_backup_%s.sql", timestamp);
            
            // Tạo backup directory nếu chưa có
            Path backupDirPath = Paths.get(backupDirectory);
            if (!Files.exists(backupDirPath)) {
                Files.createDirectories(backupDirPath);
            }

            String filePath = Paths.get(backupDirectory, fileName).toString();
            backup.setFileName(fileName);
            backup.setFilePath(filePath);

            // Save initial record
            backup = backupHistoryRepository.save(backup);

            // Thực hiện backup bằng mysqldump
            boolean success = executeMysqlDump(dbName, filePath);

            if (success) {
                // Lấy file size
                File backupFile = new File(filePath);
                if (backupFile.exists()) {
                    backup.setFileSize(backupFile.length());
                }

                backup.setStatus(BackupHistory.BackupStatus.COMPLETED);
                backup.setCompletedAt(LocalDateTime.now());
                log.info("Backup thành công: {}", fileName);
            } else {
                backup.setStatus(BackupHistory.BackupStatus.FAILED);
                backup.setErrorMessage("mysqldump command failed");
                log.error("Backup thất bại: {}", fileName);
            }

        } catch (Exception e) {
            backup.setStatus(BackupHistory.BackupStatus.FAILED);
            backup.setErrorMessage(e.getMessage());
            log.error("Lỗi khi backup database: ", e);
        } finally {
            backupHistoryRepository.save(backup);
        }
    }

    /**
     * Tạo backup tự động (scheduled)
     */
    @Async
    @Transactional
    public void createScheduledBackup() {
        log.info("Bắt đầu scheduled backup database");

        BackupHistory backup = new BackupHistory();
        backup.setBackupType(BackupHistory.BackupType.SCHEDULED);
        backup.setStatus(BackupHistory.BackupStatus.IN_PROGRESS);
        backup.setNote("Backup tự động theo lịch");

        try {
            String dbName = extractDatabaseName(databaseUrl);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("finpal_scheduled_%s.sql", timestamp);
            
            Path backupDirPath = Paths.get(backupDirectory);
            if (!Files.exists(backupDirPath)) {
                Files.createDirectories(backupDirPath);
            }

            String filePath = Paths.get(backupDirectory, fileName).toString();
            backup.setFileName(fileName);
            backup.setFilePath(filePath);

            backup = backupHistoryRepository.save(backup);

            boolean success = executeMysqlDump(dbName, filePath);

            if (success) {
                File backupFile = new File(filePath);
                if (backupFile.exists()) {
                    backup.setFileSize(backupFile.length());
                }
                backup.setStatus(BackupHistory.BackupStatus.COMPLETED);
                backup.setCompletedAt(LocalDateTime.now());
                log.info("Scheduled backup thành công: {}", fileName);
            } else {
                backup.setStatus(BackupHistory.BackupStatus.FAILED);
                backup.setErrorMessage("mysqldump command failed");
                log.error("Scheduled backup thất bại: {}", fileName);
            }

        } catch (Exception e) {
            backup.setStatus(BackupHistory.BackupStatus.FAILED);
            backup.setErrorMessage(e.getMessage());
            log.error("Lỗi khi scheduled backup: ", e);
        } finally {
            backupHistoryRepository.save(backup);
        }
    }

    /**
     * Khôi phục database từ backup
     */
    @Transactional
    public void restoreBackup(Long backupId, String username) {
        log.info("Bắt đầu restore database từ backup ID: {} bởi user: {}", backupId, username);

        BackupHistory backup = backupHistoryRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("Backup không tồn tại"));

        if (backup.getStatus() != BackupHistory.BackupStatus.COMPLETED) {
            throw new RuntimeException("Chỉ có thể restore từ backup đã hoàn thành");
        }

        File backupFile = new File(backup.getFilePath());
        if (!backupFile.exists()) {
            throw new RuntimeException("File backup không tồn tại: " + backup.getFilePath());
        }

        try {
            String dbName = extractDatabaseName(databaseUrl);
            boolean success = executeMysqlRestore(dbName, backup.getFilePath());

            if (success) {
                log.info("Restore database thành công từ: {}", backup.getFileName());
            } else {
                throw new RuntimeException("mysql restore command failed");
            }

        } catch (Exception e) {
            log.error("Lỗi khi restore database: ", e);
            throw new RuntimeException("Không thể restore database: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách backup history
     */
    @Transactional(readOnly = true)
    public Page<BackupHistoryDTO> getBackupHistory(Pageable pageable) {
        Page<BackupHistory> backups = backupHistoryRepository.findAll(pageable);
        return backups.map(backup -> {
            BackupHistoryDTO dto = BackupHistoryDTO.fromEntity(backup);
            
            // Lấy username của người tạo
            if (backup.getCreatedBy() != null) {
                userRepository.findById(backup.getCreatedBy()).ifPresent(user -> {
                    dto.setCreatedByUsername(user.getUsername());
                });
            }
            
            return dto;
        });
    }

    /**
     * Lấy thống kê backup
     */
    @Transactional(readOnly = true)
    public BackupStatisticsDTO getBackupStatistics() {
        BackupStatisticsDTO stats = new BackupStatisticsDTO();
        
        stats.setTotalBackups(backupHistoryRepository.count());
        stats.setSuccessfulBackups(backupHistoryRepository.countByStatus(BackupHistory.BackupStatus.COMPLETED));
        stats.setFailedBackups(backupHistoryRepository.countByStatus(BackupHistory.BackupStatus.FAILED));
        
        BackupHistory latestBackup = backupHistoryRepository.findLatestSuccessfulBackup();
        if (latestBackup != null) {
            stats.setLatestBackup(BackupHistoryDTO.fromEntity(latestBackup));
        }
        
        // Tính tổng dung lượng
        List<BackupHistory> successfulBackups = backupHistoryRepository
                .findByStatusOrderByCreatedAtDesc(BackupHistory.BackupStatus.COMPLETED);
        
        long totalBytes = successfulBackups.stream()
                .filter(b -> b.getFileSize() != null)
                .mapToLong(BackupHistory::getFileSize)
                .sum();
        
        stats.setTotalStorageBytes(totalBytes);
        stats.setTotalStorageUsed(formatBytes(totalBytes));
        
        return stats;
    }

    /**
     * Xóa backup cũ (cleanup)
     */
    @Transactional
    public void cleanupOldBackups() {
        log.info("Bắt đầu cleanup old backups (retention: {} days, max count: {})", retentionDays, maxBackupCount);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        
        // Xóa backup cũ hơn retention days
        List<BackupHistory> oldBackups = backupHistoryRepository
                .findByCreatedAtBeforeAndStatus(cutoffDate, BackupHistory.BackupStatus.COMPLETED);
        
        for (BackupHistory backup : oldBackups) {
            deleteBackupFile(backup);
            backupHistoryRepository.delete(backup);
            log.info("Đã xóa backup cũ: {}", backup.getFileName());
        }

        // Giới hạn số lượng backup
        List<BackupHistory> allBackups = backupHistoryRepository
                .findByStatusOrderByCreatedAtDesc(BackupHistory.BackupStatus.COMPLETED);
        
        if (allBackups.size() > maxBackupCount) {
            List<BackupHistory> excessBackups = allBackups.subList(maxBackupCount, allBackups.size());
            for (BackupHistory backup : excessBackups) {
                deleteBackupFile(backup);
                backupHistoryRepository.delete(backup);
                log.info("Đã xóa backup vượt quá giới hạn: {}", backup.getFileName());
            }
        }

        log.info("Cleanup completed. Xóa {} backups", oldBackups.size() + Math.max(0, allBackups.size() - maxBackupCount));
    }

    /**
     * Xóa một backup cụ thể
     */
    @Transactional
    public void deleteBackup(Long backupId) {
        BackupHistory backup = backupHistoryRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("Backup không tồn tại"));

        deleteBackupFile(backup);
        backupHistoryRepository.delete(backup);
        log.info("Đã xóa backup: {}", backup.getFileName());
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private boolean executeMysqlDump(String dbName, String outputFilePath) {
        try {
            // Build mysqldump command
            ProcessBuilder processBuilder;
            
            if (databasePassword != null && !databasePassword.isEmpty()) {
                processBuilder = new ProcessBuilder(
                    "mysqldump",
                    "-u" + databaseUsername,
                    "-p" + databasePassword,
                    "--databases", dbName,
                    "--result-file=" + outputFilePath,
                    "--single-transaction",
                    "--quick",
                    "--lock-tables=false"
                );
            } else {
                processBuilder = new ProcessBuilder(
                    "mysqldump",
                    "-u" + databaseUsername,
                    "--databases", dbName,
                    "--result-file=" + outputFilePath,
                    "--single-transaction",
                    "--quick",
                    "--lock-tables=false"
                );
            }

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Đọc output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("mysqldump: {}", line);
            }

            int exitCode = process.waitFor();
            return exitCode == 0;

        } catch (Exception e) {
            log.error("Error executing mysqldump: ", e);
            return false;
        }
    }

    private boolean executeMysqlRestore(String dbName, String inputFilePath) {
        try {
            ProcessBuilder processBuilder;
            
            if (databasePassword != null && !databasePassword.isEmpty()) {
                processBuilder = new ProcessBuilder(
                    "mysql",
                    "-u" + databaseUsername,
                    "-p" + databasePassword,
                    dbName
                );
            } else {
                processBuilder = new ProcessBuilder(
                    "mysql",
                    "-u" + databaseUsername,
                    dbName
                );
            }

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Đọc file backup và ghi vào process input
            try (BufferedReader fileReader = new BufferedReader(new FileReader(inputFilePath));
                 BufferedWriter processWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                
                String line;
                while ((line = fileReader.readLine()) != null) {
                    processWriter.write(line);
                    processWriter.newLine();
                }
            }

            // Đọc output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("mysql restore: {}", line);
            }

            int exitCode = process.waitFor();
            return exitCode == 0;

        } catch (Exception e) {
            log.error("Error executing mysql restore: ", e);
            return false;
        }
    }

    private String extractDatabaseName(String jdbcUrl) {
        // jdbc:mysql://localhost:3306/finpal_db?...
        try {
            String[] parts = jdbcUrl.split("/");
            String dbPart = parts[parts.length - 1];
            return dbPart.split("\\?")[0];
        } catch (Exception e) {
            log.error("Error extracting database name from URL: {}", jdbcUrl);
            return "finpal_db"; // fallback
        }
    }

    private void deleteBackupFile(BackupHistory backup) {
        try {
            File file = new File(backup.getFilePath());
            if (file.exists()) {
                Files.delete(file.toPath());
                log.debug("Đã xóa file backup: {}", backup.getFilePath());
            }
        } catch (IOException e) {
            log.error("Không thể xóa file backup: {}", backup.getFilePath(), e);
        }
    }

    private String formatBytes(long bytes) {
        if (bytes == 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
