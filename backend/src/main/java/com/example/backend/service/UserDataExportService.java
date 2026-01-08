package com.example.backend.service;

import com.example.backend.model.*;
import com.example.backend.repository.*;
import com.example.backend.util.EncryptionUtil;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDataExportService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final EncryptionUtil encryptionUtil;

    @Value("${pdf.export.temp.directory:/tmp/finpal-exports}")
    private String exportDirectory;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // Timezone Việt Nam (UTC+7)
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Lấy thời gian hiện tại theo múi giờ Việt Nam
     */
    private LocalDateTime getVietnamNow() {
        return ZonedDateTime.now(VIETNAM_ZONE).toLocalDateTime();
    }

    /**
     * Xuất tất cả dữ liệu cá nhân của người dùng ra file PDF
     */
    @Transactional(readOnly = true)
    public String exportUserData(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Tạo thư mục export nếu chưa có
        Path exportPath = Paths.get(exportDirectory);
        if (!Files.exists(exportPath)) {
            Files.createDirectories(exportPath);
        }

        // Tạo tên file
        String fileName = String.format("FinPal_Data_Export_%s_%s.pdf",
                user.getUsername(),
                getVietnamNow().format(FILE_DATE_FORMATTER));
        String filePath = exportPath.resolve(fileName).toString();

        // Tạo PDF
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc)) {

            // Font hỗ trợ Unicode (tiếng Việt)
            PdfFont font;
            PdfFont boldFont;
            try {
                // Thử load font từ hệ thống (ưu tiên Noto Sans hoặc DejaVu Sans trên Linux)
                String fontPath = "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf";
                String boldFontPath = "/usr/share/fonts/truetype/noto/NotoSans-Bold.ttf";

                if (new File(fontPath).exists()) {
                    font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H,
                            PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    boldFont = PdfFontFactory.createFont(boldFontPath, PdfEncodings.IDENTITY_H,
                            PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                } else {
                    // Fallback to standard names if file path not found
                    log.warn("Noto Sans font not found at {}, falling back to StandardFonts", fontPath);
                    font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                    boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                }
            } catch (Exception e) {
                log.warn("Could not load preferred font, using StandardFonts: " + e.getMessage());
                font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            }

            // Tiêu đề (dùng tiếng Việt không dấu để đảm bảo hiển thị nếu fallback fail)
            // Nhưng nếu font load được thì dùng tiếng Việt có dấu ok
            Paragraph title = new Paragraph("FINPAL - BÁO CÁO DỮ LIỆU CÁ NHÂN")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Thông tin người dùng
            addUserInfo(document, user, font, boldFont);

            // Thống kê tổng quan
            addOverviewStatistics(document, userId, font, boldFont);

            // Danh sách giao dịch
            addTransactions(document, userId, font, boldFont);

            // Ngân sách
            addBudgets(document, userId, font, boldFont);

            // Mục tiêu tiết kiệm
            addSavingsGoals(document, userId, font, boldFont);

            // Footer
            Paragraph footer = new Paragraph(String.format("Báo cáo được tạo vào: %s",
                    getVietnamNow().format(DATE_TIME_FORMATTER)))
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20);
            document.add(footer);

            log.info("Đã tạo file PDF export cho user {}: {}", userId, filePath);
            return filePath;
        }
    }

    private void addUserInfo(Document document, User user, PdfFont font, PdfFont boldFont) {
        document.add(new Paragraph("1. THÔNG TIN NGƯỜI DÙNG")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(10));

        Table table = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }))
                .setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "Tên đăng nhập:", user.getUsername(), font, boldFont);
        addTableRow(table, "Email:", user.getEmail(), font, boldFont);
        addTableRow(table, "Họ tên:", user.getFullName() != null ? user.getFullName() : "N/A", font, boldFont);
        addTableRow(table, "Số điện thoại:", user.getPhone() != null ? user.getPhone() : "N/A", font, boldFont);
        addTableRow(table, "Ngày sinh:",
                user.getDateOfBirth() != null ? user.getDateOfBirth().format(DATE_FORMATTER) : "N/A", font, boldFont);
        addTableRow(table, "Ngày tạo tài khoản:",
                user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_TIME_FORMATTER) : "N/A", font, boldFont);

        document.add(table);
    }

    private void addOverviewStatistics(Document document, Long userId, PdfFont font, PdfFont boldFont) {
        document.add(new Paragraph("2. THỐNG KÊ TỔNG QUAN")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(15));

        // Tính toán thống kê
        List<Transaction> allTransactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
        BigDecimal totalIncome = allTransactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = allTransactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Table table = new Table(UnitValue.createPercentArray(new float[] { 40, 60 }))
                .setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "Tổng số giao dịch:", String.valueOf(allTransactions.size()), font, boldFont);
        addTableRow(table, "Tổng thu nhập:", formatMoney(totalIncome) + " VND", font, boldFont);
        addTableRow(table, "Tổng chi tiêu:", formatMoney(totalExpense) + " VND", font, boldFont);
        addTableRow(table, "Số dư:", formatMoney(totalIncome.subtract(totalExpense)) + " VND", font, boldFont);

        document.add(table);
    }

    private void addTransactions(Document document, Long userId, PdfFont font, PdfFont boldFont) {
        document.add(new Paragraph("3. LỊCH SỬ GIAO DỊCH")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(15));

        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);

        if (transactions.isEmpty()) {
            document.add(new Paragraph("Không có giao dịch nào.").setFont(font));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[] { 15, 20, 30, 20, 15 }))
                .setWidth(UnitValue.createPercentValue(100));

        // Header
        table.addHeaderCell(new Cell().add(new Paragraph("Ngày").setFont(boldFont).setFontSize(10)));
        table.addHeaderCell(new Cell().add(new Paragraph("Loại").setFont(boldFont).setFontSize(10)));
        table.addHeaderCell(new Cell().add(new Paragraph("Danh mục").setFont(boldFont).setFontSize(10)));
        table.addHeaderCell(new Cell().add(new Paragraph("Mô tả").setFont(boldFont).setFontSize(10)));
        table.addHeaderCell(new Cell().add(new Paragraph("Số tiền").setFont(boldFont).setFontSize(10)));

        // Data (giới hạn 1000 giao dịch để tránh file PDF quá lớn)
        int limit = Math.min(transactions.size(), 1000);
        for (int i = 0; i < limit; i++) {
            Transaction t = transactions.get(i);

            // Decrypt description
            String description = t.getDescription();
            try {
                if (description != null && !description.isEmpty()) {
                    description = encryptionUtil.decrypt(description);
                }
            } catch (Exception e) {
                log.debug("Failed to decrypt transaction description for ID {}: {}", t.getId(), e.getMessage());
            }

            table.addCell(new Cell()
                    .add(new Paragraph(t.getTransactionDate().format(DATE_FORMATTER)).setFont(font).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(t.getType().name()).setFont(font).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(t.getCategory() != null ? t.getCategory().getName() : "N/A")
                    .setFont(font).setFontSize(9)));
            table.addCell(new Cell().add(
                    new Paragraph(description != null ? description : "").setFont(font).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(formatMoney(t.getAmount())).setFont(font).setFontSize(9)));
        }

        document.add(table);

        if (transactions.size() > 1000) {
            document.add(new Paragraph(String.format("(Chỉ hiển thị 1000/%d giao dịch gần nhất)", transactions.size()))
                    .setFont(font)
                    .setFontSize(9)
                    .setItalic());
        }
    }

    private void addBudgets(Document document, Long userId, PdfFont font, PdfFont boldFont) {
        document.add(new Paragraph("4. NGÂN SÁCH")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(15));

        List<Budget> budgets = budgetRepository.findByUserId(userId);

        if (budgets.isEmpty()) {
            document.add(new Paragraph("Không có ngân sách nào.").setFont(font));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[] { 30, 25, 25, 20 }))
                .setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(new Cell().add(new Paragraph("Danh mục").setFont(boldFont).setFontSize(10)));
        table.addHeaderCell(new Cell().add(new Paragraph("Giới hạn").setFont(boldFont).setFontSize(10)));
        table.addHeaderCell(new Cell().add(new Paragraph("Thời gian").setFont(boldFont).setFontSize(10)));
        table.addHeaderCell(new Cell().add(new Paragraph("Trạng thái").setFont(boldFont).setFontSize(10)));

        for (Budget budget : budgets) {
            // Only categoryId is available, not category object
            String categoryName = "Tổng";
            if (budget.getCategoryId() != null) {
                Category cat = null;
                try {
                    cat = categoryRepository.findById(budget.getCategoryId()).orElse(null);
                } catch (Exception ignored) {
                }
                if (cat != null) {
                    categoryName = cat.getName();
                }
            }
            table.addCell(new Cell().add(new Paragraph(categoryName).setFont(font).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(formatMoney(budget.getAmount())).setFont(font).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(budget.getPeriod().name()).setFont(font).setFontSize(9)));
            table.addCell(new Cell()
                    .add(new Paragraph(budget.getIsActive() ? "Active" : "Inactive").setFont(font).setFontSize(9)));
        }

        document.add(table);
    }

    private void addSavingsGoals(Document document, Long userId, PdfFont font, PdfFont boldFont) {
        document.add(new Paragraph("5. MỤC TIÊU TIẾT KIỆM")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(15));

        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(userId);

        if (goals.isEmpty()) {
            document.add(new Paragraph("Không có mục tiêu tiết kiệm.").setFont(font));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[] { 30, 20, 20, 15, 15 }))
                .setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(new Cell().add(new Paragraph("Tên mục tiêu").setFont(boldFont).setFontSize(10)));
        table.addHeaderCell(new Cell().add(new Paragraph("Mục tiêu").setFont(boldFont).setFontSize(10)));
        table.addHeaderCell(new Cell().add(new Paragraph("Hiện tại").setFont(boldFont).setFontSize(10)));
        table.addHeaderCell(new Cell().add(new Paragraph("Tiến độ").setFont(boldFont).setFontSize(10)));
        table.addHeaderCell(new Cell().add(new Paragraph("Hạn").setFont(boldFont).setFontSize(10)));

        for (SavingsGoal goal : goals) {
            table.addCell(new Cell().add(new Paragraph(goal.getName()).setFont(font).setFontSize(9)));
            table.addCell(
                    new Cell().add(new Paragraph(formatMoney(goal.getTargetAmount())).setFont(font).setFontSize(9)));
            table.addCell(
                    new Cell().add(new Paragraph(formatMoney(goal.getCurrentAmount())).setFont(font).setFontSize(9)));

            double progress = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                    ? goal.getCurrentAmount().divide(goal.getTargetAmount(), 4, java.math.RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0;
            table.addCell(
                    new Cell().add(new Paragraph(String.format("%.1f%%", progress)).setFont(font).setFontSize(9)));
            // Use deadline instead of getTargetDate
            table.addCell(new Cell()
                    .add(new Paragraph(goal.getDeadline() != null ? goal.getDeadline().format(DATE_FORMATTER) : "N/A")
                            .setFont(font).setFontSize(9)));
        }

        document.add(table);
    }

    private void addTableRow(Table table, String label, String value, PdfFont font, PdfFont boldFont) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(boldFont).setFontSize(10)));
        table.addCell(new Cell().add(new Paragraph(value).setFont(font).setFontSize(10)));
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null)
            return "0";
        return String.format("%,d", amount.longValue());
    }

    /**
     * Xóa các file export cũ (quá thời hạn retention)
     */
    public void cleanupOldExports(int retentionDays) {
        try {
            Path exportPath = Paths.get(exportDirectory);
            if (!Files.exists(exportPath)) {
                return;
            }

            LocalDateTime cutoffDate = getVietnamNow().minusDays(retentionDays);

            Files.walk(exportPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".pdf"))
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path)
                                    .toInstant()
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDateTime()
                                    .isBefore(cutoffDate);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("Deleted old export file: {}", path);
                        } catch (Exception e) {
                            log.error("Failed to delete file: {}", path, e);
                        }
                    });

        } catch (Exception e) {
            log.error("Error cleaning up old exports", e);
        }
    }
}
