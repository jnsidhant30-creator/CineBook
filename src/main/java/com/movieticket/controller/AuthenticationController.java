package com.movieticket.controller;

import com.movieticket.dao.UserDAO;
import com.movieticket.model.User;
import com.movieticket.util.AuditService;
import com.movieticket.util.PasswordUtil;
import com.movieticket.util.UserSession;
import com.movieticket.view.AdminDashboardFrame;
import com.movieticket.view.LoginFrame;
import com.movieticket.view.RegisterFrame;
import com.movieticket.view.UserDashboardFrame;

import javax.swing.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AuthenticationController {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DELAY_MS    = 3000; // 3-second delay after 5 failed attempts

    /** Tracks per-username failed attempt count. Cleared on success. */
    private static final Map<String, AtomicInteger> failedAttempts = new ConcurrentHashMap<>();

    private final UserDAO userDAO;
    private LoginFrame loginFrame;
    private RegisterFrame registerFrame;
    private AdminDashboardFrame adminDashboardFrame;
    private UserDashboardFrame userDashboardFrame;

    public AuthenticationController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void startApplication() {
        SwingUtilities.invokeLater(() -> {
            loginFrame = new LoginFrame();
            initLoginListeners();
            loginFrame.setVisible(true);
        });
    }

    private void initLoginListeners() {
        loginFrame.getLoginButton().addActionListener(e -> handleLogin());

        loginFrame.getClearButton().addActionListener(e -> loginFrame.clearFields());

        loginFrame.getRegisterButton().addActionListener(e -> {
            loginFrame.setVisible(false);
            if (registerFrame == null) {
                registerFrame = new RegisterFrame();
                initRegisterListeners();
            }
            registerFrame.clearFields();
            registerFrame.setVisible(true);
        });

        loginFrame.getExitButton().addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(loginFrame,
                    "Are you sure you want to exit the application?",
                    "Exit Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
    }

    private void initRegisterListeners() {
        registerFrame.getRegisterButton().addActionListener(e -> handleRegistration());

        registerFrame.getClearButton().addActionListener(e -> registerFrame.clearFields());

        registerFrame.getBackButton().addActionListener(e -> {
            registerFrame.setVisible(false);
            loginFrame.setVisible(true);
        });
    }

    private void handleLogin() {
        String username = loginFrame.getUsernameField().getText().trim();
        String password = new String(loginFrame.getPasswordField().getPassword()).trim();

        // Validate fields
        if (username.isEmpty()) {
            loginFrame.showError("Please enter email or username.");
            return;
        }
        if (password.isEmpty()) {
            loginFrame.showError("Please enter your password.");
            return;
        }

        loginFrame.setLoading(true);

        // Run authentication in background to avoid blocking the UI
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Apply delay if user is rate-limited
                AtomicInteger counter = failedAttempts.getOrDefault(username, new AtomicInteger(0));
                if (counter.get() >= MAX_FAILED_ATTEMPTS) {
                    Thread.sleep(LOCKOUT_DELAY_MS);
                }
                Thread.sleep(400); // brief animation delay
                return null;
            }

            @Override
            protected void done() {
                try {
                    Optional<User> userOpt = userDAO.findByUsername(username);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        boolean passwordMatches = checkPassword(password, user.getPassword());

                        if (passwordMatches) {
                            // Reset failed counter on success
                            failedAttempts.remove(username);

                            // Start session (password NOT stored in session)
                            UserSession.getInstance().startSession(user);

                            // Log successful login
                            AuditService.logLogin(user.getUserId(), user.getRole(),
                                AuditService.LOGIN_SUCCESS, "User '" + username + "' logged in");

                            String role = user.getRole().toUpperCase();
                            if ("ADMIN".equals(role) || "USER".equals(role)) {
                                loginFrame.showSuccess(() -> {
                                    loginFrame.setVisible(false);
                                    loginFrame.clearFields();
                                    if ("ADMIN".equals(role)) {
                                        AdminDashboardFrame adminFrame = new AdminDashboardFrame();
                                        AdminDashboardController adminController = new AdminDashboardController(adminFrame);
                                        adminFrame.setVisible(true);
                                    } else {
                                        UserDashboardFrame userFrame = new UserDashboardFrame();
                                        UserDashboardController userController = new UserDashboardController(userFrame);
                                        userFrame.setVisible(true);
                                    }
                                });
                            } else {
                                loginFrame.showError("Invalid email or password.");
                            }
                        } else {
                            // Increment failed counter
                            failedAttempts.computeIfAbsent(username, k -> new AtomicInteger(0)).incrementAndGet();
                            // Log failed attempt (use 0 userId for unknown)
                            AuditService.logLogin(userOpt.map(User::getUserId).orElse(0),
                                userOpt.map(User::getRole).orElse("UNKNOWN"),
                                AuditService.LOGIN_FAILED,
                                "Failed login attempt for '" + username + "'");
                            loginFrame.showError("Invalid email or password.");
                        }
                    } else {
                        failedAttempts.computeIfAbsent(username, k -> new AtomicInteger(0)).incrementAndGet();
                        AuditService.logLogin(0, "UNKNOWN", AuditService.LOGIN_FAILED,
                            "Failed login attempt for unknown user '" + username + "'");
                        loginFrame.showError("Invalid email or password.");
                    }
                } catch (Exception ex) {
                    loginFrame.showError("Database Error.");
                    ex.printStackTrace();
                } finally {
                    loginFrame.setLoading(false);
                }
            }
        };
        worker.execute();
    }

    /**
     * Checks a supplied password against a stored value.
     * Supports both BCrypt hashes (new accounts) and plaintext (legacy accounts).
     * Never logs either value.
     */
    private boolean checkPassword(String supplied, String stored) {
        if (stored == null || supplied == null) return false;
        if (PasswordUtil.isBcryptHash(stored)) {
            return PasswordUtil.verifyPassword(supplied, stored);
        }
        // Legacy plaintext comparison for existing accounts
        return stored.equals(supplied);
    }

    private void handleRegistration() {
        String username = registerFrame.getUsernameField().getText().trim();
        String password = new String(registerFrame.getPasswordField().getPassword()).trim();
        String confirmPassword = new String(registerFrame.getConfirmPasswordField().getPassword()).trim();

        // Validation checks
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(registerFrame, "Please enter username.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(registerFrame, "Please enter password.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(registerFrame, "Please confirm your password.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(registerFrame, "Passwords do not match.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Check username uniqueness
            Optional<User> userOpt = userDAO.findByUsername(username);
            if (userOpt.isPresent()) {
                JOptionPane.showMessageDialog(registerFrame, "Username already exists.",
                        "Registration Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Hash the password using BCrypt for new accounts
            String hashedPassword = PasswordUtil.hashPassword(password);
            User newUser = new User(0, username, hashedPassword, "USER");
            int userId = userDAO.createUser(newUser);
            if (userId > 0) {
                AuditService.logLogin(userId, "USER", AuditService.USER_CREATED,
                    "New user registered: '" + username + "'");
                JOptionPane.showMessageDialog(registerFrame, "Registration successful. Please login.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                registerFrame.setVisible(false);
                loginFrame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(registerFrame, "Registration failed. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(registerFrame,
                    "Unable to connect to the database.\nPlease check that MySQL Server is running.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void handleLogout(JFrame currentDashboard) {
        int confirm = JOptionPane.showConfirmDialog(currentDashboard,
                "Are you sure you want to logout?",
                "Logout Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            UserSession.getInstance().clearSession();
            currentDashboard.setVisible(false);
            currentDashboard.dispose();
            loginFrame.setVisible(true);
        }
    }
}
