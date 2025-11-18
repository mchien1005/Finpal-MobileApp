package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "Account ID không được để trống")
    private Long accountId;

    private Long categoryId; // Optional - có thể auto-categorize

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Loại giao dịch không được để trống")
    private String type; // INCOME or EXPENSE

    private String merchant;

    private String description;

    @NotNull(message = "Ngày giao dịch không được để trống")
    private LocalDateTime transactionDate;

    private Boolean isAuto = false;

    private String notes;

    private String tags;
}
