package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilter {
    private String transactionSource; // VCB, TCB, CASH, MOMO...
    private Long categoryId;
    private String type; // INCOME, EXPENSE
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isAuto;
    private Boolean isVerified;
    private String keyword; // Tìm kiếm trong description và notes
}
