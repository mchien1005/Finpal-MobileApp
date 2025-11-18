package com.example.backend.service;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // Validate input
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return new LoginResponse(false, "Username và password không được để trống", null, null);
        }

        // Check if user exists
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return new LoginResponse(false, "Tên đăng nhập không tồn tại", null, null);
        }

        User user = userOptional.get();

        // Verify password
        if (!user.getPassword().equals(password)) {
            return new LoginResponse(false, "Mật khẩu không đúng", null, null);
        }

        // Generate token (simple UUID for demo - use JWT in production)
        String token = UUID.randomUUID().toString();

        // Create user info (without password)
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName());

        return new LoginResponse(true, "Đăng nhập thành công", token, userInfo);
    }
}
