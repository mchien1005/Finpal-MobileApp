package com.example.backend.service;

import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service tải thông tin User cho Spring Security
 * Implement UserDetailsService để tích hợp với Spring Security authentication
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user từ database theo username (dùng bởi Spring Security)
     * 
     * @param username Tên đăng nhập
     * @return UserDetails (User entity implements UserDetails interface)
     * @throws UsernameNotFoundException Nếu không tìm thấy user
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
