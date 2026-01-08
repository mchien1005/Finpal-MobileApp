package com.example.backend.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho biểu đồ tăng trưởng người dùng theo tháng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dữ liệu tăng trưởng người dùng theo tháng (dùng cho biểu đồ đường)")
public class UserGrowthDTO {

    @Schema(description = "Danh sách dữ liệu theo từng tháng")
    private List<MonthlyUserData> data;

    @Schema(description = "Tổng số người dùng hiện tại", example = "12543")
    private Long totalUsers;

    @Schema(description = "Tổng số người dùng đang hoạt động", example = "9876")
    private Long totalActiveUsers;

    @Schema(description = "Tỷ lệ tăng trưởng trung bình (%)", example = "15.3")
    private Double growthRate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Dữ liệu người dùng theo tháng")
    public static class MonthlyUserData {

        @Schema(description = "Ký hiệu tháng ngắn gọn", example = "T1")
        private String month;

        @Schema(description = "Label đầy đủ của tháng", example = "Tháng 1/2026")
        private String monthLabel;

        @Schema(description = "Tổng số người dùng cuối tháng", example = "10500")
        private Long totalUsers;

        @Schema(description = "Số người dùng active trong tháng", example = "7350")
        private Long activeUsers;

        @Schema(description = "Số người dùng mới đăng ký trong tháng", example = "450")
        private Long newUsers;
    }
}
