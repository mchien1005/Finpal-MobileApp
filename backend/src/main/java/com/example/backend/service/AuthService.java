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
                    user.getFullName(),
                    user.getRole().name(),
                    user.getAvatarUrl());

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
                    user.getFullName(),
                    user.getRole().name()
                    user.getAvatarUrl());

            return new LoginResponse(true, "Đăng ký thành công", token, userInfo);

        } catch (Exception e) {
            return new LoginResponse(false, "Có lỗi xảy ra khi đăng ký: " + e.getMessage(), null, null);
        }
    }

    /**
     * Đổi mật khẩu user
     * - Kiểm tra mật khẩu hiện tại
     * - Mã hóa và lưu mật khẩu mới
     * 
     * @param userId          ID của user
     * @param currentPassword Mật khẩu hiện tại
     * @param newPassword     Mật khẩu mới
     * @return true nếu đổi thành công, false nếu thất bại
     */
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        // Mã hóa và lưu mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return true;
    }

    /**
     * Lấy thông tin profile của user
     * 
     * @param userId ID của user
     * @return User object
     */
    public User getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    /**
     * Cập nhật thông tin profile của user
     * - Validate email không trùng với user khác
     * - Cập nhật fullName, email, phone, avatarUrl
     * 
     * @param userId    ID của user
     * @param fullName  Họ tên mới (có thể null nếu không thay đổi)
     * @param email     Email mới (có thể null nếu không thay đổi)
     * @param phone     Số điện thoại mới (có thể null nếu không thay đổi)
     * @param avatarUrl URL ảnh đại diện mới (có thể null nếu không thay đổi)
     * @return User đã được cập nhật
     */
    public User updateProfile(Long userId, String fullName, String email, String phone, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Kiểm tra email không trùng với user khác
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(email, userId)) {
                throw new RuntimeException("Email đã được sử dụng bởi tài khoản khác");
            }
            user.setEmail(email);
        }

        // Cập nhật các trường khác nếu có giá trị
        if (fullName != null) {
            user.setFullName(fullName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }

        return userRepository.save(user);
    }
}
