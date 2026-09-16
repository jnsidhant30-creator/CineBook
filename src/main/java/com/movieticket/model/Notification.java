package com.movieticket.model;

import java.time.LocalDateTime;

/**
 * Notification model representing user-side system notifications.
 */
public class Notification {

    private int notificationId;
    private int userId;
    private String title;
    private String message;
    private String notificationType; // BOOKING_CONFIRMED, BOOKING_CANCELLED, SHOW_REMINDER, SHOW_UPDATE, NEW_MOVIE, GENERAL
    private boolean readStatus;
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(int notificationId, int userId, String title, String message, String notificationType, boolean readStatus, LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.readStatus = readStatus;
        this.createdAt = createdAt;
    }

    public Notification(int userId, String title, String message, String notificationType) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.readStatus = false;
        this.createdAt = LocalDateTime.now();
    }

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public boolean isReadStatus() { return readStatus; }
    public void setReadStatus(boolean readStatus) { this.readStatus = readStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
