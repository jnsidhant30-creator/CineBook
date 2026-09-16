package com.movieticket.dao;

import com.movieticket.model.Notification;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User Notifications.
 * Handles notification creation, retrieval, unread counting, marking as read, and automated triggers.
 */
public class NotificationDAO {

    public NotificationDAO() {
        ensureTableExists();
    }

    /**
     * Ensures the notifications table exists in MySQL database automatically.
     */
    private void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS notifications (" +
                     "notification_id INT PRIMARY KEY AUTO_INCREMENT, " +
                     "user_id INT NOT NULL, " +
                     "title VARCHAR(120) NOT NULL, " +
                     "message TEXT NOT NULL, " +
                     "notification_type VARCHAR(50) DEFAULT 'GENERAL', " +
                     "is_read BOOLEAN DEFAULT FALSE, " +
                     "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                     "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                     ")";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Notice: Error checking notifications table: " + e.getMessage());
        }
    }

    public boolean createNotification(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, title, message, notification_type, is_read, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getTitle());
            stmt.setString(3, notification.getMessage());
            stmt.setString(4, notification.getNotificationType());
            stmt.setBoolean(5, notification.isReadStatus());
            stmt.setTimestamp(6, Timestamp.valueOf(notification.getCreatedAt() != null ? notification.getCreatedAt() : LocalDateTime.now()));

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error creating notification: " + e.getMessage());
            return false;
        }
    }

    public List<Notification> getUserNotifications(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC, notification_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error fetching notifications: " + e.getMessage());
        }
        return list;
    }

    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error counting unread notifications: " + e.getMessage());
        }
        return 0;
    }

    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error marking notification as read: " + e.getMessage());
            return false;
        }
    }

    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error marking all notifications as read: " + e.getMessage());
            return false;
        }
    }

    /**
     * Broadcasts a "New Movie Available" notification to all registered customers when a new movie is added.
     */
    public void notifyAllUsersAboutNewMovie(String movieTitle) {
        String fetchUsersSql = "SELECT user_id FROM users WHERE UPPER(role) = 'USER'";
        String insertNotifSql = "INSERT INTO notifications (user_id, title, message, notification_type, is_read, created_at) VALUES (?, ?, ?, 'NEW_MOVIE', FALSE, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(fetchUsersSql)) {

            List<Integer> userIds = new ArrayList<>();
            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }

            if (!userIds.isEmpty()) {
                try (PreparedStatement pStmt = conn.prepareStatement(insertNotifSql)) {
                    for (int uId : userIds) {
                        pStmt.setInt(1, uId);
                        pStmt.setString(2, "New Movie Available");
                        pStmt.setString(3, "\"" + movieTitle + "\" is now available for booking! Check showtimes and reserve your seats.");
                        pStmt.addBatch();
                    }
                    pStmt.executeBatch();
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error notifying users about new movie: " + e.getMessage());
        }
    }

    /**
     * Checks if user has shows today/tomorrow and generates show reminders if not already notified.
     */
    public void generateShowRemindersForUser(int userId) {
        String sql = "SELECT DISTINCT b.booking_id, m.title, t.theatre_name, s.show_date, s.show_time " +
                     "FROM bookings b " +
                     "JOIN shows s ON b.show_id = s.show_id " +
                     "JOIN movies m ON s.movie_id = m.movie_id " +
                     "JOIN theatres t ON s.theatre_id = t.theatre_id " +
                     "WHERE b.user_id = ? AND b.status = 'CONFIRMED' AND s.show_date >= CURRENT_DATE " +
                     "AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id = ? AND n.notification_type = 'SHOW_REMINDER' AND n.message LIKE CONCAT('%CB-', b.booking_id, '%'))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int bookingId = rs.getInt("booking_id");
                    String title = rs.getString("title");
                    String theatre = rs.getString("theatre_name");
                    String date = rs.getString("show_date");
                    String time = rs.getString("show_time");

                    Notification notif = new Notification(
                            userId,
                            "Show Reminder",
                            "Your upcoming movie \"" + title + "\" is scheduled for " + date + " at " + time + " (" + theatre + "). Booking ID: CB-" + bookingId,
                            "SHOW_REMINDER"
                    );
                    createNotification(notif);
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error generating show reminders: " + e.getMessage());
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        return new Notification(
                rs.getInt("notification_id"),
                rs.getInt("user_id"),
                rs.getString("title"),
                rs.getString("message"),
                rs.getString("notification_type"),
                rs.getBoolean("is_read"),
                createdAt
        );
    }
}
