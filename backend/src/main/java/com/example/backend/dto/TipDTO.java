package com.example.backend.dto;

import com.example.backend.model.Tip.TipCategory;
import com.example.backend.model.Tip.TipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipDTO {

    private Long id;
    private String tipCode;
    private String title;
    private String content;
    private TipCategory category;
    private String icon;
    private Integer viewCount;
    private Integer likeCount;
    private Integer displayOrder;
    private TipStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Request DTO for create
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String tipCode;
        private String title;
        private String content;
        private TipCategory category;
        private String icon;
        private Integer displayOrder;
        private TipStatus status;
    }

    // Request DTO for update
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String content;
        private TipCategory category;
        private String icon;
        private Integer displayOrder;
        private TipStatus status;
    }
}
