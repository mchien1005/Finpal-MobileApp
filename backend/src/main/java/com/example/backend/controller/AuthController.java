package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý Authentication (Xác thực người dùng)
 * 
 * Chức năng:
 * - Đăng nhập (login)
 * - Đăng ký tài khoản mới (register)
 * 
 * Endpoints:
 * - POST /api/auth/login - Đăng nhập
 * - POST /api/auth/register - Đăng ký
 */
@RestController // Đánh dấu đây là REST API Controller
@RequestMapping("/api/auth") // Tất cả endpoints trong class này đều bắt đầu với /api/auth
@CrossOrigin(origins = "*") // Cho phép CORS từ mọi domain (cần thiết cho mobile app)
@RequiredArgsConstructor // Lombok tự động tạo constructor cho các final fields
public class AuthController {

    // Inject AuthService để xử lý business logic
    private final AuthService authService;

    /**
     * API Đăng nhập
     * 
     * @param request - Thông tin đăng nhập (username, password)
     * @return LoginResponse chứa JWT token nếu thành công
     * 
     *         HTTP 200 OK - Đăng nhập thành công
     *         HTTP 401 UNAUTHORIZED - Sai username hoặc password
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // Gọi service để xác thực và tạo JWT token
        LoginResponse response = authService.login(request);

        if (response.isSuccess()) {
            // Đăng nhập thành công, trả về token
            return ResponseEntity.ok(response);
        } else {
            // Sai thông tin đăng nhập
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * API Đăng ký tài khoản mới
     * 
     * @param request - Thông tin đăng ký (username, email, password, fullName)
     * @return LoginResponse chứa JWT token nếu đăng ký thành công
     * 
     *         HTTP 201 CREATED - Đăng ký thành công
     *         HTTP 400 BAD_REQUEST - Username/email đã tồn tại hoặc dữ liệu không
     *         hợp lệ
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        // @Valid tự động validate dữ liệu theo rules trong RegisterRequest
        LoginResponse response = authService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFullName());

        if (response.isSuccess()) {
            // Đăng ký thành công, tự động đăng nhập và trả về token
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            // Đăng ký thất bại (username hoặc email đã tồn tại)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * API test để kiểm tra Auth module hoạt động
     * 
     * @return String message
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth API is working!");
    }
}
