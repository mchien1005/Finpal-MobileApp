package com.example.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String hashedPassword = encoder.encode(rawPassword);
        System.out.println("Password: " + rawPassword);
        System.out.println("Hashed: " + hashedPassword);

        // Test verify
        boolean matches = encoder.matches(rawPassword, hashedPassword);
        System.out.println("Matches: " + matches);
    }
}
