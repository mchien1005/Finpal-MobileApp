package com.example.backend.service;

import com.example.backend.model.AccountDeletionLog;
import com.example.backend.model.User;
import com.example.backend.model.UserRequest;
import com.example.backend.repository.AccountDeletionLogRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRequestService {

    private final UserRequestRepository userRequestRepository;
    private final UserRepository userRepository;
    private final UserDataExportService dataExportService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final AccountDeletionLogRepository accountDeletionLogRepository;

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
     * Lấy ID người dùng từ username
     */
    public Long getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
        return user.getId();
    }

    /**
     * Lấy tất cả yêu cầu (cho admin) với phân trang
     */
    @Transactional(readOnly = true)
    public Page<UserRequest> getAllRequests(Pageable pageable) {
        return userRequestRepository.findAll(pageable);
    }

    /**
     * Lấy yêu cầu theo ID
     * 
     * @param requestId ID của yêu cầu
     * @return UserRequest
     * @throws RuntimeException nếu không tìm thấy
     */
    @Transactional(readOnly = true)
    public UserRequest getRequestById(Long requestId) {
        return userRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu với ID: " + requestId));
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

        // Sau 3 giây sẽ tự động xử lý và chuyển sang COMPLETED
        final Long finalRequestId = requestId;
        CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS).execute(() -> {
            try {
                log.info("⏳ Processing request {} after 3s delay...", finalRequestId);
                processApprovedRequestById(finalRequestId);
            } catch (Exception e) {
                log.error("Error processing request {} after delay: {}", finalRequestId, e.getMessage());
            }
        });

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
     * Xử lý yêu cầu đã được duyệt (theo ID)
     * Dùng cho delayed execution vì không thể pass entity detached vào lambda
     */
    @Transactional
    public void processApprovedRequestById(Long requestId) {
        // Sử dụng findByIdWithUser để load User cùng với UserRequest
        // Tránh lỗi LazyInitializationException khi chạy async
        UserRequest request = userRequestRepository.findByIdWithUser(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu: " + requestId));

        // Chỉ xử lý nếu đang ở trạng thái APPROVED
        if (request.getStatus() != UserRequest.RequestStatus.APPROVED) {
            log.warn("Request {} is not in APPROVED status, skipping processing", requestId);
            return;
        }

        processApprovedRequest(request);
    }

    /**
     * Xử lý yêu cầu đã được duyệt
     * Chạy async trong thread riêng, tạo transaction mới để load entities
     */
    @Async
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void processApprovedRequest(UserRequest request) {
        try {
            // Reload request với User đã được fetch để tránh LazyInitializationException
            Long requestId = request.getId();
            UserRequest freshRequest = userRequestRepository.findByIdWithUser(requestId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu: " + requestId));

            if (freshRequest.getRequestType() == UserRequest.RequestType.EXPORT_DATA) {
                processDataExportRequest(freshRequest);
            } else if (freshRequest.getRequestType() == UserRequest.RequestType.DELETE_ACCOUNT) {
                processAccountDeletionRequest(freshRequest);
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
     * Xử lý yêu cầu xóa tài khoản khi admin phê duyệt
     * KHÔNG xóa ngay - chỉ gửi email thông báo và đặt lịch xóa sau 24h
     * Người dùng hoặc admin có thể hủy trong 24h này
     */
    private void processAccountDeletionRequest(UserRequest request) {
        User user = request.getUser();
        String username = user.getUsername();
        String userEmail = user.getEmail();

        try {
            log.info("📋 Processing account deletion approval for user: {} (ID: {})", username, user.getId());

            // Bước 1: Gửi email thông báo cho người dùng
            try {
                emailService.sendAccountDeletionApprovalNotification(userEmail, username);
                request.setEmailSentAt(LocalDateTime.now());
                log.info("📧 Account deletion approval email sent to: {}", userEmail);
            } catch (Exception emailError) {
                log.warn("⚠️ Failed to send deletion approval email to {}: {}",
                        userEmail, emailError.getMessage());
                // Tiếp tục xử lý dù email thất bại
            }

            // Bước 2: Đặt thời gian xóa dự kiến (24 giờ sau)
            LocalDateTime scheduledTime = LocalDateTime.now().plusHours(24);
            request.setScheduledDeletionAt(scheduledTime);

            // Giữ trạng thái APPROVED - scheduled task sẽ xử lý việc xóa
            userRequestRepository.save(request);

            log.info("⏰ Account deletion scheduled for user: {} at {}", username, scheduledTime);
            log.info("ℹ️ User or admin can cancel this request within 24 hours");

        } catch (Exception e) {
            log.error("❌ Error processing account deletion approval for request {}: {}",
                    request.getId(), e.getMessage(), e);
            throw new RuntimeException("Lỗi khi xử lý phê duyệt xóa tài khoản: " + e.getMessage(), e);
        }
    }

    /**
     * Thực hiện xóa tài khoản (gọi bởi scheduled task sau 24h)
     */
    @Transactional
    public void executeAccountDeletion(Long requestId) {
        UserRequest request = userRequestRepository.findByIdWithUser(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu: " + requestId));

        // Kiểm tra trạng thái - chỉ xóa nếu vẫn đang APPROVED
        if (request.getStatus() != UserRequest.RequestStatus.APPROVED) {
            log.info("Request {} is not in APPROVED status (current: {}), skipping deletion",
                    requestId, request.getStatus());
            return;
        }

        User user = request.getUser();
        Long userId = user.getId();
        String username = user.getUsername();
        String userEmail = user.getEmail();
        String fullName = user.getFullName();

        // Thông tin admin phê duyệt
        User approvedBy = request.getApprovedBy();
        Long approvedByAdminId = approvedBy != null ? approvedBy.getId() : null;
        String approvedByAdminUsername = approvedBy != null ? approvedBy.getUsername() : null;

        try {
            log.info("🗑️ Executing scheduled account deletion for user: {} (ID: {})", username, userId);

            // Bước 1: Xóa tất cả các UserRequest khác của user (trừ request hiện tại)
            userRequestRepository.deleteByUserIdAndIdNot(userId, requestId);

            // Bước 2: XÓA TÀI KHOẢN NGƯỜI DÙNG VÀ TẤT CẢ DỮ LIỆU LIÊN QUAN
            log.info("🔴 DELETING user account and all related data for: {} (ID: {})", username, userId);
            userRepository.delete(user);

            // Bước 3: Xóa request hiện tại
            userRequestRepository.deleteById(requestId);

            // Bước 4: LƯU LỊCH SỬ XÓA TÀI KHOẢN ĐỂ AUDIT
            AccountDeletionLog auditLog = new AccountDeletionLog();
            auditLog.setDeletedUserId(userId);
            auditLog.setUsername(username);
            auditLog.setEmail(userEmail);
            auditLog.setFullName(fullName);
            auditLog.setRequestId(requestId);
            auditLog.setRequestReason(request.getReason());
            auditLog.setRequestCreatedAt(request.getCreatedAt());
            auditLog.setApprovedByAdminId(approvedByAdminId);
            auditLog.setApprovedByAdminUsername(approvedByAdminUsername);
            auditLog.setAdminNote(request.getAdminNote());
            auditLog.setApprovedAt(request.getApprovedAt());
            auditLog.setDeletedAt(LocalDateTime.now());
            auditLog.setEmailNotificationSent(request.getEmailSentAt() != null);
            auditLog.setDeletionStatus("COMPLETED");

            accountDeletionLogRepository.save(auditLog);
            log.info("📝 Audit log saved for deleted account: {} (ID: {})", username, userId);

            log.info("✅ ACCOUNT DELETED SUCCESSFULLY: {} (ID: {})", username, userId);

        } catch (Exception e) {
            log.error("❌ Error executing account deletion for request {}: {}",
                    requestId, e.getMessage(), e);

            // Lưu audit log với trạng thái FAILED
            try {
                AccountDeletionLog failedLog = new AccountDeletionLog();
                failedLog.setDeletedUserId(userId);
                failedLog.setUsername(username);
                failedLog.setEmail(userEmail);
                failedLog.setFullName(fullName);
                failedLog.setRequestId(requestId);
                failedLog.setRequestReason(request.getReason());
                failedLog.setRequestCreatedAt(request.getCreatedAt());
                failedLog.setApprovedByAdminId(approvedByAdminId);
                failedLog.setApprovedByAdminUsername(approvedByAdminUsername);
                failedLog.setAdminNote(request.getAdminNote());
                failedLog.setApprovedAt(request.getApprovedAt());
                failedLog.setDeletedAt(LocalDateTime.now());
                failedLog.setEmailNotificationSent(request.getEmailSentAt() != null);
                failedLog.setDeletionStatus("FAILED");
                failedLog.setErrorMessage(e.getMessage());

                accountDeletionLogRepository.save(failedLog);
            } catch (Exception auditError) {
                log.error("Failed to save audit log", auditError);
            }

            throw new RuntimeException("Lỗi khi xóa tài khoản: " + e.getMessage(), e);
        }
    }

    /**
     * Hủy yêu cầu xóa tài khoản đã được phê duyệt (trong 24h)
     */
    @Transactional
    public UserRequest cancelApprovedDeletion(Long requestId, Long adminId, String cancelReason) {
        UserRequest request = userRequestRepository.findByIdWithUser(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu: " + requestId));

        // Chỉ có thể hủy yêu cầu DELETE_ACCOUNT đang ở trạng thái APPROVED
        if (request.getRequestType() != UserRequest.RequestType.DELETE_ACCOUNT) {
            throw new RuntimeException("Chỉ có thể hủy yêu cầu xóa tài khoản");
        }

        if (request.getStatus() != UserRequest.RequestStatus.APPROVED) {
            throw new RuntimeException(
                    "Yêu cầu không ở trạng thái đã duyệt. Trạng thái hiện tại: " + request.getStatus());
        }

        // Kiểm tra thời gian - chỉ hủy được nếu chưa đến thời gian xóa
        if (request.getScheduledDeletionAt() != null &&
                LocalDateTime.now().isAfter(request.getScheduledDeletionAt())) {
            throw new RuntimeException("Đã quá thời gian cho phép hủy. Thời gian xóa dự kiến đã qua.");
        }

        // Cập nhật trạng thái
        request.setStatus(UserRequest.RequestStatus.CANCELLED);
        request.setAdminNote((request.getAdminNote() != null ? request.getAdminNote() + " | " : "")
                + "Đã hủy: " + (cancelReason != null ? cancelReason : "Không có lý do"));
        request.setScheduledDeletionAt(null);

        userRequestRepository.save(request);

        // Gửi email thông báo hủy cho người dùng
        try {
            emailService.sendAccountDeletionCancelledNotification(
                    request.getUser().getEmail(),
                    request.getUser().getUsername());
            log.info("📧 Deletion cancellation email sent to: {}", request.getUser().getEmail());
        } catch (Exception e) {
            log.warn("Failed to send cancellation email", e);
        }

        log.info("🚫 Account deletion request {} cancelled. User: {}",
                requestId, request.getUser().getUsername());

        return request;
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
