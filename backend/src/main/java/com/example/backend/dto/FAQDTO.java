package com.example.backend.dto;

import com.example.backend.model.FAQ.FAQCategory;
import com.example.backend.model.FAQ.FAQStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FAQDTO {

    private Long id;
    private String faqCode;
    private String question;
    private String answer;
    private FAQCategory category;
    private Integer viewCount;
    private Integer helpfulCount;
    private Integer notHelpfulCount;
    private Integer displayOrder;
    private FAQStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Request DTO for create
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String faqCode;
        private String question;
        private String answer;
        private FAQCategory category;
        private Integer displayOrder;
        private FAQStatus status;
    }

    // Request DTO for update
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String question;
        private String answer;
        private FAQCategory category;
        private Integer displayOrder;
        private FAQStatus status;
    }

    // Request cho feedback (helpful/not helpful)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackRequest {
        private boolean helpful;
    }
}
