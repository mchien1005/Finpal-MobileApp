package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.model.UserRequest;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRequestService {

    private final UserRequestRepository userRequestRepository;
    private final UserRepository userRepository;
    private final UserDataExportService dataExportService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Value("${pdf.export.retention.days:7}")
    private int exportRetentionDays;

    /**
     * Tạo yêu cầu mới từ người dùng
     */
    @Transactional
    public UserRequest createRequest(String username, UserRequest.RequestType requestType, String reason) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra xem user đã có yêu cầu PENDING cùng loại chưa
        boolean hasPendingRequest = userRequestRepository.existsByUserIdAndStatusAndRequestType(
                user.getId(),
                UserRequest.RequestStatus.PENDING,
                requestType);

        if (hasPendingRequest) {
            throw new RuntimeException("Bạn đã có một yêu cầu " + requestType + " đang chờ xử lý");
        }

        UserRequest request = new UserRequest();
        request.setUser(user);
        request.setRequestType(requestType);
        request.setReason(reason);
        request.setStatus(UserRequest.RequestStatus.PENDING);

        UserRequest savedRequest = userRequestRepository.save(request);
        log.info("Created new user request: {} for user: {}", requestType, username);

        // Gửi notification cho admin
        sendNotificationToAdmins(savedRequest);

        return savedRequest;
    }

    /**
     * Gửi notification cho tất cả admin khi có yêu cầu mới
     */
    private void sendNotificationToAdmins(UserRequest request) {
        try {
            String requestTypeName = request.getRequestType() == UserRequest.RequestType.EXPORT_DATA
                    ? "xuất dữ liệu"
                    : "xóa tài khoản";

            String title = "Yêu cầu mới từ người dùng";
            String message = String.format("Người dùng %s đã gửi yêu cầu %s. Vui lòng kiểm tra và xử lý.",
                    request.getUser().getUsername(),
                    requestTypeName);

            // Gửi notification cho tất cả admin
            notificationService.sendNotificationToAllAdmins(title, message);

            log.info("Sent notification to admins about new request from user: {}", request.getUser().getUsername());

        } catch (Exception e) {
            log.error("Error sending notification to admins for request {}", request.getId(), e);
            // Không throw exception vì notification không phải là critical operation
        }
    }

    /**
     * Lấy tất cả yêu cầu của một người dùng
     */
    @Transactional(readOnly = true)
    public List<UserRequest> getUserRequests(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        return userRequestRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    /**
     * Lấy tất cả yêu cầu (cho admin) với phân trang
     */
    @Transactional(readOnly = true)
    public Page<UserRequest> getAllRequests(Pageable pageable) {
        return userRequestRepository.findAll(pageable);
    }

    /**
     * Lấy yêu cầu theo trạng thái
     */
    @Transactional(readOnly = true)
    public Page<UserRequest> getRequestsByStatus(UserRequest.RequestStatus status, Pageable pageable) {
        return userRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    /**
     * Lấy yêu cầu theo loại
     */
    @Transactional(readOnly = true)
    public Page<UserRequest> getRequestsByType(UserRequest.RequestType type, Pageable pageable) {
        return userRequestRepository.findByRequestTypeOrderByCreatedAtDesc(type, pageable);
    }

    /**
     * Đếm số yêu cầu đang chờ duyệt
     */
    @Transactional(readOnly = true)
    public long countPendingRequests() {
        return userRequestRepository.countByStatus(UserRequest.RequestStatus.PENDING);
    }

    /**
     * Admin duyệt yêu cầu
     */
    @Transactional
    public UserRequest approveRequest(Long requestId, String adminUsername, String adminNote) {
        UserRequest request = userRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        if (request.getStatus() != UserRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Yêu cầu này đã được xử lý");
        }

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin"));

        request.setStatus(UserRequest.RequestStatus.APPROVED);
        request.setApprovedBy(admin);
        request.setApprovedAt(LocalDateTime.now());
        request.setAdminNote(adminNote);

        UserRequest savedRequest = userRequestRepository.save(request);
        log.info("Request {} approved by admin: {}", requestId, adminUsername);

        // Xử lý yêu cầu ngay sau khi duyệt (chạy async)
        // Nếu muốn tách thành 2 bước (APPROVED -> COMPLETED), comment dòng dưới
        // và để scheduler hoặc admin trigger manual
        processApprovedRequest(savedRequest);

        return savedRequest;
    }

    /**
     * Admin từ chối yêu cầu
     */
    @Transactional
    public UserRequest rejectRequest(Long requestId, String adminUsername, String adminNote) {
        UserRequest request = userRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        if (request.getStatus() != UserRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Yêu cầu này đã được xử lý");
        }

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin"));

        request.setStatus(UserRequest.RequestStatus.REJECTED);
        request.setApprovedBy(admin);
        request.setApprovedAt(LocalDateTime.now());
        request.setAdminNote(adminNote);

        UserRequest savedRequest = userRequestRepository.save(request);
        log.info("Request {} rejected by admin: {}", requestId, adminUsername);

        // Gửi email thông báo từ chối
        processRejectedRequest(savedRequest);

        return savedRequest;
    }

    /**
     * Xử lý yêu cầu đã được duyệt
     */
    @Async
    protected void processApprovedRequest(UserRequest request) {
        try {
            if (request.getRequestType() == UserRequest.RequestType.EXPORT_DATA) {
                processDataExportRequest(request);
            } else if (request.getRequestType() == UserRequest.RequestType.DELETE_ACCOUNT) {
                processAccountDeletionRequest(request);
            }
        } catch (Exception e) {
            log.error("Error processing approved request {}", request.getId(), e);
        }
    }

    /**
     * Xử lý yêu cầu xuất dữ liệu
     */
    private void processDataExportRequest(UserRequest request) {
        try {
            log.info("Processing data export request for user: {}", request.getUser().getUsername());

            // Tạo PDF
            String pdfPath = dataExportService.exportUserData(request.getUser().getId());

            // Cập nhật request
            request.setFilePath(pdfPath);

            // Gửi email với PDF
            emailService.sendDataExportEmail(
                    request.getUser().getEmail(),
                    request.getUser().getUsername(),
                    pdfPath);

            // Cập nhật trạng thái
            request.setStatus(UserRequest.RequestStatus.COMPLETED);
            request.setEmailSentAt(LocalDateTime.now());
            userRequestRepository.save(request);

            log.info("Data export completed and sent to: {}", request.getUser().getEmail());

        } catch (Exception e) {
            log.error("Error processing data export for request {}", request.getId(), e);
            throw new RuntimeException("Lỗi khi xuất dữ liệu: " + e.getMessage(), e);
        }
    }

    /**
     * Xử lý yêu cầu xóa tài khoản
     */
    private void processAccountDeletionRequest(UserRequest request) {
        try {
            log.info("Processing account deletion request for user: {}", request.getUser().getUsername());

            // Gửi email thông báo
            emailService.sendAccountDeletionNotification(
                    request.getUser().getEmail(),
                    request.getUser().getUsername());

            // Cập nhật trạng thái
            request.setStatus(UserRequest.RequestStatus.COMPLETED);
            request.setEmailSentAt(LocalDateTime.now());
            userRequestRepository.save(request);

            // Lưu ý: Thực tế xóa tài khoản nên được thực hiện sau 24-48h
            // để user có thời gian hủy bỏ nếu nhầm lẫn
            // Có thể tạo một scheduled task để xóa các tài khoản đã được approved

            log.info("Account deletion notification sent to: {}", request.getUser().getEmail());

        } catch (Exception e) {
            log.error("Error processing account deletion for request {}", request.getId(), e);
            throw new RuntimeException("Lỗi khi xử lý xóa tài khoản: " + e.getMessage(), e);
        }
    }

    /**
     * Xử lý yêu cầu bị từ chối
     */
    private void processRejectedRequest(UserRequest request) {
        try {
            emailService.sendRequestRejectionEmail(
                    request.getUser().getEmail(),
                    request.getUser().getUsername(),
                    request.getRequestType().name(),
                    request.getAdminNote());
            log.info("Rejection email sent to: {}", request.getUser().getEmail());
        } catch (Exception e) {
            log.error("Error sending rejection email for request {}", request.getId(), e);
        }
    }

    /**
     * Thực sự xóa tài khoản (được gọi bởi scheduled task sau khi đã thông báo)
     */
    @Transactional
    public void deleteUserAccount(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            // Xóa tất cả dữ liệu liên quan đến user
            // Cascade delete sẽ xử lý transactions, categories, budgets, etc.
            userRepository.delete(user);

            log.info("User account deleted: {}", user.getUsername());

        } catch (Exception e) {
            log.error("Error deleting user account {}", userId, e);
            throw new RuntimeException("Lỗi khi xóa tài khoản: " + e.getMessage(), e);
        }
    }

    /**
     * Dọn dẹp các file PDF export cũ
     */
    public void cleanupOldExports() {
        try {
            dataExportService.cleanupOldExports(exportRetentionDays);
            log.info("Cleaned up old export files older than {} days", exportRetentionDays);
        } catch (Exception e) {
            log.error("Error cleaning up old exports", e);
        }
    }

    /**
     * Lấy thống kê yêu cầu
     */
    @Transactional(readOnly = true)
    public List<Object[]> getRequestStatistics() {
        return userRequestRepository.getRequestStatistics();
    }
}
