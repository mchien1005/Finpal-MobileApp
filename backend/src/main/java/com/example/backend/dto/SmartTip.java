package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho Smart Tip từ AI Backend
 * Đại diện cho một gợi ý/tip thông minh từ hệ thống AI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartTip {
    
    /**
     * ID duy nhất của tip
     */
    private String tipId;
    
    /**
     * Loại tip: 'saving', 'warning', 'achievement', 'habit', 'seasonal', 'general'
     */
    private String tipType;
    
    /**
     * Emoji/icon đại diện
     */
    private String icon;
    
    /**
     * Tiêu đề ngắn gọn
     */
    private String title;
    
    /**
     * Nội dung chi tiết của tip
     */
    private String content;
    
    /**
     * Độ ưu tiên 1-5 (5 = quan trọng nhất)
     */
    private Integer priority;
    
    /**
     * Danh mục liên quan (nếu có)
     */
    private String category;
    
    /**
     * Text cho nút hành động (nếu có)
     */
    private String actionText;
    
    /**
     * URL điều hướng khi click (nếu có)
     */
    private String actionUrl;
    
    /**
     * Tip có được cá nhân hóa không
     */
    private Boolean isPersonalized;
}
