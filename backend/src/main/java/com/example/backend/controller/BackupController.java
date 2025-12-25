package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.DatabaseBackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller quản lý sao lưu và khôi phục database
 * Chỉ ADMIN mới có quyền truy cập
 */
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
@Tag(name = "Database Backup", description = "API quản lý sao lưu và khôi phục CSDL (chỉ ADMIN)")
public class BackupController {

    private final DatabaseBackupService backupService;

    /**
     * POST /api/backup/create
     * Tạo backup database thủ công
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo backup database", description = "Tạo bản sao lưu database thủ công. Chỉ ADMIN mới có quyền. "
            +
            "Backup sẽ được thực hiện bất đồng bộ.")
    public ResponseEntity<Map<String, Object>> createBackup(
            @RequestBody(required = false) CreateBackupRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        String note = request != null ? request.getNote() : null;

        // Async backup
        backupService.createBackup(username, note);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã bắt đầu quá trình backup database");

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * POST /api/backup/restore
     * Khôi phục database từ backup
     */
    @PostMapping("/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Khôi phục database", description = "Khôi phục database từ một backup đã lưu. Chỉ ADMIN mới có quyền. "
            +
            "⚠️ CẢNH BÁO: Thao tác này sẽ ghi đè toàn bộ dữ liệu hiện tại!")
    public ResponseEntity<Map<String, Object>> restoreBackup(
            @Valid @RequestBody RestoreBackupRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        try {
            backupService.restoreBackup(request.getBackupId(), username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Khôi phục database thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi khôi phục: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/backup/history
     * Lấy danh sách lịch sử backup (phân trang)
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy lịch sử backup", description = "Lấy danh sách tất cả các bản backup (có phân trang). Chỉ ADMIN mới có quyền.")
    public ResponseEntity<Page<BackupHistoryDTO>> getBackupHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        // Parse sort parameters
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<BackupHistoryDTO> backupHistory = backupService.getBackupHistory(pageable);
        return ResponseEntity.ok(backupHistory);
    }

    /**
     * GET /api/backup/statistics
     * Lấy thống kê backup
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy thống kê backup", description = "Lấy thống kê tổng quan về backup (tổng số, thành công, thất bại, dung lượng). Chỉ ADMIN mới có quyền.")
    public ResponseEntity<BackupStatisticsDTO> getBackupStatistics() {
        BackupStatisticsDTO statistics = backupService.getBackupStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * GET /api/backup/download/{id}
     * Tải file backup về máy
     */
    @GetMapping("/download/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tải file backup", description = "Tải file backup SQL về máy. Chỉ có thể tải các backup đã hoàn thành thành công. Chỉ ADMIN mới có quyền.")
    public ResponseEntity<?> downloadBackup(@PathVariable Long id) {
        try {
            Resource resource = backupService.getBackupFileAsResource(id);
            String fileName = backupService.getBackupFileName(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi tải backup: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * DELETE /api/backup/{id}
     * Xóa một backup
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa backup", description = "Xóa một bản backup cụ thể (cả file và record). Chỉ ADMIN mới có quyền.")
    public ResponseEntity<Map<String, Object>> deleteBackup(@PathVariable Long id) {

        try {
            backupService.deleteBackup(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xóa backup thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi xóa backup: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * POST /api/backup/cleanup
     * Xóa các backup cũ theo retention policy
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dọn dẹp backup cũ", description = "Xóa các backup cũ theo chính sách lưu trữ (retention policy). Chỉ ADMIN mới có quyền.")
    public ResponseEntity<Map<String, Object>> cleanupOldBackups() {

        try {
            backupService.cleanupOldBackups();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã dọn dẹp backup cũ thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi cleanup: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/backup/health
     * Kiểm tra tình trạng hệ thống backup
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kiểm tra sức khỏe hệ thống backup", description = "Kiểm tra xem hệ thống backup có hoạt động bình thường không (mysqldump có sẵn, thư mục có quyền ghi). Chỉ ADMIN mới có quyền.")
    public ResponseEntity<Map<String, Object>> checkBackupHealth() {
        Map<String, Object> health = backupService.checkBackupSystemHealth();
        return ResponseEntity.ok(health);
    }

    /**
     * GET /api/backup/failed
     * Lấy danh sách các backup thất bại với chi tiết lỗi
     */
    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách backup thất bại", description = "Lấy danh sách các backup thất bại kèm thông tin lỗi chi tiết. Chỉ ADMIN mới có quyền.")
    public ResponseEntity<Page<BackupHistoryDTO>> getFailedBackups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BackupHistoryDTO> failedBackups = backupService.getFailedBackups(pageable);
        return ResponseEntity.ok(failedBackups);
    }
}
