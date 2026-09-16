package com.movieticket.dao;

import com.movieticket.model.CinePointsTransaction;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CinePointsDAO {

    public int getBalance(int userId) {
        String sql = "SELECT points_balance FROM cinepoints_wallet WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("points_balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean addPoints(int userId, int points, int bookingId, Integer showId, LocalDateTime showStartTime, String status, String description) {
        String insertWallet = "INSERT INTO cinepoints_wallet (user_id, points_balance) VALUES (?, ?) " +
                              "ON DUPLICATE KEY UPDATE points_balance = points_balance + ?";
        String insertTx = "INSERT INTO cinepoints_transactions (user_id, booking_id, points, transaction_type, description, balance_after, show_id, status, show_start_time) " +
                          "VALUES (?, ?, ?, 'EARNED', ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update wallet only if AVAILABLE
                if ("AVAILABLE".equals(status)) {
                    try (PreparedStatement stmt = conn.prepareStatement(insertWallet)) {
                        stmt.setInt(1, userId);
                        stmt.setInt(2, points);
                        stmt.setInt(3, points);
                        stmt.executeUpdate();
                    }
                }

                // Get new balance
                int newBalance = getBalanceInternal(conn, userId);

                // Insert transaction
                try (PreparedStatement stmt = conn.prepareStatement(insertTx)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, bookingId);
                    stmt.setInt(3, points);
                    stmt.setString(4, description);
                    stmt.setInt(5, newBalance);
                    
                    if (showId != null) {
                        stmt.setInt(6, showId);
                    } else {
                        stmt.setNull(6, Types.INTEGER);
                    }
                    stmt.setString(7, status);
                    
                    if (showStartTime != null) {
                        stmt.setTimestamp(8, Timestamp.valueOf(showStartTime));
                    } else {
                        stmt.setNull(8, Types.TIMESTAMP);
                    }
                    
                    stmt.executeUpdate();
                }
                
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean redeemPoints(int userId, int points, int bookingId, String description) {
        String updateWallet = "UPDATE cinepoints_wallet SET points_balance = points_balance - ? WHERE user_id = ? AND points_balance >= ?";
        String insertTx = "INSERT INTO cinepoints_transactions (user_id, booking_id, points, transaction_type, description, balance_after) " +
                          "VALUES (?, ?, ?, 'REDEEMED', ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update wallet
                int rows = 0;
                try (PreparedStatement stmt = conn.prepareStatement(updateWallet)) {
                    stmt.setInt(1, points);
                    stmt.setInt(2, userId);
                    stmt.setInt(3, points);
                    rows = stmt.executeUpdate();
                }

                if (rows == 0) {
                    // Insufficient points
                    conn.rollback();
                    return false;
                }

                // Get new balance
                int newBalance = getBalanceInternal(conn, userId);

                // Insert transaction
                try (PreparedStatement stmt = conn.prepareStatement(insertTx)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, bookingId);
                    stmt.setInt(3, points);
                    stmt.setString(4, description);
                    stmt.setInt(5, newBalance);
                    stmt.executeUpdate();
                }
                
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getBalanceInternal(Connection conn, int userId) throws SQLException {
        String sql = "SELECT points_balance FROM cinepoints_wallet WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("points_balance");
            }
        }
        return 0;
    }

    public List<CinePointsTransaction> getTransactionHistory(int userId) {
        List<CinePointsTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM cinepoints_transactions WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CinePointsTransaction tx = new CinePointsTransaction();
                    tx.setId(rs.getInt("id"));
                    tx.setUserId(rs.getInt("user_id"));
                    tx.setBookingId(rs.getInt("booking_id"));
                    tx.setPoints(rs.getInt("points"));
                    tx.setTransactionType(rs.getString("transaction_type"));
                    tx.setDescription(rs.getString("description"));
                    tx.setBalanceAfter(rs.getInt("balance_after"));
                    tx.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    int sId = rs.getInt("show_id");
                    if (!rs.wasNull()) tx.setShowId(sId);
                    tx.setStatus(rs.getString("status"));
                    Timestamp st = rs.getTimestamp("show_start_time");
                    if (st != null) tx.setShowStartTime(st.toLocalDateTime());
                    list.add(tx);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean checkBookingRewarded(int bookingId) {
        String sql = "SELECT COUNT(*) FROM cinepoints_transactions WHERE booking_id = ? AND transaction_type = 'EARNED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void reconcilePendingPoints(int userId) {
        String selectPending = "SELECT id, points FROM cinepoints_transactions WHERE user_id = ? AND status = 'PENDING' AND show_start_time <= NOW()";
        String updateStatus = "UPDATE cinepoints_transactions SET status = 'AVAILABLE', balance_after = ? WHERE id = ?";
        String updateWallet = "INSERT INTO cinepoints_wallet (user_id, points_balance) VALUES (?, ?) " +
                              "ON DUPLICATE KEY UPDATE points_balance = points_balance + ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean hasUpdates = false;
                List<Integer> txIds = new ArrayList<>();
                List<Integer> txPoints = new ArrayList<>();
                
                try (PreparedStatement stmt = conn.prepareStatement(selectPending)) {
                    stmt.setInt(1, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            txIds.add(rs.getInt("id"));
                            txPoints.add(rs.getInt("points"));
                        }
                    }
                }
                
                for (int i = 0; i < txIds.size(); i++) {
                    int txId = txIds.get(i);
                    int pts = txPoints.get(i);
                    
                    // Add to wallet
                    try (PreparedStatement stmt = conn.prepareStatement(updateWallet)) {
                        stmt.setInt(1, userId);
                        stmt.setInt(2, pts);
                        stmt.setInt(3, pts);
                        stmt.executeUpdate();
                    }
                    
                    int newBalance = getBalanceInternal(conn, userId);
                    
                    // Update tx
                    try (PreparedStatement stmt = conn.prepareStatement(updateStatus)) {
                        stmt.setInt(1, newBalance);
                        stmt.setInt(2, txId);
                        stmt.executeUpdate();
                    }
                    hasUpdates = true;
                }
                
                if (hasUpdates) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancelPendingPoints(int bookingId) {
        String updateStatus = "UPDATE cinepoints_transactions SET status = 'CANCELLED' WHERE booking_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateStatus)) {
            stmt.setInt(1, bookingId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
