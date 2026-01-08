package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tạo giao dịch mới")
public class TransactionRequest {

    @Schema(description = "Nguồn giao dịch (VCB, TCB, CASH, MOMO...)", example = "VCB")
    private String transactionSource;

    @Schema(description = "ID danh mục (tùy chọn - nếu không có sẽ tự động phân loại bằng AI)", example = "5")
    private Long categoryId;

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    @Schema(description = "Số tiền giao dịch", example = "150000", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotNull(message = "Loại giao dịch không được để trống")
    @Schema(description = "Loại giao dịch: INCOME hoặc EXPENSE", example = "EXPENSE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(description = "Mô tả giao dịch (bao gồm tên cửa hàng/merchant)", example = "Grab đi làm sáng")
    private String description;

    @NotNull(message = "Ngày giao dịch không được để trống")
    @Schema(description = "Ngày giờ giao dịch (ISO 8601)", example = "2025-11-20T08:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime transactionDate;

    @Builder.Default
    @Schema(description = "Giao dịch tự động (từ SMS)", example = "false", defaultValue = "false")
    private Boolean isAuto = false;

    @Schema(description = "Ghi chú thêm", example = "Đi làm mưa to")
    private String notes;

    @Schema(description = "SHA-256 hash của SMS (cho giao dịch tự động)", example = "a1b2c3d4e5f6...")
    private String smsContent;

    @Schema(description = "Gợi ý từ merchant trong SMS để AI/Rule phân loại (KHÔNG lưu vào DB)", example = "GRAB, Shopee...")
    private String merchantHint;
}
