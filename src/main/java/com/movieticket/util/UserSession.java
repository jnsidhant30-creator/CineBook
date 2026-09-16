package com.movieticket.util;

import com.movieticket.model.User;

/**
 * Singleton/Thread-safe session utility to store the currently logged-in user.
 * Passwords are not stored in the session for security reasons.
 */
public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void startSession(User user) {
        // Create a safe copy without password
        this.currentUser = new User(user.getUserId(), user.getUsername(), null, user.getRole());
    }

    public void clearSession() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isActive() {
        return currentUser != null;
    }

    /** Convenience — returns current user's ID, or 0 if no session. */
    public int getUserId() {
        return currentUser != null ? currentUser.getUserId() : 0;
    }

    /** Convenience — returns current user's role, or null if no session. */
    public String getRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    /** Convenience — returns current user's display name, or "Unknown" if no session. */
    public String getDisplayName() {
        return currentUser != null ? currentUser.getUsername() : "Unknown";
    }
}
