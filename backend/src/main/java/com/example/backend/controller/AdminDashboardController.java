package com.example.backend.controller;

import com.example.backend.dto.admin.*;
import com.example.backend.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Admin Dashboard
 * 
 * Cung cấp các API thống kê tổng quan hệ thống cho Admin:
 * - Tổng quan dashboard (users, transactions, AI accuracy, SMS parsing)
 * - Tăng trưởng người dùng theo tháng
 * - Khối lượng giao dịch 7 ngày
 * - Phân bổ danh mục chi tiêu
 * - Phân bổ ngân hàng
 * - Tình trạng hệ thống
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "📊 Admin Dashboard", description = """
                API thống kê tổng quan hệ thống dành cho Admin.

                **Các chức năng chính:**
                - 📈 Tổng quan: Số liệu users, transactions, AI accuracy, SMS parsing
                - 👥 Tăng trưởng users: Biểu đồ đường theo tháng
                - 💰 Khối lượng giao dịch: Biểu đồ cột 7 ngày gần nhất
                - 🥧 Phân bổ danh mục: Biểu đồ tròn chi tiêu theo category
                - 🏦 Phân bổ ngân hàng: Biểu đồ cột ngang theo bank
                - 🖥️ Tình trạng hệ thống: CPU, RAM, Database, AI Backend

                **Yêu cầu:** Đăng nhập với vai trò ADMIN
                """)
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

        private final AdminDashboardService adminDashboardService;

        /**
         * GET /api/admin/dashboard/overview
         * Lấy tổng quan dashboard cho Admin
         */
        @GetMapping("/overview")
        @Operation(summary = "Tổng quan Dashboard", description = """
                        Lấy các chỉ số tổng quan cho Admin Dashboard trong một request.

                        **Thống kê người dùng:**
                        - Tổng số users trong hệ thống
                        - Users active hôm nay (có đăng nhập/giao dịch)
                        - % tăng trưởng so với tháng trước

                        **Thống kê giao dịch:**
                        - Số lượng giao dịch hôm nay
                        - Tổng giá trị giao dịch (VND)
                        - % tăng trưởng so với hôm qua

                        **AI & SMS:**
                        - Độ chính xác AI categorization
                        - Tỷ lệ parse SMS thành công
                        - Số SMS parse thất bại hôm nay
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Thành công - Trả về thông tin tổng quan", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminDashboardOverviewDTO.class), examples = @ExampleObject(value = """
                                        {
                                            "totalUsers": 12543,
                                            "activeUsersToday": 1234,
                                            "userGrowthPercent": 12.5,
                                            "transactionsToday": 8392,
                                            "totalValueToday": 245600000,
                                            "transactionGrowthPercent": 8.2,
                                            "aiAccuracy": 94.2,
                                            "aiAccuracyChange": 2.1,
                                            "smsParsingRate": 98.7,
                                            "smsFailedToday": 128
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
                        @ApiResponse(responseCode = "403", description = "Không có quyền Admin")
        })
        public ResponseEntity<AdminDashboardOverviewDTO> getOverview() {
                AdminDashboardOverviewDTO overview = adminDashboardService.getOverview();
                return ResponseEntity.ok(overview);
        }

        /**
         * GET /api/admin/dashboard/user-growth
         * Lấy dữ liệu tăng trưởng người dùng theo tháng
         */
        @GetMapping("/user-growth")
        @Operation(summary = "Tăng trưởng người dùng", description = """
                        Lấy dữ liệu tăng trưởng người dùng theo tháng (dùng cho biểu đồ đường).

                        **Response bao gồm:**
                        - Dữ liệu theo từng tháng (total users, active users, new users)
                        - Tổng số users hiện tại
                        - Số users active
                        - Tỷ lệ tăng trưởng trung bình

                        **Ví dụ tham số:**
                        - `months=7`: Lấy 7 tháng gần nhất (mặc định)
                        - `months=12`: Lấy 12 tháng gần nhất
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserGrowthDTO.class), examples = @ExampleObject(value = """
                                        {
                                            "data": [
                                                {"month": "T7", "monthLabel": "Tháng 7/2025", "totalUsers": 8500, "activeUsers": 5950, "newUsers": 320},
                                                {"month": "T8", "monthLabel": "Tháng 8/2025", "totalUsers": 9200, "activeUsers": 6440, "newUsers": 700},
                                                {"month": "T9", "monthLabel": "Tháng 9/2025", "totalUsers": 9800, "activeUsers": 6860, "newUsers": 600},
                                                {"month": "T10", "monthLabel": "Tháng 10/2025", "totalUsers": 10500, "activeUsers": 7350, "newUsers": 700},
                                                {"month": "T11", "monthLabel": "Tháng 11/2025", "totalUsers": 11200, "activeUsers": 7840, "newUsers": 700},
                                                {"month": "T12", "monthLabel": "Tháng 12/2025", "totalUsers": 12000, "activeUsers": 8400, "newUsers": 800},
                                                {"month": "T1", "monthLabel": "Tháng 1/2026", "totalUsers": 12543, "activeUsers": 8780, "newUsers": 543}
                                            ],
                                            "totalUsers": 12543,
                                            "totalActiveUsers": 9876,
                                            "growthRate": 15.3
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
                        @ApiResponse(responseCode = "403", description = "Không có quyền Admin")
        })
        public ResponseEntity<UserGrowthDTO> getUserGrowth(
                        @Parameter(description = "Số tháng cần lấy dữ liệu (1-12)", example = "7") @RequestParam(required = false, defaultValue = "7") Integer months) {
                UserGrowthDTO growth = adminDashboardService.getUserGrowth(months);
                return ResponseEntity.ok(growth);
        }

        /**
         * GET /api/admin/dashboard/transaction-volume
         * Lấy khối lượng giao dịch theo ngày
         */
        @GetMapping("/transaction-volume")
        @Operation(summary = "Khối lượng giao dịch", description = """
                        Lấy thống kê giao dịch theo ngày (dùng cho biểu đồ cột kép).

                        **Response bao gồm:**
                        - Dữ liệu theo từng ngày (số lượng, tổng giá trị triệu VND)
                        - Tổng số giao dịch trong khoảng thời gian
                        - Tổng giá trị giao dịch (VND)
                        - Trung bình giao dịch/ngày

                        **Ví dụ tham số:**
                        - `days=7`: Lấy 7 ngày gần nhất (mặc định)
                        - `days=14`: Lấy 14 ngày gần nhất
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionVolumeDTO.class), examples = @ExampleObject(value = """
                                        {
                                            "data": [
                                                {"day": "T2", "date": "30/12/2025", "transactionCount": 1250, "totalValue": 95.50},
                                                {"day": "T3", "date": "31/12/2025", "transactionCount": 1380, "totalValue": 105.20},
                                                {"day": "T4", "date": "01/01/2026", "transactionCount": 1520, "totalValue": 118.75},
                                                {"day": "T5", "date": "02/01/2026", "transactionCount": 1650, "totalValue": 132.40},
                                                {"day": "T6", "date": "03/01/2026", "transactionCount": 1890, "totalValue": 156.80},
                                                {"day": "T7", "date": "04/01/2026", "transactionCount": 2100, "totalValue": 178.50},
                                                {"day": "CN", "date": "05/01/2026", "transactionCount": 1850, "totalValue": 145.60}
                                            ],
                                            "totalTransactions": 11640,
                                            "totalValue": 932750000,
                                            "avgDailyTransactions": 1662.86
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
                        @ApiResponse(responseCode = "403", description = "Không có quyền Admin")
        })
        public ResponseEntity<TransactionVolumeDTO> getTransactionVolume(
                        @Parameter(description = "Số ngày cần lấy dữ liệu (1-30)", example = "7") @RequestParam(required = false, defaultValue = "7") Integer days) {
                TransactionVolumeDTO volume = adminDashboardService.getTransactionVolume(days);
                return ResponseEntity.ok(volume);
        }

        /**
         * GET /api/admin/dashboard/category-distribution
         * Lấy phân bổ chi tiêu theo danh mục
         */
        @GetMapping("/category-distribution")
        @Operation(summary = "Phân bổ danh mục chi tiêu", description = """
                        Lấy thống kê chi tiêu theo danh mục (toàn hệ thống, dùng cho biểu đồ tròn).

                        **Dữ liệu lấy từ 30 ngày gần nhất.**

                        **Response bao gồm:**
                        - Danh sách categories với: tên, icon, màu, số tiền, phần trăm, số giao dịch
                        - Tổng chi tiêu toàn hệ thống
                        - Tổng số giao dịch chi tiêu

                        **Sắp xếp:** Theo tổng số tiền giảm dần
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDistributionDTO.class), examples = @ExampleObject(value = """
                                        {
                                            "categories": [
                                                {"categoryId": 1, "categoryName": "Ăn uống", "icon": "<mdi:food>", "color": "#FF5733", "amount": 2975000000, "percentage": 35.0, "transactionCount": 15987},
                                                {"categoryId": 2, "categoryName": "Di chuyển", "icon": "<mdi:car>", "color": "#33A1FF", "amount": 1700000000, "percentage": 20.0, "transactionCount": 8234},
                                                {"categoryId": 3, "categoryName": "Mua sắm", "icon": "<mdi:shopping>", "color": "#FF33A1", "amount": 1275000000, "percentage": 15.0, "transactionCount": 6543},
                                                {"categoryId": 4, "categoryName": "Giải trí", "icon": "<mdi:movie>", "color": "#A1FF33", "amount": 850000000, "percentage": 10.0, "transactionCount": 4321},
                                                {"categoryId": 5, "categoryName": "Hóa đơn", "icon": "<mdi:receipt>", "color": "#FFC733", "amount": 765000000, "percentage": 9.0, "transactionCount": 3210},
                                                {"categoryId": 6, "categoryName": "Khác", "icon": "<mdi:dots-horizontal>", "color": "#808080", "amount": 935000000, "percentage": 11.0, "transactionCount": 7383}
                                            ],
                                            "totalSpending": 8500000000,
                                            "totalTransactions": 45678
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
                        @ApiResponse(responseCode = "403", description = "Không có quyền Admin")
        })
        public ResponseEntity<CategoryDistributionDTO> getCategoryDistribution() {
                CategoryDistributionDTO distribution = adminDashboardService.getCategoryDistribution();
                return ResponseEntity.ok(distribution);
        }

        /**
         * GET /api/admin/dashboard/bank-distribution
         * Lấy phân bổ giao dịch theo ngân hàng
         */
        @GetMapping("/bank-distribution")
        @Operation(summary = "Phân bổ ngân hàng", description = """
                        Lấy thống kê giao dịch theo ngân hàng (dùng cho biểu đồ cột ngang).

                        **Dữ liệu lấy từ tất cả giao dịch SMS tự động.**

                        **Response bao gồm:**
                        - Danh sách banks: mã, tên, số users, phần trăm, số giao dịch
                        - Tổng số users có giao dịch SMS
                        - Tổng số giao dịch từ SMS

                        **Sắp xếp:** Theo số giao dịch giảm dần
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankDistributionDTO.class), examples = @ExampleObject(value = """
                                        {
                                            "banks": [
                                                {"bankCode": "VCB", "bankName": "Vietcombank", "userCount": 3245, "percentage": 25.9, "transactionCount": 45678},
                                                {"bankCode": "TCB", "bankName": "Techcombank", "userCount": 2891, "percentage": 23.1, "transactionCount": 38456},
                                                {"bankCode": "MBB", "bankName": "MB Bank", "userCount": 2134, "percentage": 17.0, "transactionCount": 28765},
                                                {"bankCode": "ACB", "bankName": "ACB", "userCount": 1567, "percentage": 12.5, "transactionCount": 19876},
                                                {"bankCode": "VPB", "bankName": "VPBank", "userCount": 1234, "percentage": 9.8, "transactionCount": 15234},
                                                {"bankCode": "BID", "bankName": "BIDV", "userCount": 987, "percentage": 7.9, "transactionCount": 11234},
                                                {"bankCode": "MOMO", "bankName": "MoMo", "userCount": 485, "percentage": 3.8, "transactionCount": 6435}
                                            ],
                                            "totalUsers": 12543,
                                            "totalTransactions": 165678
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
                        @ApiResponse(responseCode = "403", description = "Không có quyền Admin")
        })
        public ResponseEntity<BankDistributionDTO> getBankDistribution() {
                BankDistributionDTO distribution = adminDashboardService.getBankDistribution();
                return ResponseEntity.ok(distribution);
        }

        /**
         * GET /api/admin/dashboard/system-health
         * Lấy tình trạng sức khỏe hệ thống
         */
        @GetMapping("/system-health")
        @Operation(summary = "Tình trạng hệ thống", description = """
                        Lấy thông tin sức khỏe hệ thống realtime.

                        **Thông tin CPU & RAM:**
                        - Phần trăm sử dụng CPU
                        - Phần trăm và dung lượng RAM (total/used/free)

                        **Database:**
                        - Trạng thái kết nối (CONNECTED/DISCONNECTED/SLOW)
                        - Số connections hiện tại
                        - Response time (ms)

                        **AI Backend:**
                        - Trạng thái (ONLINE/OFFLINE/DEGRADED)
                        - Response time (ms)

                        **General:**
                        - Uptime của server
                        - JVM version
                        - Active threads
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SystemHealthDTO.class), examples = @ExampleObject(value = """
                                        {
                                            "cpuUsage": 45.0,
                                            "ramUsage": 68.0,
                                            "totalMemoryMB": 8192,
                                            "usedMemoryMB": 5570,
                                            "freeMemoryMB": 2622,
                                            "databaseStatus": "CONNECTED",
                                            "databaseConnections": 15,
                                            "databaseResponseMs": 25,
                                            "aiBackendStatus": "ONLINE",
                                            "aiResponseMs": 150,
                                            "lastUpdated": "2026-01-05T17:00:00",
                                            "uptimeSeconds": 86400,
                                            "jvmVersion": "21.0.1",
                                            "activeThreads": 45
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
                        @ApiResponse(responseCode = "403", description = "Không có quyền Admin")
        })
        public ResponseEntity<SystemHealthDTO> getSystemHealth() {
                SystemHealthDTO health = adminDashboardService.getSystemHealth();
                return ResponseEntity.ok(health);
        }
}
