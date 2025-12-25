package com.example.backend.scheduler;

import com.example.backend.model.UserRequest;
import com.example.backend.repository.UserRequestRepository;
import com.example.backend.service.UserRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task để thực hiện xóa tài khoản sau 24h
 * Chạy mỗi 5 phút để kiểm tra các yêu cầu đã đến thời gian xóa
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountDeletionScheduler {

    private final UserRequestRepository userRequestRepository;
    private final UserRequestService userRequestService;

    /**
     * Kiểm tra và xóa các tài khoản đã đến thời gian scheduled
     * Chạy mỗi 5 phút
     */
    @Scheduled(fixedRate = 300000) // 5 phút = 300,000 ms
    public void executeScheduledDeletions() {
        log.debug("🔍 Checking for scheduled account deletions...");

        try {
            LocalDateTime now = LocalDateTime.now();

            // Tìm các yêu cầu xóa đã đến thời gian scheduled
            List<UserRequest> pendingDeletions = userRequestRepository
                    .findDeletionRequestsReadyToExecute(now);

            if (pendingDeletions.isEmpty()) {
                log.debug("No pending deletions found");
                return;
            }

            log.info("📋 Found {} deletion request(s) ready to execute", pendingDeletions.size());

            for (UserRequest request : pendingDeletions) {
                try {
                    log.info("⏰ Executing scheduled deletion for request {} (user: {})",
                            request.getId(), request.getUser().getUsername());

                    userRequestService.executeAccountDeletion(request.getId());

                    log.info("✅ Successfully deleted account for request {}", request.getId());

                } catch (Exception e) {
                    log.error("❌ Failed to execute deletion for request {}: {}",
                            request.getId(), e.getMessage(), e);
                    // Tiếp tục với request tiếp theo, không dừng lại
                }
            }

        } catch (Exception e) {
            log.error("❌ Error in scheduled deletion task", e);
        }
    }

    /**
     * Log trạng thái các yêu cầu xóa đang chờ
     * Chạy mỗi giờ
     */
    @Scheduled(fixedRate = 3600000) // 1 giờ = 3,600,000 ms
    public void logPendingDeletions() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime next24Hours = now.plusHours(24);

            // Đếm số yêu cầu sẽ được xóa trong 24h tới
            List<UserRequest> upcoming = userRequestRepository
                    .findDeletionRequestsReadyToExecute(next24Hours);

            if (!upcoming.isEmpty()) {
                log.info("📊 Status: {} account deletion(s) scheduled within next 24 hours",
                        upcoming.size());

                for (UserRequest request : upcoming) {
                    log.info("  - User: {} | Scheduled at: {} | Time remaining: {} hours",
                            request.getUser().getUsername(),
                            request.getScheduledDeletionAt(),
                            java.time.Duration.between(now, request.getScheduledDeletionAt()).toHours());
                }
            }

        } catch (Exception e) {
            log.error("Error logging pending deletions", e);
        }
    }
}
