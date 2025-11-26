package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Đăng nhập request")
public class LoginRequest {
    @JsonProperty(value = "username", required = true)
    @Schema(description = "Tên đăng nhập", example = "demo", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @JsonProperty(value = "password", required = true)
    @Schema(description = "Mật khẩu", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
