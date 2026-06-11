package com.licensify.app.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility for generating and validating cryptographically-verifiable license keys.
 * Format: LCS-XXXX-XXXX-XXXX-YYYY where X is random and Y is a signature checksum.
 */
public class LicenseGenerator {

    private static final String SECRET_SALT = "LICENSIFY-SEC-KEY-2026";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a new, uniquely formatted license key.
     */
    public static String generateLicenseKey() {
        String p1 = generateRandomGroup(4);
        String p2 = generateRandomGroup(4);
        String p3 = generateRandomGroup(4);
        
        String data = p1 + "-" + p2 + "-" + p3;
        String signature = calculateSignature(data);

        return "LCS-" + data + "-" + signature;
    }

    /**
     * Validates if the given license key has a valid checksum signature.
     * This is useful for client-side license checking without hitting the database.
     */
    public static boolean isValidKeyFormat(String licenseKey) {
        if (licenseKey == null) return false;
        
        // Expected format: LCS-XXXX-XXXX-XXXX-YYYY
        String[] parts = licenseKey.split("-");
        if (parts.length != 5) return false;

        if (!"LCS".equals(parts[0])) return false;

        String p1 = parts[1];
        String p2 = parts[2];
        String p3 = parts[3];
        String signature = parts[4];

        if (p1.length() != 4 || p2.length() != 4 || p3.length() != 4 || signature.length() != 4) {
            return false;
        }

        String data = p1 + "-" + p2 + "-" + p3;
        String computedSignature = calculateSignature(data);

        return computedSignature.equals(signature);
    }

    private static String generateRandomGroup(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    private static String calculateSignature(String data) {
        try {
            String combined = data + SECRET_SALT;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            // Return first 4 chars in uppercase
            return hexString.toString().substring(0, 4).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 hashing not available", e);
        }
    }
}
