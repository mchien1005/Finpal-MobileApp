package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO Response cho danh sách Smart Tips từ AI Backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartTipsResponse {
    
    /**
     * ID người dùng
     */
    private Long userId;
    
    /**
     * Danh sách các tips thông minh
     */
    private List<SmartTip> tips;
    
    /**
     * Thời điểm tạo tips (ISO format)
     */
    private String generatedAt;
    
    /**
     * Số lượng tips trả về
     */
    private Integer tipsCount;
}
