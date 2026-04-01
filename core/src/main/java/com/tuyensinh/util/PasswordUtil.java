package com.tuyensinh.util;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    private static final int BCRYPT_ROUNDS = 10;

    public static String hashPassword(String rawPassword) {
        // Su dung BCrypt-style hash (manual implementation for compatibility)
        // Trong production nen dung jBCrypt hoac Spring Security
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Add salt
            SecureRandom sr = new SecureRandom();
            byte[] salt = new byte[16];
            sr.nextBytes(salt);
            md.update(salt);
            byte[] hashed = md.digest(rawPassword.getBytes("UTF-8"));
            String saltStr = Base64.getEncoder().encodeToString(salt);
            String hashStr = Base64.getEncoder().encodeToString(hashed);
            return "$2a$10$" + saltStr + "." + hashStr;
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean checkPassword(String rawPassword, String storedHash) {
        // Simple check - trong production dung BCrypt.compare
        if (storedHash == null || storedHash.isEmpty()) return false;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Extract salt from stored hash (simplified)
            if (storedHash.startsWith("$2a$10$")) {
                String[] parts = storedHash.substring(7).split("\\.");
                if (parts.length == 2) {
                    byte[] salt = Base64.getDecoder().decode(parts[0]);
                    md.update(salt);
                    byte[] hashed = md.digest(rawPassword.getBytes("UTF-8"));
                    String computed = Base64.getEncoder().encodeToString(hashed);
                    return computed.equals(parts[1]);
                }
            }
            // Fallback: direct SHA-256 comparison
            byte[] hashed = md.digest(rawPassword.getBytes("UTF-8"));
            String hashStr = Base64.getEncoder().encodeToString(hashed);
            return hashStr.equals(storedHash);
        } catch (Exception e) {
            return false;
        }
    }

    public static BigDecimal safeToDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double safeToDouble(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
