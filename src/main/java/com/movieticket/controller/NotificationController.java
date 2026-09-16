package com.movieticket.controller;

import com.movieticket.dao.NotificationDAO;
import com.movieticket.model.Notification;
import com.movieticket.view.components.NotificationDialog;

import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller managing User Notifications lifecycle, async database execution,
 * unread counter updates, and NotificationDialog modal lifecycle.
 */
public class NotificationController {

    private final NotificationDAO notificationDAO;
    private NotificationDialog notificationDialog;

    public NotificationController() {
        this.notificationDAO = new NotificationDAO();
    }

    public NotificationController(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    /**
     * Asynchronously loads unread count and notifies UI badge callback.
     */
    public void updateUnreadBadgeCount(int userId, Consumer<Integer> badgeCallback) {
        if (userId <= 0) return;
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                notificationDAO.generateShowRemindersForUser(userId);
                return notificationDAO.getUnreadCount(userId);
            }

            @Override
            protected void done() {
                try {
                    int unread = get();
                    if (badgeCallback != null) {
                        badgeCallback.accept(unread);
                    }
                } catch (Exception e) {
                    System.err.println("[NotificationController] Error fetching unread count: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Opens the NotificationDialog modal and loads current user notifications.
     */
    public void openNotificationDialog(JFrame owner, int userId, Runnable onBadgeUpdated) {
        if (userId <= 0) return;

        if (notificationDialog == null || notificationDialog.getOwner() != owner) {
            notificationDialog = new NotificationDialog(owner, "CineBook Notifications");
            notificationDialog.setOnNotificationClick(n -> {
                if (!n.isReadStatus()) {
                    markNotificationAsRead(n.getNotificationId(), userId, () -> {
                        loadDialogData(userId, onBadgeUpdated);
                    });
                }
            });
            notificationDialog.setOnMarkAllRead(() -> {
                markAllNotificationsAsRead(userId, () -> {
                    loadDialogData(userId, onBadgeUpdated);
                });
            });
            notificationDialog.setOnRefreshRequested(() -> {
                loadDialogData(userId, onBadgeUpdated);
            });
        }

        loadDialogData(userId, onBadgeUpdated);
        notificationDialog.setLocationRelativeTo(owner);
        notificationDialog.setVisible(true);
    }

    private void loadDialogData(int userId, Runnable onBadgeUpdated) {
        SwingWorker<List<Notification>, Void> worker = new SwingWorker<>() {
            private int unreadCount = 0;

            @Override
            protected List<Notification> doInBackground() {
                notificationDAO.generateShowRemindersForUser(userId);
                unreadCount = notificationDAO.getUnreadCount(userId);
                return notificationDAO.getUserNotifications(userId);
            }

            @Override
            protected void done() {
                try {
                    List<Notification> list = get();
                    if (notificationDialog != null) {
                        notificationDialog.setNotifications(list, unreadCount);
                    }
                    if (onBadgeUpdated != null) {
                        onBadgeUpdated.run();
                    }
                } catch (Exception e) {
                    System.err.println("[NotificationController] Error loading notification dialog data: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    public void markNotificationAsRead(int notificationId, int userId, Runnable onComplete) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return notificationDAO.markAsRead(notificationId);
            }

            @Override
            protected void done() {
                if (onComplete != null) onComplete.run();
            }
        };
        worker.execute();
    }

    public void markAllNotificationsAsRead(int userId, Runnable onComplete) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return notificationDAO.markAllAsRead(userId);
            }

            @Override
            protected void done() {
                if (onComplete != null) onComplete.run();
            }
        };
        worker.execute();
    }

    /**
     * Helper to automatically create a Booking Confirmation notification.
     */
    public void sendBookingConfirmation(int userId, int bookingId, String movieTitle, String seats, String date, String time) {
        String msg = "Your booking for \"" + movieTitle + "\" has been confirmed!\n" +
                     "Booking ID: CB-" + bookingId + "\n" +
                     "Seats: " + seats + "\n" +
                     "Showtime: " + date + " at " + time;
        Notification n = new Notification(userId, "Booking Confirmed", msg, "BOOKING_CONFIRMED");
        createNotificationAsync(n);
    }

    /**
     * Helper to automatically create a Booking Cancellation notification.
     */
    public void sendBookingCancellation(int userId, int bookingId, String movieTitle) {
        String msg = "Your booking CB-" + bookingId + " for \"" + movieTitle + "\" has been cancelled successfully.";
        Notification n = new Notification(userId, "Booking Cancelled", msg, "BOOKING_CANCELLED");
        createNotificationAsync(n);
    }

    /**
     * Helper to broadcast a New Movie Available notification to all registered customers.
     */
    public void broadcastNewMovie(String movieTitle) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                notificationDAO.notifyAllUsersAboutNewMovie(movieTitle);
                return null;
            }
        };
        worker.execute();
    }

    private void createNotificationAsync(Notification n) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return notificationDAO.createNotification(n);
            }
        };
        worker.execute();
    }
}
