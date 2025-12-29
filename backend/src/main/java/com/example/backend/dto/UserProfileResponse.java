package com.example.backend.dto;

import com.example.backend.model.Gender;
import com.example.backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActiveAt;
    private String lastActiveText; 

    public static UserProfileResponse fromUser(User user) {
        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getAvatarUrl(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastActiveAt(),
                null);
        
        // Tính toán text hiển thị thời gian hoạt động
        response.setLastActiveText(calculateLastActiveText(user.getLastActiveAt()));
        return response;
    }

    private static String calculateLastActiveText(LocalDateTime lastActiveAt) {
        if (lastActiveAt == null) {
            return "Chưa có hoạt động";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(lastActiveAt, now);

        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (seconds < 60) {
            return "Đang hoạt động";
        } else if (minutes < 60) {
            return "Hoạt động " + minutes + " phút trước";
        } else if (hours < 24) {
            return "Hoạt động " + hours + " giờ trước";
        } else if (days < 7) {
            return "Hoạt động " + days + " ngày trước";
        } else if (days < 30) {
            long weeks = days / 7;
            return "Hoạt động " + weeks + " tuần trước";
        } else if (days < 365) {
            long months = days / 30;
            return "Hoạt động " + months + " tháng trước";
        } else {
            long years = days / 365;
            return "Hoạt động " + years + " năm trước";
        }
    }
}
