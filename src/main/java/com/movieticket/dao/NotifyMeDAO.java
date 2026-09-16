package com.movieticket.dao;

import com.movieticket.util.DatabaseConnection;
import com.movieticket.model.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for managing Notify Me subscriptions.
 */
public class NotifyMeDAO {

    public NotifyMeDAO() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS movie_notifications (" +
                     "notification_id INT PRIMARY KEY AUTO_INCREMENT, " +
                     "user_id INT NOT NULL, " +
                     "movie_id INT NOT NULL, " +
                     "enabled BOOLEAN DEFAULT TRUE, " +
                     "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                     "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                     "FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE, " +
                     "UNIQUE KEY uk_user_movie_notification (user_id, movie_id)" +
                     ")";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("[NotifyMeDAO] Error creating table: " + e.getMessage());
        }
    }

    public boolean addNotificationPreference(int userId, int movieId) {
        String sql = "INSERT INTO movie_notifications (user_id, movie_id, enabled) VALUES (?, ?, TRUE) " +
                     "ON DUPLICATE KEY UPDATE enabled = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotifyMeDAO] Error adding notification preference: " + e.getMessage());
            return false;
        }
    }

    public boolean removeNotificationPreference(int userId, int movieId) {
        String sql = "UPDATE movie_notifications SET enabled = FALSE WHERE user_id = ? AND movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotifyMeDAO] Error removing notification preference: " + e.getMessage());
            return false;
        }
    }

    public boolean isUserSubscribed(int userId, int movieId) {
        String sql = "SELECT enabled FROM movie_notifications WHERE user_id = ? AND movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("enabled");
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotifyMeDAO] Error checking subscription status: " + e.getMessage());
        }
        return false;
    }

    public void notifySubscribedUsers(int movieId, String movieTitle) {
        String sql = "SELECT user_id FROM movie_notifications WHERE movie_id = ? AND enabled = TRUE";
        List<Integer> userIds = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotifyMeDAO] Error fetching subscribed users: " + e.getMessage());
            return;
        }

        if (userIds.isEmpty()) {
            return;
        }

        NotificationDAO notificationDAO = new NotificationDAO();
        for (int userId : userIds) {
            Notification notif = new Notification(
                    0,
                    userId,
                    "Movie Now Available!",
                    "\"" + movieTitle + "\" is now available for booking! Go check the showtimes and reserve your seats.",
                    "UPCOMING_MOVIE_AVAILABLE",
                    false,
                    null
            );
            notificationDAO.createNotification(notif);
        }

        // Disable notifications for this movie so they don't get spammed again
        String disableSql = "UPDATE movie_notifications SET enabled = FALSE WHERE movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(disableSql)) {
            stmt.setInt(1, movieId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[NotifyMeDAO] Error disabling sent notifications: " + e.getMessage());
        }
    }
}
