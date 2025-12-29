package com.example.backend.controller;

import com.example.backend.dto.ChangePasswordRequest;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.model.User;
import com.example.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý Authentication (Xác thực người dùng)
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "🔐 Authentication", description = "API đăng nhập & đăng ký tài khoản")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng nhập", description = "Đăng nhập với username/password để nhận JWT token. Dùng token này để gọi các API khác.", responses = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công - trả về JWT token"),
            @ApiResponse(responseCode = "401", description = "Sai username hoặc password")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản mới. Sau khi đăng ký thành công sẽ tự động đăng nhập và trả về JWT token.", responses = {
            @ApiResponse(responseCode = "201", description = "Đăng ký thành công - trả về JWT token"),
            @ApiResponse(responseCode = "400", description = "Username/email đã tồn tại hoặc dữ liệu không hợp lệ")
    })
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFullName());

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "Test API", description = "Kiểm tra Auth module hoạt động")
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth API is working!");
    }

    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu cho user đã đăng nhập. Yêu cầu nhập mật khẩu hiện tại và mật khẩu mới.", responses = {
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Mật khẩu hiện tại không đúng hoặc dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            authService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok().body(java.util.Map.of(
                    "success", true,
                    "message", "Đổi mật khẩu thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }
}
