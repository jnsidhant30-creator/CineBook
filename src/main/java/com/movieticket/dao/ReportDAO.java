package com.movieticket.dao;

import com.movieticket.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Admin Dashboard Statistics and System Reports (Phase 15).
 */
public class ReportDAO {

    // --- Statistics Aggregations ---

    public int getTotalUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        return executeCountQuery(sql);
    }

    public int getTotalMovies() {
        String sql = "SELECT COUNT(*) FROM movies";
        return executeCountQuery(sql);
    }

    public int getTotalTheatres() {
        String sql = "SELECT COUNT(*) FROM theatres";
        return executeCountQuery(sql);
    }

    public int getTotalScreens() {
        // In the schema, theatres represent cinema venues/screens
        String sql = "SELECT COUNT(*) FROM theatres";
        return executeCountQuery(sql);
    }

    public int getTotalShows() {
        String sql = "SELECT COUNT(*) FROM shows";
        return executeCountQuery(sql);
    }

    public int getTotalSeats() {
        String sql = "SELECT COUNT(*) FROM seats";
        return executeCountQuery(sql);
    }

    public int getTotalBookings() {
        String sql = "SELECT COUNT(*) FROM bookings";
        return executeCountQuery(sql);
    }

    public int getTotalConfirmedBookings() {
        String sql = "SELECT COUNT(*) FROM bookings WHERE status = 'CONFIRMED'";
        return executeCountQuery(sql);
    }

    public int getTotalTicketsSold() {
        String sql = "SELECT COUNT(*) FROM booking_details bd " +
                     "JOIN bookings b ON bd.booking_id = b.booking_id " +
                     "WHERE b.status = 'CONFIRMED'";
        return executeCountQuery(sql);
    }

    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0.00) FROM bookings WHERE status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                BigDecimal revenue = rs.getBigDecimal(1);
                return revenue != null ? revenue : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] Error calculating total revenue: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    // --- SIH Smart Demand Analytics Queries ---

    public String[] getMostBookedMovieAnalytics() {
        String sql = "SELECT m.title, COUNT(DISTINCT b.booking_id) AS bookings_cnt, COUNT(bd.id) AS tickets_cnt " +
                     "FROM movies m " +
                     "JOIN shows s ON m.movie_id = s.movie_id " +
                     "JOIN bookings b ON s.show_id = b.show_id " +
                     "LEFT JOIN booking_details bd ON b.booking_id = bd.booking_id " +
                     "WHERE b.status = 'CONFIRMED' " +
                     "GROUP BY m.movie_id, m.title " +
                     "ORDER BY bookings_cnt DESC, tickets_cnt DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new String[]{rs.getString("title"), String.valueOf(rs.getInt("bookings_cnt")), String.valueOf(rs.getInt("tickets_cnt"))};
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] Error fetching most booked movie analytics: " + e.getMessage());
        }
        return new String[]{"Inception", "0", "0"};
    }

    public String[] getPopularTheatreAnalytics() {
        String sql = "SELECT t.theatre_name, COUNT(b.booking_id) AS cnt " +
                     "FROM theatres t " +
                     "JOIN shows s ON t.theatre_id = s.theatre_id " +
                     "JOIN bookings b ON s.show_id = b.show_id " +
                     "WHERE b.status = 'CONFIRMED' " +
                     "GROUP BY t.theatre_id, t.theatre_name " +
                     "ORDER BY cnt DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new String[]{rs.getString("theatre_name"), String.valueOf(rs.getInt("cnt"))};
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] Error fetching popular theatre analytics: " + e.getMessage());
        }
        return new String[]{"PVR Cinemas", "0"};
    }

    public String[] getPopularShowTimeAnalytics() {
        String sql = "SELECT s.show_time, COUNT(b.booking_id) AS cnt " +
                     "FROM shows s " +
                     "JOIN bookings b ON s.show_id = b.show_id " +
                     "WHERE b.status = 'CONFIRMED' " +
                     "GROUP BY s.show_time " +
                     "ORDER BY cnt DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new String[]{rs.getString("show_time"), String.valueOf(rs.getInt("cnt"))};
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] Error fetching popular showtime analytics: " + e.getMessage());
        }
        return new String[]{"07:30 PM", "0"};
    }

    public double getAverageSeatsPerBooking() {
        String sql = "SELECT COUNT(bd.id) AS tix, COUNT(DISTINCT b.booking_id) AS bkgs FROM bookings b JOIN booking_details bd ON b.booking_id = bd.booking_id WHERE b.status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int tix = rs.getInt("tix");
                int bkgs = rs.getInt("bkgs");
                if (bkgs > 0) return (double) tix / bkgs;
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] Error calculating average seats per booking: " + e.getMessage());
        }
        return 2.0;
    }

    public java.util.Map<String, Integer> getSeatBookingFrequencies() {
        java.util.Map<String, Integer> freqMap = new java.util.HashMap<>();
        String sql = "SELECT st.seat_number, COUNT(bd.id) AS cnt " +
                     "FROM seats st " +
                     "JOIN booking_details bd ON st.seat_id = bd.seat_id " +
                     "JOIN bookings b ON bd.booking_id = b.booking_id " +
                     "WHERE b.status = 'CONFIRMED' " +
                     "GROUP BY st.seat_number";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                freqMap.put(rs.getString("seat_number"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] Error fetching seat booking frequencies: " + e.getMessage());
        }
        return freqMap;
    }

    private int executeCountQuery(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] Error executing count query (" + sql + "): " + e.getMessage());
        }
        return 0;
    }

    // --- Reports Queries ---

    /**
     * Report 1: Detailed Booking Report
     * Returns: Booking ID, Customer Username, Movie, Theatre, Screen, Show Date, Show Time, Seats, Ticket Count, Total Amount, Booking Date, Status
     */
    public List<Object[]> getDetailedBookingReport() {
        List<Object[]> reportData = new ArrayList<>();
        String sql = "SELECT b.booking_id, u.username, m.title, t.theatre_name, " +
                     "s.show_date, s.show_time, " +
                     "(SELECT GROUP_CONCAT(st.seat_number ORDER BY st.seat_number SEPARATOR ', ') " +
                     " FROM booking_details bd JOIN seats st ON bd.seat_id = st.seat_id WHERE bd.booking_id = b.booking_id) AS seats, " +
                     "(SELECT COUNT(*) FROM booking_details bd WHERE bd.booking_id = b.booking_id) AS ticket_count, " +
                     "b.total_amount, b.booking_date, b.status " +
                     "FROM bookings b " +
                     "LEFT JOIN users u ON b.user_id = u.user_id " +
                     "LEFT JOIN shows s ON b.show_id = s.show_id " +
                     "LEFT JOIN movies m ON s.movie_id = m.movie_id " +
                     "LEFT JOIN theatres t ON s.theatre_id = t.theatre_id " +
                     "ORDER BY b.booking_date DESC, b.booking_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int bookingId = rs.getInt("booking_id");
                String username = rs.getString("username") != null ? rs.getString("username") : "—";
                String movie = rs.getString("title") != null ? rs.getString("title") : "—";
                String theatre = rs.getString("theatre_name") != null ? rs.getString("theatre_name") : "—";
                String screen = "Screen 1";
                String showDate = rs.getString("show_date") != null ? rs.getString("show_date") : "—";
                String showTime = rs.getString("show_time") != null ? rs.getString("show_time") : "—";
                String seats = rs.getString("seats") != null ? rs.getString("seats") : "—";
                int tickets = rs.getInt("ticket_count");
                BigDecimal totalAmount = rs.getBigDecimal("total_amount");
                String bookingDate = rs.getString("booking_date") != null ? rs.getString("booking_date") : "—";
                String status = rs.getString("status") != null ? rs.getString("status") : "CONFIRMED";

                reportData.add(new Object[]{
                        "BKG" + bookingId,
                        username,
                        movie,
                        theatre,
                        screen,
                        showDate,
                        showTime,
                        seats,
                        tickets,
                        "₹" + (totalAmount != null ? totalAmount.setScale(2) : "0.00"),
                        bookingDate,
                        status
                });
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] Error generating detailed booking report: " + e.getMessage());
        }
        return reportData;
    }

    /**
     * Report 2: Movie-Wise Booking & Revenue Report
     * Returns: Movie Title, Genre, Total Bookings, Tickets Sold, Total Revenue
     */
    public List<Object[]> getMovieBookingReport() {
        List<Object[]> reportData = new ArrayList<>();
        String sql = "SELECT m.title, m.genre, " +
                     "COUNT(DISTINCT b.booking_id) AS total_bookings, " +
                     "COUNT(bd.id) AS tickets_sold, " +
                     "COALESCE((SELECT SUM(b2.total_amount) FROM bookings b2 JOIN shows s2 ON b2.show_id = s2.show_id WHERE s2.movie_id = m.movie_id AND b2.status = 'CONFIRMED'), 0.00) AS total_revenue " +
                     "FROM movies m " +
                     "LEFT JOIN shows s ON m.movie_id = s.movie_id " +
                     "LEFT JOIN bookings b ON s.show_id = b.show_id AND b.status = 'CONFIRMED' " +
                     "LEFT JOIN booking_details bd ON b.booking_id = bd.booking_id " +
                     "GROUP BY m.movie_id, m.title, m.genre " +
                     "ORDER BY total_revenue DESC, tickets_sold DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String title = rs.getString("title");
                String genre = rs.getString("genre") != null ? rs.getString("genre") : "—";
                int totalBookings = rs.getInt("total_bookings");
                int ticketsSold = rs.getInt("tickets_sold");
                BigDecimal totalRevenue = rs.getBigDecimal("total_revenue");

                reportData.add(new Object[]{
                        title,
                        genre,
                        totalBookings,
                        ticketsSold,
                        "₹" + (totalRevenue != null ? totalRevenue.setScale(2) : "0.00")
                });
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] Error generating movie booking report: " + e.getMessage());
        }
        return reportData;
    }

    /**
     * Report 3: Show-Wise Booking & Revenue Report
     * Returns: Movie Title, Theatre, Screen, Show Date, Show Time, Bookings, Tickets Sold, Total Revenue
     */
    public List<Object[]> getShowBookingReport() {
        List<Object[]> reportData = new ArrayList<>();
        String sql = "SELECT m.title, t.theatre_name, s.show_date, s.show_time, " +
                     "COUNT(DISTINCT b.booking_id) AS total_bookings, " +
                     "COUNT(bd.id) AS tickets_sold, " +
                     "COALESCE((SELECT SUM(b2.total_amount) FROM bookings b2 WHERE b2.show_id = s.show_id AND b2.status = 'CONFIRMED'), 0.00) AS total_revenue " +
                     "FROM shows s " +
                     "JOIN movies m ON s.movie_id = m.movie_id " +
                     "JOIN theatres t ON s.theatre_id = t.theatre_id " +
                     "LEFT JOIN bookings b ON s.show_id = b.show_id AND b.status = 'CONFIRMED' " +
                     "LEFT JOIN booking_details bd ON b.booking_id = bd.booking_id " +
                     "GROUP BY s.show_id, m.title, t.theatre_name, s.show_date, s.show_time " +
                     "ORDER BY s.show_date DESC, s.show_time ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String movie = rs.getString("title");
                String theatre = rs.getString("theatre_name");
                String screen = "Screen 1";
                String showDate = rs.getString("show_date") != null ? rs.getString("show_date") : "—";
                String showTime = rs.getString("show_time") != null ? rs.getString("show_time") : "—";
                int totalBookings = rs.getInt("total_bookings");
                int ticketsSold = rs.getInt("tickets_sold");
                BigDecimal totalRevenue = rs.getBigDecimal("total_revenue");

                reportData.add(new Object[]{
                        movie,
                        theatre,
                        screen,
                        showDate,
                        showTime,
                        totalBookings,
                        ticketsSold,
                        "₹" + (totalRevenue != null ? totalRevenue.setScale(2) : "0.00")
                });
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] Error generating show booking report: " + e.getMessage());
        }
        return reportData;
    }
}
