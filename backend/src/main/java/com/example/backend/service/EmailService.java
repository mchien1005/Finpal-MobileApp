package com.example.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Gửi email với file đính kèm
     */
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML

            // Đính kèm file
            if (attachmentPath != null && !attachmentPath.isEmpty()) {
                File file = new File(attachmentPath);
                if (file.exists()) {
                    FileSystemResource fileResource = new FileSystemResource(file);
                    helper.addAttachment(file.getName(), fileResource);
                    log.info("Attached file: {} (size: {} bytes)", file.getName(), file.length());
                } else {
                    log.warn("Attachment file not found: {}", attachmentPath);
                }
            }

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Không thể gửi email: " + e.getMessage(), e);
        }
    }

    /**
     * Gửi email xuất dữ liệu cá nhân
     */
    public void sendDataExportEmail(String to, String username, String pdfPath) {
        String subject = "FinPal - Dữ liệu cá nhân của bạn";
        
        String body = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                        .content { background-color: #f9f9f9; padding: 20px; border-radius: 0 0 5px 5px; }
                        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; }
                        .button { display: inline-block; padding: 10px 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 10px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>FinPal</h1>
                            <p>Quản lý tài chính cá nhân thông minh</p>
                        </div>
                        <div class="content">
                            <h2>Xin chào %s,</h2>
                            <p>Yêu cầu xuất dữ liệu cá nhân của bạn đã được xử lý thành công.</p>
                            <p>File PDF đính kèm chứa toàn bộ thông tin về:</p>
                            <ul>
                                <li>Thông tin tài khoản</li>
                                <li>Lịch sử giao dịch</li>
                                <li>Danh mục cá nhân</li>
                                <li>Ngân sách</li>
                                <li>Mục tiêu tiết kiệm</li>
                                <li>Thống kê và phân tích</li>
                            </ul>
                            <p><strong>Lưu ý bảo mật:</strong></p>
                            <ul>
                                <li>File này chứa thông tin cá nhân nhạy cảm của bạn</li>
                                <li>Vui lòng lưu trữ file ở nơi an toàn</li>
                                <li>Không chia sẻ file này với người khác</li>
                            </ul>
                            <p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi qua email này.</p>
                            <div class="footer">
                                <p>Email này được gửi tự động vào: %s</p>
                                <p>© 2024 FinPal. All rights reserved.</p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """, 
                username, 
                LocalDateTime.now().format(DATE_TIME_FORMATTER)
        );

        sendEmailWithAttachment(to, subject, body, pdfPath);
    }

    /**
     * Gửi email thông báo tài khoản sẽ bị xóa
     */
    public void sendAccountDeletionNotification(String to, String username) {
        String subject = "FinPal - Xác nhận xóa tài khoản";
        
        String body = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #f44336; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                        .content { background-color: #f9f9f9; padding: 20px; border-radius: 0 0 5px 5px; }
                        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 15px 0; }
                        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>FinPal</h1>
                            <p>Xóa tài khoản</p>
                        </div>
                        <div class="content">
                            <h2>Xin chào %s,</h2>
                            <p>Yêu cầu xóa tài khoản của bạn đã được admin phê duyệt.</p>
                            <div class="warning">
                                <h3>⚠️ CẢNH BÁO QUAN TRỌNG</h3>
                                <p><strong>Tài khoản của bạn sẽ bị xóa vĩnh viễn trong vòng 24 giờ tới.</strong></p>
                                <p>Tất cả dữ liệu của bạn sẽ bị xóa bao gồm:</p>
                                <ul>
                                    <li>Thông tin tài khoản</li>
                                    <li>Tất cả giao dịch</li>
                                    <li>Danh mục cá nhân</li>
                                    <li>Ngân sách và mục tiêu tiết kiệm</li>
                                    <li>Tất cả dữ liệu liên quan khác</li>
                                </ul>
                            </div>
                            <p>Nếu đây là nhầm lẫn, vui lòng liên hệ với chúng tôi ngay lập tức qua email này hoặc qua hotline hỗ trợ.</p>
                            <p>Chúng tôi rất tiếc khi phải chia tay với bạn. Cảm ơn bạn đã sử dụng FinPal!</p>
                            <div class="footer">
                                <p>Email này được gửi vào: %s</p>
                                <p>© 2024 FinPal. All rights reserved.</p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """, 
                username,
                LocalDateTime.now().format(DATE_TIME_FORMATTER)
        );

        sendEmailWithAttachment(to, subject, body, null);
    }

    /**
     * Gửi email thông báo yêu cầu bị từ chối
     */
    public void sendRequestRejectionEmail(String to, String username, String requestType, String reason) {
        String subject = "FinPal - Yêu cầu của bạn đã bị từ chối";
        
        String requestTypeName = requestType.equals("EXPORT_DATA") 
                ? "xuất dữ liệu cá nhân" 
                : "xóa tài khoản";
        
        String body = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #ff9800; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                        .content { background-color: #f9f9f9; padding: 20px; border-radius: 0 0 5px 5px; }
                        .reason-box { background-color: #fff; border: 1px solid #ddd; padding: 15px; margin: 15px 0; border-radius: 5px; }
                        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>FinPal</h1>
                            <p>Thông báo yêu cầu</p>
                        </div>
                        <div class="content">
                            <h2>Xin chào %s,</h2>
                            <p>Yêu cầu <strong>%s</strong> của bạn đã bị từ chối bởi quản trị viên.</p>
                            <div class="reason-box">
                                <h3>Lý do từ chối:</h3>
                                <p>%s</p>
                            </div>
                            <p>Nếu bạn có bất kỳ thắc mắc nào về quyết định này, vui lòng liên hệ với chúng tôi qua email này.</p>
                            <p>Bạn có thể tạo yêu cầu mới nếu cần thiết.</p>
                            <div class="footer">
                                <p>Email này được gửi vào: %s</p>
                                <p>© 2024 FinPal. All rights reserved.</p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """, 
                username,
                requestTypeName,
                reason != null ? reason : "Không có lý do cụ thể",
                LocalDateTime.now().format(DATE_TIME_FORMATTER)
        );

        sendEmailWithAttachment(to, subject, body, null);
    }
}
