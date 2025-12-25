package com.example.backend.controller;

import com.example.backend.dto.CreateUserRequestDTO;
import com.example.backend.dto.ProcessRequestDTO;
import com.example.backend.dto.UserRequestDTO;
import com.example.backend.model.AccountDeletionLog;
import com.example.backend.model.UserRequest;
import com.example.backend.repository.AccountDeletionLogRepository;
import com.example.backend.service.UserRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
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

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-requests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Requests", description = "API quản lý yêu cầu của người dùng (xuất dữ liệu, xóa tài khoản)")
public class UserRequestController {

    private final UserRequestService userRequestService;
    private final AccountDeletionLogRepository accountDeletionLogRepository;

    /**
     * Tạo yêu cầu mới (người dùng)
     */
    @PostMapping
    @Operation(summary = "Tạo yêu cầu mới", description = "Người dùng tạo yêu cầu xuất dữ liệu hoặc xóa tài khoản")
    public ResponseEntity<?> createRequest(
            @Valid @RequestBody CreateUserRequestDTO requestDTO,
            Authentication authentication) {
        try {
            String username = authentication.getName();

            UserRequest.RequestType requestType = UserRequest.RequestType.valueOf(requestDTO.getRequestType());

            UserRequest request = userRequestService.createRequest(
                    username,
                    requestType,
                    requestDTO.getReason());

            return ResponseEntity.status(HttpStatus.CREATED).body(UserRequestDTO.fromEntity(request));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Loại yêu cầu không hợp lệ: " + requestDTO.getRequestType());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating user request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tạo yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách yêu cầu của người dùng hiện tại
     */
    @GetMapping("/my-requests")
    @Operation(summary = "Lấy yêu cầu của tôi", description = "Lấy tất cả yêu cầu của người dùng hiện tại")
    public ResponseEntity<?> getMyRequests(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<UserRequest> requests = userRequestService.getUserRequests(username);

            List<UserRequestDTO> dtos = requests.stream()
                    .map(UserRequestDTO::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            log.error("Error getting user requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả yêu cầu (admin) với phân trang
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy tất cả yêu cầu (Admin)", description = "Admin lấy tất cả yêu cầu với phân trang")
    public ResponseEntity<?> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<UserRequest> requestsPage = userRequestService.getAllRequests(pageable);

            Page<UserRequestDTO> dtoPage = requestsPage.map(UserRequestDTO::fromEntity);

            return ResponseEntity.ok(dtoPage);

        } catch (Exception e) {
            log.error("Error getting all requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Lấy yêu cầu theo trạng thái (admin)
     */
    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy yêu cầu theo trạng thái (Admin)", description = "Admin lấy yêu cầu theo trạng thái")
    public ResponseEntity<?> getRequestsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            UserRequest.RequestStatus requestStatus = UserRequest.RequestStatus.valueOf(status);

            Pageable pageable = PageRequest.of(page, size);
            Page<UserRequest> requestsPage = userRequestService.getRequestsByStatus(requestStatus, pageable);

            Page<UserRequestDTO> dtoPage = requestsPage.map(UserRequestDTO::fromEntity);

            return ResponseEntity.ok(dtoPage);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Trạng thái không hợp lệ: " + status);
        } catch (Exception e) {
            log.error("Error getting requests by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Lấy yêu cầu theo loại (admin)
     */
    @GetMapping("/admin/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy yêu cầu theo loại (Admin)", description = "Admin lấy yêu cầu theo loại")
    public ResponseEntity<?> getRequestsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            UserRequest.RequestType requestType = UserRequest.RequestType.valueOf(type);

            Pageable pageable = PageRequest.of(page, size);
            Page<UserRequest> requestsPage = userRequestService.getRequestsByType(requestType, pageable);

            Page<UserRequestDTO> dtoPage = requestsPage.map(UserRequestDTO::fromEntity);

            return ResponseEntity.ok(dtoPage);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Loại yêu cầu không hợp lệ: " + type);
        } catch (Exception e) {
            log.error("Error getting requests by type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Đếm số yêu cầu đang chờ duyệt (admin)
     */
    @GetMapping("/admin/pending-count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Đếm yêu cầu chờ duyệt (Admin)", description = "Đếm số yêu cầu đang chờ admin duyệt")
    public ResponseEntity<?> countPendingRequests() {
        try {
            long count = userRequestService.countPendingRequests();
            return ResponseEntity.ok(count);

        } catch (Exception e) {
            log.error("Error counting pending requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi đếm yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Duyệt yêu cầu (admin)
     */
    @PutMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Duyệt yêu cầu (Admin)", description = "Admin duyệt yêu cầu của người dùng")
    public ResponseEntity<?> approveRequest(
            @PathVariable Long id,
            @RequestBody ProcessRequestDTO processDTO,
            Authentication authentication) {
        try {
            String adminUsername = authentication.getName();

            UserRequest request = userRequestService.approveRequest(
                    id,
                    adminUsername,
                    processDTO.getAdminNote());

            return ResponseEntity.ok(UserRequestDTO.fromEntity(request));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error approving request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi duyệt yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Từ chối yêu cầu (admin)
     */
    @PutMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Từ chối yêu cầu (Admin)", description = "Admin từ chối yêu cầu của người dùng")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long id,
            @RequestBody ProcessRequestDTO processDTO,
            Authentication authentication) {
        try {
            String adminUsername = authentication.getName();

            UserRequest request = userRequestService.rejectRequest(
                    id,
                    adminUsername,
                    processDTO.getAdminNote());

            return ResponseEntity.ok(UserRequestDTO.fromEntity(request));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error rejecting request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi từ chối yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Hủy yêu cầu xóa tài khoản đã được phê duyệt (admin)
     * Chỉ có thể hủy trong 24h sau khi phê duyệt
     */
    @PutMapping("/admin/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Hủy yêu cầu xóa tài khoản (Admin)", description = "Hủy yêu cầu xóa tài khoản đã được phê duyệt. Chỉ có thể hủy trong vòng 24 giờ sau khi phê duyệt.")
    public ResponseEntity<?> cancelDeletionRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ProcessRequestDTO processDTO,
            Authentication authentication) {
        try {
            String adminUsername = authentication.getName();

            // Lấy admin ID
            Long adminId = userRequestService.getUserIdByUsername(adminUsername);

            String cancelReason = processDTO != null ? processDTO.getAdminNote() : null;

            UserRequest request = userRequestService.cancelApprovedDeletion(id, adminId, cancelReason);

            log.info("Admin {} cancelled deletion request {}", adminUsername, id);

            return ResponseEntity.ok(UserRequestDTO.fromEntity(request));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error cancelling deletion request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi hủy yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Lấy thống kê yêu cầu (admin)
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thống kê yêu cầu (Admin)", description = "Lấy thống kê yêu cầu theo loại và trạng thái")
    public ResponseEntity<?> getRequestStatistics() {
        try {
            List<Object[]> stats = userRequestService.getRequestStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error getting request statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy thống kê: " + e.getMessage());
        }
    }

    /**
     * Tải file xuất dữ liệu PDF (admin)
     */
    @GetMapping("/admin/{id}/download-export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tải file xuất dữ liệu (Admin)", description = "Admin tải file PDF xuất dữ liệu của người dùng. Chỉ áp dụng cho yêu cầu EXPORT_DATA đã hoàn thành.")
    public ResponseEntity<?> downloadExportFile(@PathVariable Long id) {
        try {
            // Lấy thông tin yêu cầu
            UserRequest request = userRequestService.getRequestById(id);

            // Kiểm tra loại yêu cầu
            if (request.getRequestType() != UserRequest.RequestType.EXPORT_DATA) {
                return ResponseEntity.badRequest()
                        .body("Chỉ có thể tải file cho yêu cầu xuất dữ liệu (EXPORT_DATA)");
            }

            // Kiểm tra trạng thái
            if (request.getStatus() != UserRequest.RequestStatus.COMPLETED) {
                return ResponseEntity.badRequest()
                        .body("Yêu cầu chưa được xử lý hoàn thành. Trạng thái hiện tại: " + request.getStatus());
            }

            // Kiểm tra file path
            String filePath = request.getFilePath();
            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy file xuất dữ liệu cho yêu cầu này");
            }

            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("File xuất dữ liệu không còn tồn tại trên server: " + file.getName());
            }

            // Tạo tên file download
            String downloadFileName = String.format("export_data_%s_%s.pdf",
                    request.getUser().getUsername(),
                    request.getCreatedAt().toLocalDate().toString());

            Resource resource = new FileSystemResource(file);

            log.info("Admin downloading export file for request ID: {}, user: {}",
                    id, request.getUser().getUsername());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"")
                    .body(resource);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error downloading export file for request {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tải file: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra trạng thái file xuất dữ liệu (admin)
     */
    @GetMapping("/admin/{id}/export-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kiểm tra trạng thái file xuất dữ liệu (Admin)", description = "Kiểm tra xem file xuất dữ liệu có tồn tại không và thông tin chi tiết")
    public ResponseEntity<?> checkExportFileStatus(@PathVariable Long id) {
        try {
            UserRequest request = userRequestService.getRequestById(id);

            java.util.Map<String, Object> status = new java.util.LinkedHashMap<>();
            status.put("requestId", id);
            status.put("requestType", request.getRequestType().name());
            status.put("requestStatus", request.getStatus().name());
            status.put("username", request.getUser().getUsername());

            if (request.getRequestType() == UserRequest.RequestType.EXPORT_DATA) {
                String filePath = request.getFilePath();
                if (filePath != null && !filePath.isEmpty()) {
                    File file = new File(filePath);
                    status.put("fileExists", file.exists());
                    status.put("filePath", filePath);

                    if (file.exists()) {
                        status.put("fileSize", file.length());
                        status.put("fileSizeFormatted", formatBytes(file.length()));
                        status.put("lastModified", new java.util.Date(file.lastModified()).toString());
                        status.put("downloadable", true);
                    } else {
                        status.put("downloadable", false);
                        status.put("message", "File đã bị xóa hoặc không còn tồn tại");
                    }
                } else {
                    status.put("fileExists", false);
                    status.put("downloadable", false);
                    status.put("message", "Chưa có file xuất dữ liệu");
                }
            } else {
                status.put("downloadable", false);
                status.put("message", "Loại yêu cầu này không có file để tải");
            }

            return ResponseEntity.ok(status);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error checking export file status for request {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi kiểm tra trạng thái file: " + e.getMessage());
        }
    }

    /**
     * Hàm helper để format bytes thành human-readable
     */
    private String formatBytes(long bytes) {
        if (bytes == 0)
            return "0 B";
        String[] units = { "B", "KB", "MB", "GB" };
        int unitIndex = 0;
        double size = bytes;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
    }

    // ==================== ACCOUNT DELETION HISTORY APIs ====================

    /**
     * Lấy lịch sử xóa tài khoản (admin)
     */
    @GetMapping("/admin/deletion-history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy lịch sử xóa tài khoản (Admin)", description = "Lấy danh sách tất cả tài khoản đã bị xóa với phân trang. Thông tin bao gồm username, email, admin đã phê duyệt, thời gian xóa, v.v.")
    public ResponseEntity<?> getDeletionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "deletedAt"));
            Page<AccountDeletionLog> history = accountDeletionLogRepository.findAllByOrderByDeletedAtDesc(pageable);
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Error getting deletion history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy lịch sử xóa tài khoản: " + e.getMessage());
        }
    }

    /**
     * Tìm kiếm trong lịch sử xóa tài khoản (admin)
     */
    @GetMapping("/admin/deletion-history/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tìm kiếm lịch sử xóa tài khoản (Admin)", description = "Tìm kiếm trong lịch sử xóa tài khoản theo username hoặc email")
    public ResponseEntity<?> searchDeletionHistory(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AccountDeletionLog> results = accountDeletionLogRepository.searchByUsernameOrEmail(keyword, pageable);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            log.error("Error searching deletion history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tìm kiếm: " + e.getMessage());
        }
    }

    /**
     * Xem chi tiết một bản ghi xóa tài khoản (admin)
     */
    @GetMapping("/admin/deletion-history/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xem chi tiết xóa tài khoản (Admin)", description = "Xem chi tiết một bản ghi xóa tài khoản theo ID")
    public ResponseEntity<?> getDeletionDetail(@PathVariable Long id) {
        try {
            AccountDeletionLog log = accountDeletionLogRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi với ID: " + id));
            return ResponseEntity.ok(log);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error getting deletion detail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy chi tiết: " + e.getMessage());
        }
    }

    /**
     * Thống kê xóa tài khoản (admin)
     */
    @GetMapping("/admin/deletion-history/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thống kê xóa tài khoản (Admin)", description = "Lấy thống kê tổng quan về việc xóa tài khoản")
    public ResponseEntity<?> getDeletionStats() {
        try {
            java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
            stats.put("totalDeletedAccounts", accountDeletionLogRepository.count());

            // Có thể thêm các thống kê khác nếu cần

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error getting deletion stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy thống kê: " + e.getMessage());
        }
    }
}
