package com.example.backend.service;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;
import com.example.backend.model.LoginHistory;
import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.repository.LoginHistoryRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Service Xác thực & Đăng ký (Authentication & Registration)
 * Chức năng: Login, Register, tạo JWT token, chặn đăng nhập thất bại
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoginHistoryRepository loginHistoryRepository;

    // Số lần đăng nhập thất bại tối đa trước khi bị chặn
    @Value("${security.login.max-failed-attempts:5}")
    private int maxFailedAttempts;

    // Thời gian chặn (phút) sau khi vượt quá số lần thất bại
    @Value("${security.login.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    /**
     * Đăng nhập user với kiểm tra số lần thất bại
     * - Kiểm tra user có bị chặn không
     * - Xác thực username/password
     * - Ghi lịch sử đăng nhập
     * - Tạo JWT token
     * 
     * @param request LoginRequest chứa username và password
     * @return LoginResponse chứa token, userInfo, và message thành công/thất bại
     */
    public LoginResponse login(LoginRequest request) {
        String usernameOrEmail = request.getUsername();
        String password = request.getPassword();

        // Kiểm tra đầu vào
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return new LoginResponse(false, "Username/Email và password không được để trống", null, null);
        }

        // Tìm user trong database
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElse(null);

        // Kiểm tra user có tồn tại không
        if (user == null) {
            return new LoginResponse(false, "Tên đăng nhập hoặc mật khẩu không đúng", null, null);
        }

        // Kiểm tra tài khoản có bị vô hiệu hóa không
        if (!user.getIsActive()) {
            saveLoginHistory(user, LoginHistory.LoginStatus.BLOCKED, "Tài khoản đã bị vô hiệu hóa");
            return new LoginResponse(false, "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ admin.", null, null);
        }

        // Kiểm tra số lần đăng nhập thất bại gần đây
        LocalDateTime oneHourAgo = LocalDateTime.now().minusMinutes(lockoutDurationMinutes);
        Long recentFailedAttempts = loginHistoryRepository.countRecentFailedLogins(user.getId(), oneHourAgo);

        if (recentFailedAttempts >= maxFailedAttempts) {
            saveLoginHistory(user, LoginHistory.LoginStatus.BLOCKED, "Vượt quá số lần đăng nhập thất bại");
            int remainingMinutes = lockoutDurationMinutes;
            log.warn("🚫 User {} bị chặn đăng nhập do {} lần thất bại liên tiếp", user.getUsername(), recentFailedAttempts);
            return new LoginResponse(false, 
                    String.format("Tài khoản tạm thời bị khóa do đăng nhập sai quá %d lần. Vui lòng thử lại sau %d phút.", 
                            maxFailedAttempts, remainingMinutes), 
                    null, null);
        }

        try {
            // Xác thực user qua Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, password));

            // Đăng nhập thành công - ghi lịch sử
            saveLoginHistory(user, LoginHistory.LoginStatus.SUCCESS, null);

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

            log.info("✅ User {} đăng nhập thành công", user.getUsername());
            return new LoginResponse(true, "Đăng nhập thành công", token, userInfo);

        } catch (Exception e) {
            // Đăng nhập thất bại - ghi lịch sử
            saveLoginHistory(user, LoginHistory.LoginStatus.FAILED, "Sai mật khẩu");
            
            long remainingAttempts = maxFailedAttempts - recentFailedAttempts - 1;
            log.warn("❌ User {} đăng nhập thất bại. Còn {} lần thử.", user.getUsername(), remainingAttempts);
            
            if (remainingAttempts <= 2 && remainingAttempts > 0) {
                return new LoginResponse(false, 
                        String.format("Sai mật khẩu. Còn %d lần thử trước khi tài khoản bị tạm khóa.", remainingAttempts), 
                        null, null);
            }
            return new LoginResponse(false, "Tên đăng nhập hoặc mật khẩu không đúng", null, null);
        }
    }

    /**
     * Lưu lịch sử đăng nhập
     */
    private void saveLoginHistory(User user, LoginHistory.LoginStatus status, String failureReason) {
        try {
            String ipAddress = getClientIpAddress();
            String userAgent = getUserAgent();
            
            LoginHistory history = LoginHistory.builder()
                    .user(user)
                    .loginTime(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .deviceName(parseDeviceName(userAgent))
                    .deviceType(parseDeviceType(userAgent))
                    .operatingSystem(parseOS(userAgent))
                    .browser(parseBrowser(userAgent))
                    .status(status)
                    .failureReason(failureReason)
                    .build();

            loginHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("Lỗi khi lưu lịch sử đăng nhập: {}", e.getMessage());
        }
    }

    /**
     * Lấy IP của client
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Không thể lấy IP address: {}", e.getMessage());
        }
        return "unknown";
    }

    /**
     * Lấy User-Agent từ request
     */
    private String getUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Không thể lấy User-Agent: {}", e.getMessage());
        }
        return "";
    }

    /**
     * Parse tên thiết bị từ User-Agent
     */
    private String parseDeviceName(String userAgent) {
        if (userAgent == null) return "Unknown Device";
        
        if (userAgent.contains("iPhone")) return "iPhone";
        if (userAgent.contains("iPad")) return "iPad";
        if (userAgent.contains("Xiaomi")) return "Xiaomi";
        if (userAgent.contains("Redmi")) return "Redmi";
        if (userAgent.contains("OnePlus")) return "OnePlus";
        if (userAgent.contains("OPPO")) return "OPPO";
        if (userAgent.contains("Vivo")) return "Vivo";
        if (userAgent.contains("Realme")) return "Realme";
        if (userAgent.contains("Nokia")) return "Nokia";
        if (userAgent.contains("Huawei")) return "Huawei";
        if (userAgent.contains("Poco")) return "Poco";
        if (userAgent.contains("Samsung")) return "Samsung";
        if (userAgent.contains("Pixel")) return "Google Pixel";
        if (userAgent.contains("Macintosh")) return "MacBook";
        if (userAgent.contains("Windows")) return "Windows PC";
        if (userAgent.contains("Android")) return "Android Device";
        
        return "Unknown Device";
    }

    /**
     * Parse loại thiết bị từ User-Agent
     */
    private String parseDeviceType(String userAgent) {
        if (userAgent == null) return "UNKNOWN";
        
        if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
            return "MOBILE";
        }
        if (userAgent.contains("iPad") || userAgent.contains("Tablet")) {
            return "TABLET";
        }
        return "DESKTOP";
    }

    /**
     * Parse OS từ User-Agent
     */
    private String parseOS(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        if (userAgent.contains("iPhone OS")) return "iOS";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("Windows NT 10")) return "Windows 10/11";
        if (userAgent.contains("Macintosh")) return "macOS";
        if (userAgent.contains("Linux")) return "Linux";
        
        return "Unknown";
    }

    /**
     * Parse browser từ User-Agent
     */
    private String parseBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) return "Chrome";
        if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) return "Safari";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Edg")) return "Edge";
        if (userAgent.contains("Opera") || userAgent.contains("OPR")) return "Opera";
        
        return "Unknown";
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
                    user.getRole().name(),
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
     * - Cập nhật fullName, email, phone, dateOfBirth, gender, avatarUrl
     * 
     * @param userId      ID của user
     * @param fullName    Họ tên mới (có thể null nếu không thay đổi)
     * @param email       Email mới (có thể null nếu không thay đổi)
     * @param phone       Số điện thoại mới (có thể null nếu không thay đổi)
     * @param dateOfBirth Ngày sinh mới (có thể null nếu không thay đổi)
     * @param gender      Giới tính mới (có thể null nếu không thay đổi)
     * @param avatarUrl   URL ảnh đại diện mới (có thể null nếu không thay đổi)
     * @return User đã được cập nhật
     */
    public User updateProfile(Long userId, String fullName, String email, String phone, java.time.LocalDate dateOfBirth, com.example.backend.model.Gender gender, String avatarUrl) {
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
        if (dateOfBirth != null) {
            user.setDateOfBirth(dateOfBirth);
        }
        if (gender != null) {
            user.setGender(gender);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }

        return userRepository.save(user);
    }
}
