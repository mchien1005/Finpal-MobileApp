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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // Validate input
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return new LoginResponse(false, "Username và password không được để trống", null, null);
        }

        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            // Get user details
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update last login
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Generate JWT token
            String token = jwtService.generateToken(user);

            // Create user info (without password)
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

    public LoginResponse register(String username, String email, String password, String fullName) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            return new LoginResponse(false, "Username không được để trống", null, null);
        }
        if (email == null || email.trim().isEmpty()) {
            return new LoginResponse(false, "Email không được để trống", null, null);
        }
        if (password == null || password.length() < 6) {
            return new LoginResponse(false, "Password phải có ít nhất 6 ký tự", null, null);
        }

        // Check if username exists
        if (userRepository.existsByUsername(username)) {
            return new LoginResponse(false, "Username đã tồn tại", null, null);
        }

        // Check if email exists
        if (userRepository.existsByEmail(email)) {
            return new LoginResponse(false, "Email đã được sử dụng", null, null);
        }

        try {
            // Create new user
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setRole(Role.USER);
            user.setIsActive(true);
            user.setEmailVerified(false);

            // Save user
            user = userRepository.save(user);

            // Generate JWT token
            String token = jwtService.generateToken(user);

            // Create user info
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
