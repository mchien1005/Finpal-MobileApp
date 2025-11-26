package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tạo tài khoản ngân hàng/ví")
public class AccountRequest {

    @NotBlank(message = "Bank name is required")
    @Schema(description = "Tên ngân hàng", example = "Vietcombank", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bankName;

    @NotBlank(message = "Account name is required")
    @Schema(description = "Tên tài khoản", example = "Tài khoản chính", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountName;

    @NotNull(message = "Account type is required")
    @Schema(description = "Loại tài khoản: BANK, CREDIT_CARD, CASH, E_WALLET", example = "BANK", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountType;

    @NotNull(message = "Initial balance is required")
    @PositiveOrZero(message = "Balance must be zero or positive")
    @Schema(description = "Số dư ban đầu", example = "5000000", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal balance;

    @Schema(description = "Số tài khoản (tùy chọn)", example = "1234567890")
    private String accountNumber;

    @Schema(description = "Đơn vị tiền tệ (mặc định VND)", example = "VND", defaultValue = "VND")
    private String currency;
}
