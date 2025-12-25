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

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

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
            BackupResult result = executeMysqlDump(dbName, filePath);

            if (result.success) {
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
                backup.setErrorMessage(result.errorMessage);
                log.error("Backup thất bại: {} - Lỗi: {}", fileName, result.errorMessage);
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

            BackupResult result = executeMysqlDump(dbName, filePath);

            if (result.success) {
                File backupFile = new File(filePath);
                if (backupFile.exists()) {
                    backup.setFileSize(backupFile.length());
                }
                backup.setStatus(BackupHistory.BackupStatus.COMPLETED);
                backup.setCompletedAt(LocalDateTime.now());
                log.info("Scheduled backup thành công: {}", fileName);
            } else {
                backup.setStatus(BackupHistory.BackupStatus.FAILED);
                backup.setErrorMessage(result.errorMessage);
                log.error("Scheduled backup thất bại: {} - Lỗi: {}", fileName, result.errorMessage);
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

        log.info("Cleanup completed. Xóa {} backups",
                oldBackups.size() + Math.max(0, allBackups.size() - maxBackupCount));
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

    /**
     * Kiểm tra sức khỏe hệ thống backup
     * Trả về thông tin chi tiết về tình trạng các thành phần cần thiết để backup
     * hoạt động
     */
    public java.util.Map<String, Object> checkBackupSystemHealth() {
        java.util.Map<String, Object> health = new java.util.LinkedHashMap<>();
        boolean overallHealthy = true;
        java.util.List<String> issues = new java.util.ArrayList<>();

        // 1. Kiểm tra mysqldump có sẵn không
        boolean mysqldumpAvailable = isMysqldumpAvailable();
        health.put("mysqldumpAvailable", mysqldumpAvailable);
        if (!mysqldumpAvailable) {
            overallHealthy = false;
            issues.add("mysqldump không được cài đặt hoặc không có trong PATH. Vui lòng cài đặt MySQL client tools.");
        }

        // 2. Kiểm tra thư mục backup
        java.util.Map<String, Object> directoryInfo = new java.util.LinkedHashMap<>();
        directoryInfo.put("path", backupDirectory);

        Path backupDirPath = Paths.get(backupDirectory);
        boolean directoryExists = Files.exists(backupDirPath);
        directoryInfo.put("exists", directoryExists);

        if (!directoryExists) {
            try {
                Files.createDirectories(backupDirPath);
                directoryInfo.put("created", true);
                directoryInfo.put("exists", true);
                directoryExists = true;
            } catch (IOException e) {
                directoryInfo.put("created", false);
                directoryInfo.put("createError", e.getMessage());
                overallHealthy = false;
                issues.add("Không thể tạo thư mục backup: " + e.getMessage());
            }
        }

        if (directoryExists) {
            boolean writable = Files.isWritable(backupDirPath);
            directoryInfo.put("writable", writable);
            if (!writable) {
                overallHealthy = false;
                issues.add("Thư mục backup không có quyền ghi: " + backupDirectory);
            }

            // Kiểm tra dung lượng trống
            try {
                java.io.File dir = backupDirPath.toFile();
                long freeSpace = dir.getFreeSpace();
                long totalSpace = dir.getTotalSpace();
                directoryInfo.put("freeSpaceBytes", freeSpace);
                directoryInfo.put("freeSpace", formatBytes(freeSpace));
                directoryInfo.put("totalSpaceBytes", totalSpace);
                directoryInfo.put("totalSpace", formatBytes(totalSpace));

                // Cảnh báo nếu dung lượng trống < 1GB
                if (freeSpace < 1024L * 1024 * 1024) {
                    issues.add("Dung lượng trống thấp: chỉ còn " + formatBytes(freeSpace));
                }
            } catch (Exception e) {
                log.warn("Không thể kiểm tra dung lượng đĩa: {}", e.getMessage());
            }
        }
        health.put("backupDirectory", directoryInfo);

        // 3. Kiểm tra kết nối database
        java.util.Map<String, Object> databaseInfo = new java.util.LinkedHashMap<>();
        databaseInfo.put("url", databaseUrl.replaceAll("password=[^&]*", "password=***"));
        databaseInfo.put("username", databaseUsername);
        databaseInfo.put("databaseName", extractDatabaseName(databaseUrl));
        databaseInfo.put("host", extractHost(databaseUrl));
        databaseInfo.put("port", extractPort(databaseUrl));
        health.put("database", databaseInfo);

        // 4. Thống kê backup gần đây
        java.util.Map<String, Object> recentStats = new java.util.LinkedHashMap<>();
        recentStats.put("totalBackups", backupHistoryRepository.count());
        recentStats.put("successfulBackups",
                backupHistoryRepository.countByStatus(BackupHistory.BackupStatus.COMPLETED));
        recentStats.put("failedBackups", backupHistoryRepository.countByStatus(BackupHistory.BackupStatus.FAILED));
        recentStats.put("inProgressBackups",
                backupHistoryRepository.countByStatus(BackupHistory.BackupStatus.IN_PROGRESS));
        health.put("statistics", recentStats);

        // 5. Cấu hình backup
        java.util.Map<String, Object> configInfo = new java.util.LinkedHashMap<>();
        configInfo.put("retentionDays", retentionDays);
        configInfo.put("maxBackupCount", maxBackupCount);
        health.put("configuration", configInfo);

        // Kết quả tổng hợp
        health.put("healthy", overallHealthy);
        health.put("issues", issues);
        health.put("checkedAt", LocalDateTime.now().toString());

        return health;
    }

    /**
     * Lấy danh sách backup thất bại với chi tiết lỗi
     */
    @Transactional(readOnly = true)
    public Page<BackupHistoryDTO> getFailedBackups(org.springframework.data.domain.Pageable pageable) {
        Page<BackupHistory> failedBackups = backupHistoryRepository.findByStatus(
                BackupHistory.BackupStatus.FAILED, pageable);

        return failedBackups.map(backup -> {
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
     * Lấy file backup dưới dạng Resource để tải về
     * 
     * @param backupId ID của backup cần tải
     * @return Resource của file backup
     * @throws RuntimeException nếu backup không tồn tại hoặc file không tìm thấy
     */
    @Transactional(readOnly = true)
    public Resource getBackupFileAsResource(Long backupId) {
        BackupHistory backup = backupHistoryRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("Backup không tồn tại với ID: " + backupId));

        // Chỉ cho phép tải backup đã hoàn thành
        if (backup.getStatus() != BackupHistory.BackupStatus.COMPLETED) {
            throw new RuntimeException(
                    "Chỉ có thể tải các backup đã hoàn thành thành công. Trạng thái hiện tại: " + backup.getStatus());
        }

        File backupFile = new File(backup.getFilePath());
        if (!backupFile.exists()) {
            throw new RuntimeException("File backup không tồn tại trên server: " + backup.getFileName());
        }

        log.info("Đang chuẩn bị tải file backup: {} ({})", backup.getFileName(), formatBytes(backup.getFileSize()));

        return new FileSystemResource(backupFile);
    }

    /**
     * Lấy tên file của backup
     * 
     * @param backupId ID của backup
     * @return Tên file backup
     */
    @Transactional(readOnly = true)
    public String getBackupFileName(Long backupId) {
        BackupHistory backup = backupHistoryRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("Backup không tồn tại với ID: " + backupId));

        return backup.getFileName();
    }

    /**
     * Lấy thông tin chi tiết của một backup
     * 
     * @param backupId ID của backup
     * @return DTO chứa thông tin backup
     */
    @Transactional(readOnly = true)
    public BackupHistoryDTO getBackupById(Long backupId) {
        BackupHistory backup = backupHistoryRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("Backup không tồn tại với ID: " + backupId));

        BackupHistoryDTO dto = BackupHistoryDTO.fromEntity(backup);

        // Lấy username của người tạo nếu có
        if (backup.getCreatedBy() != null) {
            userRepository.findById(backup.getCreatedBy()).ifPresent(user -> {
                dto.setCreatedByUsername(user.getUsername());
            });
        }

        return dto;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Kết quả backup chứa cả status và error message
     */
    private static class BackupResult {
        boolean success;
        String errorMessage;

        BackupResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
    }

    private BackupResult executeMysqlDump(String dbName, String outputFilePath) {
        try {
            // Kiểm tra mysqldump có tồn tại không
            if (!isMysqldumpAvailable()) {
                String error = "mysqldump không được cài đặt hoặc không có trong PATH. Vui lòng cài đặt MySQL client tools.";
                log.error(error);
                return new BackupResult(false, error);
            }

            // Build mysqldump command
            ProcessBuilder processBuilder;
            String host = extractHost(databaseUrl);
            String port = extractPort(databaseUrl);

            List<String> command = new java.util.ArrayList<>();
            command.add("mysqldump");
            command.add("-h" + host);
            command.add("-P" + port);
            command.add("-u" + databaseUsername);
            if (databasePassword != null && !databasePassword.isEmpty()) {
                command.add("-p" + databasePassword);
            }
            command.add("--databases");
            command.add(dbName);
            command.add("--result-file=" + outputFilePath);
            command.add("--single-transaction");
            command.add("--quick");
            command.add("--lock-tables=false");
            command.add("--routines");
            command.add("--triggers");

            processBuilder = new ProcessBuilder(command);

            // QUAN TRỌNG: Không merge stderr vào stdout để capture error riêng
            processBuilder.redirectErrorStream(false);
            Process process = processBuilder.start();

            // Đọc stdout và stderr riêng biệt trong thread riêng
            StringBuilder stdoutBuilder = new StringBuilder();
            StringBuilder stderrBuilder = new StringBuilder();

            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stdoutBuilder.append(line).append("\n");
                        log.debug("mysqldump stdout: {}", line);
                    }
                } catch (IOException e) {
                    log.error("Error reading stdout", e);
                }
            });

            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Bỏ qua warning về password trên command line
                        if (!line.contains("Using a password on the command line interface can be insecure")) {
                            stderrBuilder.append(line).append("\n");
                            log.warn("mysqldump stderr: {}", line);
                        }
                    }
                } catch (IOException e) {
                    log.error("Error reading stderr", e);
                }
            });

            stdoutThread.start();
            stderrThread.start();

            // Chờ process hoàn thành với timeout 30 phút
            boolean finished = process.waitFor(30, java.util.concurrent.TimeUnit.MINUTES);

            if (!finished) {
                process.destroyForcibly();
                return new BackupResult(false, "Backup timeout sau 30 phút");
            }

            // Chờ các thread đọc output hoàn thành
            stdoutThread.join(5000);
            stderrThread.join(5000);

            int exitCode = process.exitValue();

            if (exitCode == 0) {
                log.info("mysqldump hoàn thành thành công, exit code: 0");
                return new BackupResult(true, null);
            } else {
                String errorMsg = stderrBuilder.toString().trim();
                if (errorMsg.isEmpty()) {
                    errorMsg = "mysqldump thất bại với exit code: " + exitCode;
                }
                log.error("mysqldump thất bại: {}", errorMsg);
                return new BackupResult(false, errorMsg);
            }

        } catch (Exception e) {
            String error = "Lỗi khi thực thi mysqldump: " + e.getMessage();
            log.error(error, e);
            return new BackupResult(false, error);
        }
    }

    /**
     * Kiểm tra mysqldump có sẵn trong hệ thống không
     */
    private boolean isMysqldumpAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("mysqldump", "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            int exitCode = process.waitFor();

            if (exitCode == 0 && line != null) {
                log.info("mysqldump version: {}", line);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("mysqldump không khả dụng: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Trích xuất host từ JDBC URL
     */
    private String extractHost(String jdbcUrl) {
        try {
            // jdbc:mysql://hostname:port/database
            String url = jdbcUrl.replace("jdbc:mysql://", "");
            String hostPort = url.split("/")[0];
            return hostPort.split(":")[0];
        } catch (Exception e) {
            return "localhost";
        }
    }

    /**
     * Trích xuất port từ JDBC URL
     */
    private String extractPort(String jdbcUrl) {
        try {
            String url = jdbcUrl.replace("jdbc:mysql://", "");
            String hostPort = url.split("/")[0];
            String[] parts = hostPort.split(":");
            if (parts.length > 1) {
                return parts[1];
            }
            return "3306"; // Default MySQL port
        } catch (Exception e) {
            return "3306";
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
                        dbName);
            } else {
                processBuilder = new ProcessBuilder(
                        "mysql",
                        "-u" + databaseUsername,
                        dbName);
            }

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Đọc file backup và ghi vào process input
            try (BufferedReader fileReader = new BufferedReader(new FileReader(inputFilePath));
                    BufferedWriter processWriter = new BufferedWriter(
                            new OutputStreamWriter(process.getOutputStream()))) {

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
        // jdbc:mysql://mysql:3306/finpal_db?createDatabaseIfNotExist=true&serverTimezone=Asia/Ho_Chi_Minh...
        try {
            // Bỏ phần "jdbc:mysql://"
            String url = jdbcUrl.replace("jdbc:mysql://", "");

            // Tìm vị trí dấu / đầu tiên sau host:port
            int slashIndex = url.indexOf('/');
            if (slashIndex == -1) {
                log.warn("Không tìm thấy database name trong URL: {}", jdbcUrl);
                return "finpal_db"; // fallback
            }

            // Lấy phần sau dấu /
            String dbPart = url.substring(slashIndex + 1);

            // Tách bỏ query parameters (phần sau dấu ?)
            int questionMarkIndex = dbPart.indexOf('?');
            if (questionMarkIndex != -1) {
                dbPart = dbPart.substring(0, questionMarkIndex);
            }

            log.debug("Extracted database name: {} from URL: {}", dbPart, jdbcUrl);
            return dbPart.isEmpty() ? "finpal_db" : dbPart;

        } catch (Exception e) {
            log.error("Error extracting database name from URL: {}", jdbcUrl, e);
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

        String[] units = { "B", "KB", "MB", "GB" };
        int unitIndex = 0;
        double size = bytes;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
