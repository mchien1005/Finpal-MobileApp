package com.example.backend.controller;

import com.example.backend.service.FcmService;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestNotificationController {

    private final FcmService fcmService;
    private final UserRepository userRepository;

    @GetMapping("/notification-payload/{userId}")
    public ResponseEntity<String> sendWithNotificationPayload(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getFcmToken() == null) {
            return ResponseEntity.badRequest().body("User not found or no FCM token");
        }

        String title = "Test Notification Payload";
        String body = "Message này có notification payload - Giống Firebase Console";

        try {
            // Logic gửi trực tiếp tại đây để debug, bypass FcmService
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setToken(user.getFcmToken())
                    .setNotification(notification);

            Map<String, String> data = new HashMap<>();
            data.put("title", title);
            data.put("body", body);
            data.put("type", "TEST");
            data.put("click_action", "FLUTTER_NOTIFICATION_CLICK");
            messageBuilder.putAllData(data);
            
            // Set channel ID explicit
            messageBuilder.setAndroidConfig(AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                    .setChannelId("high_importance_channel")
                    .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                    .build())
                .build());

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Sent notification payload test to user {}: {}", userId, response);
            return ResponseEntity.ok("Sent: " + response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/data-only/{userId}")
    public ResponseEntity<String> sendDataOnly(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getFcmToken() == null) {
            return ResponseEntity.badRequest().body("User not found or no FCM token");
        }

        String title = "Test Data Only";
        String body = "Message này chỉ có Data payload - Flutter tự handle";

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(user.getFcmToken());

            Map<String, String> data = new HashMap<>();
            data.put("title", title);
            data.put("body", body);
            data.put("type", "TEST");
            messageBuilder.putAllData(data);
            
            // Android config priority HIGH but NO notification
            messageBuilder.setAndroidConfig(AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .build());

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Sent data-only test to user {}: {}", userId, response);
            return ResponseEntity.ok("Sent: " + response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
