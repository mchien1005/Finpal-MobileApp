package com.example.backend.service;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;
import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service Xác thực & Đăng ký (Authentication & Registration)
 * Chức năng: Login, Register, tạo JWT token
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Đăng nhập user
     * - Xác thực username/password
     * - Tạo JWT token
     * - Cập nhật lastLoginAt
     * 
     * @param request LoginRequest chứa username và password
     * @return LoginResponse chứa token, userInfo, và message thành công/thất bại
     */
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // Kiểm tra đầu vào
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return new LoginResponse(false, "Username và password không được để trống", null, null);
        }

        try {
            // Xác thực user qua Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            // Lấy thông tin user từ database
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Cập nhật thời gian đăng nhập cuối
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Tạo JWT token cho user
            String token = jwtService.generateToken(user);

            // Tạo thông tin user (không bao gồm password)
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName());

            return new LoginResponse(true, "Đăng nhập thành công", token, userInfo);

        } catch (Exception e) {
            return new LoginResponse(false, "Tên đăng nhập hoặc mật khẩu không đúng", null, null);
        }
    }

    /**
     * Đăng ký user mới
     * - Validate input (username, email, password)
     * - Kiểm tra trùng lặp username/email
     * - Mã hóa password
     * - Tạo JWT token
     * 
     * @param username Tên đăng nhập (unique)
     * @param email    Email (unique)
     * @param password Mật khẩu (tối thiểu 6 ký tự)
     * @param fullName Họ tên đầy đủ
     * @return LoginResponse chứa token, userInfo, và message thành công/thất bại
     */
    public LoginResponse register(String username, String email, String password, String fullName) {
        // Kiểm tra đầu vào
        if (username == null || username.trim().isEmpty()) {
            return new LoginResponse(false, "Username không được để trống", null, null);
        }
        if (email == null || email.trim().isEmpty()) {
            return new LoginResponse(false, "Email không được để trống", null, null);
        }
        if (password == null || password.length() < 6) {
            return new LoginResponse(false, "Password phải có ít nhất 6 ký tự", null, null);
        }

        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(username)) {
            return new LoginResponse(false, "Username đã tồn tại", null, null);
        }

        // Kiểm tra email đã được sử dụng chưa
        if (userRepository.existsByEmail(email)) {
            return new LoginResponse(false, "Email đã được sử dụng", null, null);
        }

        try {
            // Tạo user mới với password đã mã hóa
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setRole(Role.USER);
            user.setIsActive(true);
            // Lưu user vào database
            user = userRepository.save(user);

            // Tạo JWT token cho user mới
            String token = jwtService.generateToken(user);

            // Tạo thông tin user
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName());

            return new LoginResponse(true, "Đăng ký thành công", token, userInfo);

        } catch (Exception e) {
            return new LoginResponse(false, "Có lỗi xảy ra khi đăng ký: " + e.getMessage(), null, null);
        }
    }
}
