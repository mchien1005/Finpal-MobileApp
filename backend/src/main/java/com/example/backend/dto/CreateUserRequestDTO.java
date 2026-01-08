package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequestDTO {
    
    @NotNull(message = "Loại yêu cầu không được để trống")
    private String requestType; // EXPORT_DATA hoặc DELETE_ACCOUNT
    
    private String reason; // Lý do yêu cầu (optional)
}
