package com.movieticket.dao;

import com.movieticket.model.SeatHold;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SeatHoldDAO — Manages temporary seat reservations with concurrency safety.
 * Uses DB-level INSERT to act as an atomic check-and-hold.
 */
public class SeatHoldDAO {

    private static final String STATUS_ACTIVE    = "ACTIVE";
    private static final String STATUS_RELEASED  = "RELEASED";
    private static final String STATUS_CONVERTED = "CONVERTED";
    private static final String STATUS_EXPIRED   = "EXPIRED";

    /**
     * Attempts to hold multiple seats for a user atomically.
     * Cleans up expired holds first, then tries INSERT for each seat.
     * Returns the list of seat IDs that could NOT be held (already held or booked).
     */
    public List<Integer> holdSeats(int showId, List<Integer> seatIds, int userId, int holdMinutes) {
        cleanupExpired(showId);

        List<Integer> failed = new ArrayList<>();
        String insertSql = "INSERT INTO seat_holds (show_id, seat_id, user_id, held_at, expires_at, status) " +
                           "SELECT ?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? MINUTE), 'ACTIVE' " +
                           "FROM DUAL " +
                           "WHERE NOT EXISTS (" +
                           "  SELECT 1 FROM seat_holds sh " +
                           "  WHERE sh.show_id = ? AND sh.seat_id = ? AND sh.status = 'ACTIVE' AND sh.expires_at > NOW()" +
                           ") AND NOT EXISTS (" +
                           "  SELECT 1 FROM booking_details bd " +
                           "  JOIN bookings b ON bd.booking_id = b.booking_id " +
                           "  JOIN shows s ON b.show_id = s.show_id " +
                           "  WHERE s.show_id = ? AND bd.seat_id = ? AND b.status = 'CONFIRMED'" +
                           ")";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                for (int seatId : seatIds) {
                    stmt.setInt(1, showId);
                    stmt.setInt(2, seatId);
                    stmt.setInt(3, userId);
                    stmt.setInt(4, holdMinutes);
                    stmt.setInt(5, showId);
                    stmt.setInt(6, seatId);
                    stmt.setInt(7, showId);
                    stmt.setInt(8, seatId);

                    int rows = stmt.executeUpdate();
                    if (rows == 0) {
                        failed.add(seatId);
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[SeatHoldDAO] holdSeats error: " + e.getMessage());
                return seatIds; // all failed
            }
        } catch (SQLException e) {
            System.err.println("[SeatHoldDAO] Connection error: " + e.getMessage());
            return seatIds;
        }
        return failed;
    }

    /**
     * Releases holds for a specific user/show.
     */
    public void releaseHold(int showId, List<Integer> seatIds, int userId) {
        if (seatIds == null || seatIds.isEmpty()) return;
        String sql = "UPDATE seat_holds SET status = 'RELEASED' " +
                     "WHERE show_id = ? AND user_id = ? AND status = 'ACTIVE' AND seat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int seatId : seatIds) {
                stmt.setInt(1, showId);
                stmt.setInt(2, userId);
                stmt.setInt(3, seatId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("[SeatHoldDAO] releaseHold error: " + e.getMessage());
        }
    }

    /**
     * Marks holds as CONVERTED when booking is confirmed.
     */
    public void convertToBooked(int showId, List<Integer> seatIds, int userId) {
        if (seatIds == null || seatIds.isEmpty()) return;
        String sql = "UPDATE seat_holds SET status = 'CONVERTED' " +
                     "WHERE show_id = ? AND user_id = ? AND status = 'ACTIVE' AND seat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int seatId : seatIds) {
                stmt.setInt(1, showId);
                stmt.setInt(2, userId);
                stmt.setInt(3, seatId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("[SeatHoldDAO] convertToBooked error: " + e.getMessage());
        }
    }

    /**
     * Cleans up expired holds for a show (lazy cleanup — called before new holds).
     */
    public void cleanupExpired(int showId) {
        String sql = "UPDATE seat_holds SET status = 'EXPIRED' " +
                     "WHERE show_id = ? AND status = 'ACTIVE' AND expires_at <= NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SeatHoldDAO] cleanupExpired error: " + e.getMessage());
        }
    }

    /**
     * Returns seat IDs that are actively held (by ANY user) for a given show.
     * Excludes expired holds.
     */
    public Set<Integer> getActiveHeldSeatIds(int showId) {
        Set<Integer> held = new HashSet<>();
        String sql = "SELECT seat_id FROM seat_holds " +
                     "WHERE show_id = ? AND status = 'ACTIVE' AND expires_at > NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    held.add(rs.getInt("seat_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[SeatHoldDAO] getActiveHeldSeatIds error: " + e.getMessage());
        }
        return held;
    }

    /**
     * Returns seat IDs held by a specific user for a show (still active).
     */
    public Set<Integer> getUserHeldSeatIds(int showId, int userId) {
        Set<Integer> held = new HashSet<>();
        String sql = "SELECT seat_id FROM seat_holds " +
                     "WHERE show_id = ? AND user_id = ? AND status = 'ACTIVE' AND expires_at > NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    held.add(rs.getInt("seat_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[SeatHoldDAO] getUserHeldSeatIds error: " + e.getMessage());
        }
        return held;
    }

    /**
     * Gets the expiry time for a user's hold on a show.
     */
    public Optional<LocalDateTime> getHoldExpiry(int showId, int userId) {
        String sql = "SELECT MIN(expires_at) FROM seat_holds " +
                     "WHERE show_id = ? AND user_id = ? AND status = 'ACTIVE' AND expires_at > NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp(1);
                    if (ts != null) return Optional.of(ts.toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            System.err.println("[SeatHoldDAO] getHoldExpiry error: " + e.getMessage());
        }
        return Optional.empty();
    }
}
