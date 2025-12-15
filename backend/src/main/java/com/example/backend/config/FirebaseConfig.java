package com.example.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Configuration cho FCM Push Notifications
 * 
 * Cấu hình kết nối với Firebase Admin SDK để gửi push notification
 * đến thiết bị di động thông qua Firebase Cloud Messaging (FCM).
 * 
 * Yêu cầu:
 * - File firebase-service-account.json trong resources/ hoặc đường dẫn tùy chỉnh
 * - Bật fcm.enabled=true trong application.properties
 */
@Configuration
@Slf4j
@ConditionalOnProperty(value = "fcm.enabled", havingValue = "true", matchIfMissing = false)
public class FirebaseConfig {

    @Value("${fcm.service-account-file:firebase-service-account.json}")
    private String serviceAccountFile;

    /**
     * Khởi tạo Firebase App khi ứng dụng startup
     * 
     * Load credentials từ file service account và khởi tạo Firebase SDK.
     * Nếu Firebase đã được khởi tạo (hot reload), sẽ skip.
     */
    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = loadServiceAccount();
                
                if (serviceAccount == null) {
                    log.warn("⚠️ Firebase service account file not found. FCM push notifications will be disabled.");
                    return;
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase Admin SDK initialized successfully for FCM push notifications");
            } else {
                log.info("ℹ️ Firebase Admin SDK already initialized");
            }
        } catch (IOException e) {
            log.error("❌ Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            log.warn("⚠️ FCM push notifications will not work. Please check your firebase-service-account.json file.");
        }
    }

    /**
     * Load service account file từ classpath hoặc filesystem
     */
    private InputStream loadServiceAccount() {
        try {
            // Thử load từ classpath trước (src/main/resources)
            Resource resource = new ClassPathResource(serviceAccountFile);
            if (resource.exists()) {
                log.info("📁 Loading Firebase service account from classpath: {}", serviceAccountFile);
                return resource.getInputStream();
            }

            // Nếu không có trong classpath, thử load từ filesystem
            java.io.File file = new java.io.File(serviceAccountFile);
            if (file.exists()) {
                log.info("📁 Loading Firebase service account from file: {}", serviceAccountFile);
                return new FileInputStream(file);
            }

            log.warn("⚠️ Firebase service account file not found: {}", serviceAccountFile);
            return null;
            
        } catch (IOException e) {
            log.error("❌ Error loading Firebase service account: {}", e.getMessage());
            return null;
        }
    }
}
