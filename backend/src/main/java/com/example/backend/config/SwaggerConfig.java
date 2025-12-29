package com.example.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Finpal API - Ví Thông Minh")
                                                .version("1.0.0")
                                                .description("""
                                                                ## Finpal Mobile App Backend API Documentation

                                                                ### Hệ thống quản lý tài chính cá nhân thông minh

                                                                **Tính năng chính:**
                                                                - 🤖 Tự động ghi nhận giao dịch từ SMS ngân hàng
                                                                - 📊 Bảng điều khiển trực quan (Dashboard)
                                                                - 💰 Quản lý ngân sách & mục tiêu tiết kiệm
                                                                - 🔔 Thông báo chủ động & phát hiện bất thường
                                                                - 🎯 AI phân loại giao dịch tự động

                                                                ---

                                                                ### Hướng dẫn sử dụng:

                                                                1. **Đăng ký tài khoản**: POST `/api/auth/register`
                                                                2. **Authorize**: Click nút \"Authorize\" 🔒 ở trên
                                                                3. **Nhập username và password** đã đăng ký
                                                                4. **Test APIs**: Tất cả endpoints đã được bảo vệ

                                                                ---

                                                                ### Modules:
                                                                - **Auth**: Đăng ký & Đăng nhập
                                                                - **Transactions**: Quản lý giao dịch (auto & manual)
                                                                - **SMS**: Nhận SMS từ ngân hàng (auto-create transaction)
                                                                - **Dashboard**: Tổng quan tài chính
                                                                - **Budgets**: Quản lý ngân sách
                                                                - **Savings Goals**: Mục tiêu tiết kiệm
                                                                - **Notifications**: Thông báo & cảnh báo
                                                                - **Statistics**: Thống kê & phân tích
                                                                """)
                                                .contact(new Contact()
                                                                .name("Finpal Team")
                                                                .email("support@finpal.com"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .components(new Components()
                                                .addSecuritySchemes("basicAuth", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("basic")
                                                                .description("Đăng nhập bằng username và password")))
                                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
        }
}
