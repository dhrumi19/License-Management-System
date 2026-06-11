package com.licensify.app.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for hashing and validating passwords using SHA-256 and a random salt.
 */
public class PasswordHasher {

    private static final int SALT_LENGTH = 16;

    /**
     * Hashes a plain-text password using SHA-256 and a secure random salt.
     * 
     * @param password The plain-text password to hash.
     * @return A string formatted as "salt:hash" encoded in Base64.
     */
    public static String hashPassword(String password) {
        try {
            // Generate a secure random salt
            SecureRandom sr = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            sr.nextBytes(salt);
            String saltStr = Base64.getEncoder().encodeToString(salt);

            // Hash password + salt
            String hashStr = hashWithSalt(password, saltStr);

            return saltStr + ":" + hashStr;
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verifies if a plain-text password matches the stored hashed password.
     * 
     * @param password The plain-text password to verify.
     * @param storedPasswordHash The stored password hash in "salt:hash" format.
     * @return true if the password is correct, false otherwise.
     */
    public static boolean verifyPassword(String password, String storedPasswordHash) {
        if (storedPasswordHash == null || !storedPasswordHash.contains(":")) {
            return false;
        }

        String[] parts = storedPasswordHash.split(":", 2);
        String salt = parts[0];
        String storedHash = parts[1];

        String computedHash = hashWithSalt(password, salt);
        return computedHash.equals(storedHash);
    }

    /**
     * Helper to compute SHA-256 of salt + password.
     */
    private static String hashWithSalt(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
