package com.example.backend.dto;

import com.example.backend.model.UserRequest;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    private Long id;
    private Long userId;
    private String username;
    private String userEmail;
    private String requestType;
    private String status;
    private String reason;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private Long approvedByUserId;
    private String approvedByUsername;
    private String filePath;
    private LocalDateTime emailSentAt;

    /**
     * Convert Entity to DTO
     */
    public static UserRequestDTO fromEntity(UserRequest request) {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setId(request.getId());
        dto.setUserId(request.getUser().getId());
        dto.setUsername(request.getUser().getUsername());
        dto.setUserEmail(request.getUser().getEmail());
        dto.setRequestType(request.getRequestType().name());
        dto.setStatus(request.getStatus().name());
        dto.setReason(request.getReason());
        dto.setAdminNote(request.getAdminNote());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setApprovedAt(request.getApprovedAt());
        
        if (request.getApprovedBy() != null) {
            dto.setApprovedByUserId(request.getApprovedBy().getId());
            dto.setApprovedByUsername(request.getApprovedBy().getUsername());
        }
        
        dto.setFilePath(request.getFilePath());
        dto.setEmailSentAt(request.getEmailSentAt());
        
        return dto;
    }
}
