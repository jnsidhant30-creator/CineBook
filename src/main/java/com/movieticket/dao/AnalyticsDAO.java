package com.movieticket.dao;

import com.movieticket.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AnalyticsDAO — Provides aggregated analytics data for the Admin Dashboard.
 * All queries are optimized with GROUP BY and indexed joins.
 */
public class AnalyticsDAO {

    // ─── Summary KPIs ────────────────────────────────────────────────────────

    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bookings WHERE status = 'CONFIRMED'";
        return fetchDecimal(sql);
    }

    public BigDecimal getRevenueToday() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bookings WHERE status = 'CONFIRMED' AND DATE(booking_date) = CURDATE()";
        return fetchDecimal(sql);
    }

    public BigDecimal getRevenueThisMonth() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bookings WHERE status = 'CONFIRMED' AND MONTH(booking_date) = MONTH(CURDATE()) AND YEAR(booking_date) = YEAR(CURDATE())";
        return fetchDecimal(sql);
    }

    public int getTotalConfirmedBookings() {
        String sql = "SELECT COUNT(*) FROM bookings WHERE status = 'CONFIRMED'";
        return fetchInt(sql);
    }

    public int getBookingsToday() {
        String sql = "SELECT COUNT(*) FROM bookings WHERE DATE(booking_date) = CURDATE()";
        return fetchInt(sql);
    }

    public int getTotalTicketsSold() {
        String sql = "SELECT COALESCE(SUM(num_seats), 0) FROM bookings WHERE status = 'CONFIRMED'";
        return fetchInt(sql);
    }

    // ─── Trend Data ─────────────────────────────────────────────────────────

    /**
     * Returns daily revenue for the last N days. Key = date string "YYYY-MM-DD", Value = revenue.
     */
    public Map<String, BigDecimal> getDailyRevenue(int lastDays) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        String sql = "SELECT DATE(booking_date) AS booking_day, COALESCE(SUM(total_amount), 0) AS revenue " +
                     "FROM bookings WHERE status = 'CONFIRMED' AND booking_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                     "GROUP BY booking_day ORDER BY booking_day ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lastDays);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("booking_day"), rs.getBigDecimal("revenue"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] getDailyRevenue error: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns daily booking counts for the last N days.
     */
    public Map<String, Integer> getDailyBookingCounts(int lastDays) {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT DATE(booking_date) AS booking_day, COUNT(*) AS cnt " +
                     "FROM bookings WHERE booking_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                     "GROUP BY booking_day ORDER BY booking_day ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lastDays);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("booking_day"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] getDailyBookingCounts error: " + e.getMessage());
        }
        return result;
    }

    // ─── Top Lists ──────────────────────────────────────────────────────────

    /**
     * Returns top N movies by ticket sales. Key = movie title, Value = tickets sold.
     */
    public Map<String, Integer> getTopMoviesByTickets(int topN) {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT m.title, COALESCE(SUM(b.num_seats), 0) AS tickets_sold " +
                     "FROM bookings b JOIN shows s ON b.show_id = s.show_id " +
                     "JOIN movies m ON s.movie_id = m.movie_id " +
                     "WHERE b.status = 'CONFIRMED' GROUP BY m.movie_id, m.title " +
                     "ORDER BY tickets_sold DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, topN);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.put(rs.getString("title"), rs.getInt("tickets_sold"));
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] getTopMoviesByTickets error: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns top N movies by revenue. Key = movie title, Value = total revenue.
     */
    public Map<String, BigDecimal> getTopMoviesByRevenue(int topN) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        String sql = "SELECT m.title, COALESCE(SUM(b.total_amount), 0) AS revenue " +
                     "FROM bookings b JOIN shows s ON b.show_id = s.show_id " +
                     "JOIN movies m ON s.movie_id = m.movie_id " +
                     "WHERE b.status = 'CONFIRMED' GROUP BY m.movie_id, m.title " +
                     "ORDER BY revenue DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, topN);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.put(rs.getString("title"), rs.getBigDecimal("revenue"));
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] getTopMoviesByRevenue error: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns top N movies by average rating. Key = movie title, Value = average rating.
     */
    public Map<String, Double> getTopRatedMovies(int topN) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT m.title, AVG(r.rating) AS avg_rating " +
                     "FROM reviews r JOIN movies m ON r.movie_id = m.movie_id " +
                     "WHERE r.status = 'VISIBLE' " +
                     "GROUP BY m.movie_id, m.title " +
                     "ORDER BY avg_rating DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, topN);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("title"), Math.round(rs.getDouble("avg_rating") * 10.0) / 10.0);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] getTopRatedMovies error: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns theatre performance: Key = theatre name, Value = total revenue.
     */
    public Map<String, BigDecimal> getTheatreRevenue() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        String sql = "SELECT t.theatre_name, COALESCE(SUM(b.total_amount), 0) AS revenue " +
                     "FROM bookings b JOIN shows s ON b.show_id = s.show_id " +
                     "JOIN theatres t ON s.theatre_id = t.theatre_id " +
                     "WHERE b.status = 'CONFIRMED' " +
                     "GROUP BY t.theatre_id, t.theatre_name ORDER BY revenue DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) result.put(rs.getString("theatre_name"), rs.getBigDecimal("revenue"));
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] getTheatreRevenue error: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns occupancy percentage per theatre. Key = theatre name, Value = occupancy %.
     */
    public Map<String, Double> getTheatreOccupancy() {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT t.theatre_name, " +
                     "ROUND(100.0 * COALESCE(SUM(b.num_seats), 0) / NULLIF(t.total_seats, 0), 1) AS occupancy_pct " +
                     "FROM theatres t " +
                     "LEFT JOIN shows s ON s.theatre_id = t.theatre_id " +
                     "LEFT JOIN bookings b ON b.show_id = s.show_id AND b.status = 'CONFIRMED' " +
                     "GROUP BY t.theatre_id, t.theatre_name, t.total_seats " +
                     "ORDER BY occupancy_pct DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) result.put(rs.getString("theatre_name"), rs.getDouble("occupancy_pct"));
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] getTheatreOccupancy error: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns booking counts grouped by seat type (REGULAR, PREMIUM, VIP).
     */
    public Map<String, Integer> getBookingsBySeatType() {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT seat_type, COUNT(*) AS cnt FROM booking_details " +
                     "JOIN bookings b ON booking_details.booking_id = b.booking_id " +
                     "WHERE b.status = 'CONFIRMED' GROUP BY seat_type ORDER BY cnt DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) result.put(rs.getString("seat_type"), rs.getInt("cnt"));
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] getBookingsBySeatType error: " + e.getMessage());
        }
        return result;
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private BigDecimal fetchDecimal(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] fetchDecimal error: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private int fetchInt(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] fetchInt error: " + e.getMessage());
        }
        return 0;
    }
}
