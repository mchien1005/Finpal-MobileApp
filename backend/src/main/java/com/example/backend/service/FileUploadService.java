package com.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${app.upload.dir:${APP_UPLOAD_DIR:uploads}}")
    private String uploadDir;

    @Value("${app.upload.avatar-dir:avatars}")
    private String avatarDir;

    /**
     * Get absolute upload path
     * Hỗ trợ cả đường dẫn tuyệt đối (Docker: /app/uploads)
     * và tương đối (local: uploads)
     */
    private Path getUploadPath() {
        Path uploadPath = Paths.get(uploadDir);

        // Nếu đường dẫn đã là tuyệt đối (Docker: /app/uploads), sử dụng trực tiếp
        if (uploadPath.isAbsolute()) {
            return uploadPath;
        }

        // Nếu là đường dẫn tương đối, resolve từ thư mục hiện tại
        Path currentPath = Paths.get("").toAbsolutePath();
        return currentPath.resolve(uploadDir);
    }

    /**
     * Upload avatar cho user
     * 
     * @param file   File ảnh upload
     * @param userId ID của user
     * @return URL của avatar đã upload
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh (jpg, png, gif)");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File không được vượt quá 5MB");
        }

        // Create upload directory if not exists
        Path uploadPath = getUploadPath().resolve(avatarDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        System.out.println("Upload path: " + uploadPath.toAbsolutePath());

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        // Save file
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("File saved to: " + filePath.toAbsolutePath());

        // Return relative URL
        return "/uploads/" + avatarDir + "/" + newFilename;
    }

    /**
     * Xóa avatar cũ
     * 
     * @param avatarUrl URL của avatar cần xóa
     */
    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }

        try {
            // Extract filename from URL
            String filename = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
            Path filePath = getUploadPath().resolve(avatarDir).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't throw
            System.err.println("Error deleting avatar: " + e.getMessage());
        }
    }
}
