package com.example.backend.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho biểu đồ phân bổ ngân hàng
 * Thống kê số lượng users và giao dịch theo từng ngân hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dữ liệu phân bổ giao dịch theo ngân hàng (dùng cho biểu đồ cột ngang)")
public class BankDistributionDTO {

    @Schema(description = "Danh sách phân bổ theo ngân hàng")
    private List<BankData> banks;

    @Schema(description = "Tổng số người dùng có giao dịch SMS", example = "8543")
    private Long totalUsers;

    @Schema(description = "Tổng số giao dịch từ SMS", example = "125678")
    private Long totalTransactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Dữ liệu giao dịch theo ngân hàng")
    public static class BankData {

        @Schema(description = "Mã ngân hàng", example = "VCB", allowableValues = { "VCB", "TCB", "ACB", "VPB", "MBB",
                "BID", "CTG", "SHB", "TPB", "VIB", "MSB", "EIB", "STB", "HDB", "MOMO", "ZALOPAY", "VNPAY" })
        private String bankCode;

        @Schema(description = "Tên đầy đủ của ngân hàng", example = "Vietcombank")
        private String bankName;

        @Schema(description = "Số người dùng sử dụng ngân hàng này", example = "3245")
        private Long userCount;

        @Schema(description = "Phần trăm người dùng sử dụng", example = "25.9")
        private Double percentage;

        @Schema(description = "Số giao dịch từ ngân hàng này", example = "45678")
        private Long transactionCount;
    }
}
