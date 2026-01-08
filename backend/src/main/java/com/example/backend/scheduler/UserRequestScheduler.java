package com.example.backend.scheduler;

import com.example.backend.model.UserRequest;
import com.example.backend.repository.UserRequestRepository;
import com.example.backend.service.UserRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRequestScheduler {

    private final UserRequestRepository userRequestRepository;
    private final UserRequestService userRequestService;

    /**
     * Scheduled task để xóa tài khoản sau 24h kể từ khi được duyệt
     * Chạy mỗi 6 giờ
     */
    @Scheduled(cron = "0 0 */6 * * *") // Chạy vào 00:00, 06:00, 12:00, 18:00
    @Transactional
    public void processAccountDeletionRequests() {
        log.info("Starting scheduled task: process account deletion requests");

        try {
            // Lấy tất cả yêu cầu DELETE_ACCOUNT đã COMPLETED và đã qua 24h
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);

            List<UserRequest> requests = userRequestRepository.findCompletedDeletionRequestsBeforeTime(cutoffTime);

            log.info("Found {} account deletion requests to process", requests.size());

            for (UserRequest request : requests) {
                try {
                    log.info("Processing account deletion for user: {} (request ID: {})",
                            request.getUser().getUsername(), request.getId());

                    // Xóa tài khoản
                    userRequestService.deleteUserAccount(request.getUser().getId());

                    // Xóa luôn request record
                    userRequestRepository.delete(request);

                    log.info("Successfully deleted account for user: {}", request.getUser().getUsername());

                } catch (Exception e) {
                    log.error("Error deleting account for request ID: {}", request.getId(), e);
                    // Tiếp tục xử lý các request khác
                }
            }

            log.info("Completed scheduled task: processed {} account deletions", requests.size());

        } catch (Exception e) {
            log.error("Error in account deletion scheduled task", e);
        }
    }

    /**
     * Scheduled task để dọn dẹp các file PDF export cũ
     * Chạy mỗi ngày lúc 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldExportFiles() {
        log.info("Starting scheduled task: cleanup old export files");

        try {
            userRequestService.cleanupOldExports();
            log.info("Completed scheduled task: cleanup old export files");

        } catch (Exception e) {
            log.error("Error in cleanup scheduled task", e);
        }
    }

    /**
     * Scheduled task để gửi nhắc nhở admin về các yêu cầu chưa xử lý
     * Chạy mỗi ngày lúc 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendPendingRequestsReminder() {
        log.info("Starting scheduled task: send pending requests reminder");

        try {
            long pendingCount = userRequestService.countPendingRequests();

            if (pendingCount > 0) {
                log.warn("There are {} pending user requests waiting for admin approval", pendingCount);
                // TODO: Gửi email/notification cho admin
                // hoặc tích hợp với hệ thống notification có sẵn
            } else {
                log.info("No pending requests");
            }

        } catch (Exception e) {
            log.error("Error in pending requests reminder task", e);
        }
    }

    /**
     * Scheduled task để cảnh báo về các yêu cầu xóa tài khoản sắp được thực thi
     * Chạy mỗi 3 giờ
     */
    @Scheduled(cron = "0 0 */3 * * *")
    public void sendAccountDeletionWarning() {
        log.info("Starting scheduled task: send account deletion warning");

        try {
            // Lấy các yêu cầu xóa tài khoản sẽ được thực thi trong 3 giờ tới
            LocalDateTime nowPlus3Hours = LocalDateTime.now().plusHours(3);
            LocalDateTime nowMinus24Hours = LocalDateTime.now().minusHours(24);

            List<UserRequest> upcomingDeletions = userRequestRepository
                    .findCompletedDeletionRequestsBetweenTimes(nowMinus24Hours, nowPlus3Hours);

            if (!upcomingDeletions.isEmpty()) {
                log.warn("Found {} accounts scheduled for deletion in next 3 hours", upcomingDeletions.size());

                for (UserRequest request : upcomingDeletions) {
                    long hoursUntilDeletion = java.time.Duration.between(
                            LocalDateTime.now(),
                            request.getEmailSentAt().plusHours(24)
                    ).toHours();

                    log.warn("Account '{}' will be deleted in approximately {} hours",
                            request.getUser().getUsername(), hoursUntilDeletion);
                }
            }

        } catch (Exception e) {
            log.error("Error in account deletion warning task", e);
        }
    }
}
