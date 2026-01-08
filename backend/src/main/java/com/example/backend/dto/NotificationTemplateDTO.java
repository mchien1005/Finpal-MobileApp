package com.example.backend.dto;

import com.example.backend.model.NotificationTemplate.TemplateStatus;
import com.example.backend.model.NotificationTemplate.TemplateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDTO {

    private Long id;
    private String templateCode;
    private String title;
    private String messageTemplate;
    private TemplateType type;
    private Integer sentCount;
    private TemplateStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Request DTO for create/update
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String templateCode;
        private String title;
        private String messageTemplate;
        private TemplateType type;
        private TemplateStatus status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String messageTemplate;
        private TemplateType type;
        private TemplateStatus status;
    }

    // Request để gửi notification từ template
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendRequest {
        private Long templateId;
        private Long[] userIds; // null = gửi cho tất cả users
        private java.util.Map<String, String> placeholders; // {amount: "500,000", category: "Ăn uống"}
    }
}
