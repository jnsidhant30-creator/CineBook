package com.movieticket.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtil — Secure BCrypt password hashing for new user registrations.
 *
 * Policy:
 * - New passwords are always hashed with BCrypt (work factor 12).
 * - Existing plaintext passwords are NOT automatically migrated (per design decision).
 * - Never log, display, or expose raw passwords or their hashes.
 */
public class PasswordUtil {

    /** BCrypt work factor — higher = slower = more secure */
    private static final int WORK_FACTOR = 12;

    /** Hashes a plaintext password using BCrypt. */
    public static String hashPassword(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     * Returns false (not an exception) if the hash is invalid.
     */
    public static boolean verifyPassword(String plaintext, String hash) {
        if (plaintext == null || hash == null) return false;
        try {
            return BCrypt.checkpw(plaintext, hash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Detects whether a stored password string is a BCrypt hash.
     * BCrypt hashes always start with "$2a$" or "$2b$".
     */
    public static boolean isBcryptHash(String password) {
        return password != null && (password.startsWith("$2a$") || password.startsWith("$2b$"));
    }
}
