package com.example.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
                                                                2. **Đăng nhập**: POST `/api/auth/login` → Nhận JWT token
                                                                3. **Authorize**: Click nút "Authorize" 🔒 ở trên, nhập: `Bearer YOUR_TOKEN`
                                                                4. **Test APIs**: Tất cả endpoints đã được bảo vệ bởi JWT

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
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8080")
                                                                .description("Local Development Server"),
                                                new Server()
                                                                .url("http://175.41.150.228:8080")
                                                                .description("AWS Server")))
                                .components(new Components()
                                                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .in(SecurityScheme.In.HEADER)
                                                                .name("Authorization")
                                                                .description("JWT token từ `/api/auth/login`. Format: `Bearer YOUR_TOKEN`")))
                                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
        }
}
