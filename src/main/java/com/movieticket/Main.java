package com.movieticket;

import com.movieticket.controller.AuthenticationController;
import com.movieticket.dao.UserDAO;

import javax.swing.*;

/**
 * Main.java — Application entry point.
 *
 * Sets Look & Feel, initializes UserDAO and AuthenticationController, and launches the login screen.
 */
public class Main {

    public static void main(String[] args) {

        // Set global Swing dark cinema theme
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            com.movieticket.util.ThemeManager.applyGlobalTheme();
        } catch (Exception e) {
            System.err.println("Could not set Look and Feel: " + e.getMessage());
        }

        // Launch the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            System.out.println("===========================================");
            System.out.println("  " + com.movieticket.util.ThemeManager.APP_DISPLAY_NAME + " — " + com.movieticket.util.ThemeManager.APP_SUBTITLE);
            System.out.println("  Version: 1.0-SNAPSHOT");
            System.out.println("  Java: " + System.getProperty("java.version"));
            System.out.println("===========================================");

            // Initialize DAO and Controller
            UserDAO userDAO = new UserDAO();
            AuthenticationController authController = new AuthenticationController(userDAO);

            // Start application at LoginFrame
            authController.startApplication();
        });

        // Seed 2026 Movies asynchronously to avoid blocking the GUI
        new Thread(() -> {
            com.movieticket.util.DatabaseSeeder.seed2026Movies();
        }).start();
    }
}
