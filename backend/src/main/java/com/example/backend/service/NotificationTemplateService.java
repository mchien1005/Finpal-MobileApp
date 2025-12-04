package com.example.backend.service;

import com.example.backend.dto.NotificationTemplateDTO;
import com.example.backend.model.Notification;
import com.example.backend.model.NotificationTemplate;
import com.example.backend.model.NotificationTemplate.TemplateStatus;
import com.example.backend.model.User;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.NotificationTemplateRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service quản lý Notification Templates (Admin)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Lấy tất cả templates
     */
    @Transactional(readOnly = true)
    public List<NotificationTemplateDTO> getAllTemplates() {
        return templateRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy templates theo status
     */
    @Transactional(readOnly = true)
    public List<NotificationTemplateDTO> getTemplatesByStatus(TemplateStatus status) {
        return templateRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy template theo ID
     */
    @Transactional(readOnly = true)
    public NotificationTemplateDTO getTemplateById(Long id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));
        return convertToDTO(template);
    }

    /**
     * Tạo template mới
     */
    @Transactional
    public NotificationTemplateDTO createTemplate(NotificationTemplateDTO.CreateRequest request, Long adminId) {
        // Check duplicate code
        if (templateRepository.existsByTemplateCode(request.getTemplateCode())) {
            throw new RuntimeException("Template code already exists: " + request.getTemplateCode());
        }

        NotificationTemplate template = new NotificationTemplate();
        template.setTemplateCode(request.getTemplateCode());
        template.setTitle(request.getTitle());
        template.setMessageTemplate(request.getMessageTemplate());
        template.setType(request.getType() != null ? request.getType() : NotificationTemplate.TemplateType.INFO);
        template.setStatus(request.getStatus() != null ? request.getStatus() : TemplateStatus.DRAFT);
        template.setSentCount(0);
        template.setCreatedBy(adminId);

        template = templateRepository.save(template);
        log.info("Created notification template: {} by admin {}", template.getTemplateCode(), adminId);

        return convertToDTO(template);
    }

    /**
     * Cập nhật template
     */
    @Transactional
    public NotificationTemplateDTO updateTemplate(Long id, NotificationTemplateDTO.UpdateRequest request) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));

        if (request.getTitle() != null) {
            template.setTitle(request.getTitle());
        }
        if (request.getMessageTemplate() != null) {
            template.setMessageTemplate(request.getMessageTemplate());
        }
        if (request.getType() != null) {
            template.setType(request.getType());
        }
        if (request.getStatus() != null) {
            template.setStatus(request.getStatus());
        }

        template = templateRepository.save(template);
        log.info("Updated notification template: {}", template.getTemplateCode());

        return convertToDTO(template);
    }

    /**
     * Xóa template
     */
    @Transactional
    public void deleteTemplate(Long id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));

        templateRepository.delete(template);
        log.info("Deleted notification template: {}", template.getTemplateCode());
    }

    /**
     * Gửi notification từ template cho users
     */
    @Transactional
    public int sendNotificationFromTemplate(NotificationTemplateDTO.SendRequest request, Long adminId) {
        NotificationTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found"));

        if (template.getStatus() != TemplateStatus.ACTIVE) {
            throw new RuntimeException("Template is not active");
        }

        // Get target users
        List<User> targetUsers;
        if (request.getUserIds() != null && request.getUserIds().length > 0) {
            targetUsers = userRepository.findAllById(List.of(request.getUserIds()));
        } else {
            // Send to all users
            targetUsers = userRepository.findAll();
        }

        // Replace placeholders in message
        String message = template.getMessageTemplate();
        if (request.getPlaceholders() != null) {
            for (Map.Entry<String, String> entry : request.getPlaceholders().entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        // Create notifications
        int sentCount = 0;
        for (User user : targetUsers) {
            try {
                Notification notification = new Notification();
                notification.setUserId(user.getId());
                notification.setType(template.getType().name());
                notification.setTitle(template.getTitle());
                notification.setContent(message);
                notification.setIsRead(false);
                notification.setPriority(mapTypeToPriority(template.getType()));

                notificationRepository.save(notification);
                sentCount++;
            } catch (Exception e) {
                log.error("Failed to send notification to user {}: {}", user.getId(), e.getMessage());
            }
        }

        // Update sent count
        template.setSentCount(template.getSentCount() + sentCount);
        templateRepository.save(template);

        log.info("Sent {} notifications from template {} by admin {}", sentCount, template.getTemplateCode(), adminId);

        return sentCount;
    }

    /**
     * Map template type to notification priority
     */
    private Notification.NotificationPriority mapTypeToPriority(NotificationTemplate.TemplateType type) {
        return switch (type) {
            case ALERT -> Notification.NotificationPriority.HIGH;
            case WARNING -> Notification.NotificationPriority.MEDIUM;
            case SUCCESS, INFO -> Notification.NotificationPriority.LOW;
        };
    }

    /**
     * Convert entity to DTO
     */
    private NotificationTemplateDTO convertToDTO(NotificationTemplate template) {
        return NotificationTemplateDTO.builder()
                .id(template.getId())
                .templateCode(template.getTemplateCode())
                .title(template.getTitle())
                .messageTemplate(template.getMessageTemplate())
                .type(template.getType())
                .sentCount(template.getSentCount())
                .status(template.getStatus())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    /**
     * Get statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getStatistics() {
        return Map.of(
                "total", templateRepository.count(),
                "active", templateRepository.countByStatus(TemplateStatus.ACTIVE),
                "inactive", templateRepository.countByStatus(TemplateStatus.INACTIVE),
                "draft", templateRepository.countByStatus(TemplateStatus.DRAFT));
    }
}
