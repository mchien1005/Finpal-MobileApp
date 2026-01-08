package com.example.backend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class để mã hóa/giải mã dữ liệu nhạy cảm (như nội dung SMS)
 * Sử dụng AES-256-GCM encryption
 */
@Component
@Slf4j
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 12 bytes cho GCM
    private static final int GCM_TAG_LENGTH = 128; // 128 bits cho authentication tag

    @Value("${encryption.secret-key:Finpal2025SecretKey32BytesLong!}")
    private String secretKey;

    /**
     * Mã hóa chuỗi text
     * @param plainText Chuỗi cần mã hóa
     * @return Chuỗi đã mã hóa (Base64 encoded: IV + ciphertext)
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }

        try {
            // Tạo IV ngẫu nhiên
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // Tạo cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(getKeyBytes(), ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            // Mã hóa
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Kết hợp IV + encrypted data
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Lỗi mã hóa: {}", e.getMessage());
            // Fallback: trả về text đã obfuscate đơn giản nếu encryption lỗi
            return obfuscate(plainText);
        }
    }

    /**
     * Giải mã chuỗi đã mã hóa
     * @param encryptedText Chuỗi đã mã hóa (Base64)
     * @return Chuỗi gốc
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return null;
        }

        try {
            // Giải mã Base64
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            // Tách IV và encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedBytes = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encryptedBytes, 0, encryptedBytes.length);

            // Tạo cipher để giải mã
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(getKeyBytes(), ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            // Giải mã
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Lỗi giải mã: {}", e.getMessage());
            return encryptedText; // Trả về text gốc nếu không giải mã được
        }
    }

    /**
     * Lấy key bytes (đảm bảo 32 bytes cho AES-256)
     */
    private byte[] getKeyBytes() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[32]; // 256 bits
        System.arraycopy(keyBytes, 0, result, 0, Math.min(keyBytes.length, 32));
        return result;
    }

    /**
     * Obfuscate text đơn giản (fallback nếu encryption lỗi)
     * Ẩn số tài khoản, số tiền, v.v.
     */
    private String obfuscate(String text) {
        // Ẩn số tài khoản (giữ 4 số đầu và 4 số cuối)
        String obfuscated = text.replaceAll("(\\d{4})\\d{4,}(\\d{4})", "$1****$2");
        // Encode base64
        return "[OBFUSCATED]" + Base64.getEncoder().encodeToString(obfuscated.getBytes(StandardCharsets.UTF_8));
    }
}
