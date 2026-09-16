package com.movieticket.util;

import com.movieticket.dao.AuditDAO;
import com.movieticket.model.AuditLog;

import javax.swing.*;

/**
 * AuditService — Non-blocking async wrapper for audit logging.
 * All operations fire via SwingWorker so the Swing EDT never stalls.
 *
 * Usage:
 *   AuditService.log("LOGIN_SUCCESS", "USER", userId, "User logged in");
 */
public class AuditService {

    private static final AuditDAO dao = new AuditDAO();

    // Audit action constants — add to this list as needed
    public static final String LOGIN_SUCCESS         = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED          = "LOGIN_FAILED";
    public static final String LOGOUT                = "LOGOUT";
    public static final String USER_CREATED          = "USER_CREATED";
    public static final String USER_UPDATED          = "USER_UPDATED";
    public static final String USER_BLOCKED          = "USER_BLOCKED";
    public static final String USER_UNBLOCKED        = "USER_UNBLOCKED";
    public static final String ROLE_CHANGED          = "ROLE_CHANGED";
    public static final String MOVIE_ADDED           = "MOVIE_ADDED";
    public static final String MOVIE_UPDATED         = "MOVIE_UPDATED";
    public static final String MOVIE_DELETED         = "MOVIE_DELETED";
    public static final String THEATRE_ADDED         = "THEATRE_ADDED";
    public static final String THEATRE_UPDATED       = "THEATRE_UPDATED";
    public static final String SHOW_CREATED          = "SHOW_CREATED";
    public static final String SHOW_UPDATED          = "SHOW_UPDATED";
    public static final String SEAT_BLOCKED          = "SEAT_BLOCKED";
    public static final String SEAT_UNBLOCKED        = "SEAT_UNBLOCKED";
    public static final String PRICE_UPDATED         = "PRICE_UPDATED";
    public static final String COUPON_CREATED        = "COUPON_CREATED";
    public static final String COUPON_UPDATED        = "COUPON_UPDATED";
    public static final String BOOKING_CANCELLED     = "BOOKING_CANCELLED";
    public static final String REFUND_CREATED        = "REFUND_CREATED";
    public static final String PAYMENT_STATUS_CHANGED = "PAYMENT_STATUS_CHANGED";
    public static final String REVIEW_MODERATED      = "REVIEW_MODERATED";
    public static final String OMDB_IMPORT           = "OMDB_IMPORT";
    public static final String UPCOMING_MOVIE_APPROVED = "UPCOMING_MOVIE_APPROVED";
    public static final String SEAT_HELD             = "SEAT_HELD";
    public static final String SEAT_RELEASED         = "SEAT_RELEASED";

    /**
     * Logs an audit event asynchronously using the current user session.
     *
     * @param action       The audit action constant (use AuditService.LOGIN_SUCCESS etc.)
     * @param entityType   The type of entity involved (e.g. "USER", "MOVIE", "BOOKING")
     * @param entityId     The ID of the entity (0 if not applicable)
     * @param description  Human-readable description of the event
     */
    public static void log(String action, String entityType, int entityId, String description) {
        UserSession session = UserSession.getInstance();
        int userId = session.getUserId();
        String role = session.getRole() != null ? session.getRole() : "SYSTEM";

        // Fire-and-forget async log — never blocks the UI thread
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    AuditLog entry = new AuditLog(userId, role, action, entityType, entityId, description);
                    dao.log(entry);
                } catch (Exception e) {
                    System.err.println("[AuditService] Failed to write audit log: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    /**
     * Logs a login event (before session is established — userId and role passed explicitly).
     */
    public static void logLogin(int userId, String role, String action, String description) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    AuditLog entry = new AuditLog(userId, role, action, "USER", userId, description);
                    dao.log(entry);
                } catch (Exception e) {
                    System.err.println("[AuditService] Failed to write login audit: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }
}
