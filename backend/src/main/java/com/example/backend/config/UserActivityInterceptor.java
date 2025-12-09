package com.example.backend.config;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

/**
 * Interceptor để cập nhật thời gian hoạt động cuối cùng của người dùng
 */
@Component
@RequiredArgsConstructor
public class UserActivityInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        
        // Lấy thông tin user từ Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof User) {
            
            User user = (User) authentication.getPrincipal();
            
            // Cập nhật lastActiveAt trong database (async để không ảnh hưởng performance)
            updateLastActiveTime(user.getId());
        }
        
        return true;
    }

    private void updateLastActiveTime(Long userId) {
        try {
            userRepository.findById(userId).ifPresent(user -> {
                user.setLastActiveAt(LocalDateTime.now());
                userRepository.save(user);
            });
        } catch (Exception e) {
            // Log error nhưng không throw exception để không ảnh hưởng request
            System.err.println("Failed to update last active time for user: " + userId);
        }
    }
}
