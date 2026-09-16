package com.movieticket.dao;

import com.movieticket.model.Booking;
import com.movieticket.model.Seat;
import com.movieticket.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDAO {

    public int createBooking(Booking booking) {
        String sql = "INSERT INTO bookings (user_id, show_id, booking_date, total_amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, booking.getUserId());
            stmt.setInt(2, booking.getShowId());
            stmt.setTimestamp(3, Timestamp.valueOf(booking.getBookingDate()));
            stmt.setBigDecimal(4, booking.getTotalAmount());
            stmt.setString(5, booking.getStatus());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Optional<Booking> getBookingById(int bookingId) {
        String sql = "SELECT * FROM bookings WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Booking> getBookingsByUser(int userId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE user_id = ? ORDER BY booking_date DESC, booking_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapResultSetToBooking(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Error fetching user bookings: " + e.getMessage());
        }
        return bookings;
    }

    public Optional<Booking> getBookingByIdAndUser(int bookingId, int userId) {
        String sql = "SELECT * FROM bookings WHERE booking_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBooking(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Error fetching booking by ID and user: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<String> getSeatNumbersForBooking(int bookingId) {
        List<String> seatNumbers = new ArrayList<>();
        String sql = "SELECT s.seat_number FROM booking_details bd " +
                     "JOIN seats s ON bd.seat_id = s.seat_id " +
                     "WHERE bd.booking_id = ? ORDER BY s.seat_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seatNumbers.add(rs.getString("seat_number"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Error fetching seat numbers for booking: " + e.getMessage());
        }
        return seatNumbers;
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public boolean updateBookingStatus(int bookingId, String status) {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, bookingId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getInt("booking_id"),
                rs.getInt("user_id"),
                rs.getInt("show_id"),
                rs.getTimestamp("booking_date").toLocalDateTime(),
                rs.getBigDecimal("total_amount"),
                rs.getString("status")
        );
    }

    public List<Integer> getBookedSeatIdsForShow(int showId) {
        List<Integer> bookedSeats = new ArrayList<>();
        String sql = "SELECT bd.seat_id FROM booking_details bd " +
                     "JOIN bookings b ON bd.booking_id = b.booking_id " +
                     "WHERE b.show_id = ? AND b.status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookedSeats.add(rs.getInt("seat_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Error fetching booked seats for show: " + e.getMessage());
        }
        return bookedSeats;
    }

    public boolean isSeatBookedForShow(int showId, int seatId) {
        String sql = "SELECT COUNT(*) FROM booking_details bd " +
                     "JOIN bookings b ON bd.booking_id = b.booking_id " +
                     "WHERE b.show_id = ? AND bd.seat_id = ? AND b.status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showId);
            stmt.setInt(2, seatId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Error checking seat availability: " + e.getMessage());
        }
        return false;
    }

    /**
     * Atomically creates a booking and reserves selected seats within a database transaction.
     * Prevents double-booking via transaction isolation and rollback.
     *
     * @return Generated booking_id if successful, -2 if seats already booked, -1 on failure.
     */
    /**
     * Atomically creates a booking and reserves selected seats within a database transaction.
     * Calculates category price per seat (REGULAR, PREMIUM, VIP) and prevents double booking.
     *
     * @return Generated booking_id if successful, -2 if seats already booked, -1 on failure.
     */
    public int createBookingWithSeatPrices(Booking booking, java.util.Map<Integer, BigDecimal> seatPrices) {
        if (seatPrices == null || seatPrices.isEmpty()) {
            return -1;
        }

        String lockSeatSql = "SELECT seat_id FROM seats WHERE seat_id = ? FOR UPDATE";

        String checkSeatSql = "SELECT COUNT(*) FROM booking_details bd " +
                              "JOIN bookings b ON bd.booking_id = b.booking_id " +
                              "WHERE b.show_id = ? AND bd.seat_id = ? AND b.status = 'CONFIRMED'";

        String insertBookingSql = "INSERT INTO bookings (user_id, show_id, booking_date, total_amount, status) " +
                                  "VALUES (?, ?, ?, ?, ?)";

        String insertDetailSql = "INSERT INTO booking_details (booking_id, seat_id, price) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Double Booking Check inside transaction with Row Lock
            try (PreparedStatement lockStmt = conn.prepareStatement(lockSeatSql);
                 PreparedStatement checkStmt = conn.prepareStatement(checkSeatSql)) {
                for (Integer seatId : seatPrices.keySet()) {
                    lockStmt.setInt(1, seatId);
                    try (ResultSet lockRs = lockStmt.executeQuery()) {
                        // Acquired lock for this seat row
                        checkStmt.setInt(1, booking.getShowId());
                        checkStmt.setInt(2, seatId);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                // Seat is already booked!
                                conn.rollback();
                                return -2; // Concurrency conflict
                            }
                        }
                    }
                }
            }

            // 2. Insert into bookings
            int generatedBookingId = -1;
            try (PreparedStatement bStmt = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                bStmt.setInt(1, booking.getUserId());
                bStmt.setInt(2, booking.getShowId());
                Timestamp ts = booking.getBookingDate() != null ?
                        Timestamp.valueOf(booking.getBookingDate()) : new Timestamp(System.currentTimeMillis());
                bStmt.setTimestamp(3, ts);
                bStmt.setBigDecimal(4, booking.getTotalAmount());
                bStmt.setString(5, booking.getStatus() != null ? booking.getStatus() : "CONFIRMED");

                int affected = bStmt.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = bStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            generatedBookingId = rs.getInt(1);
                        }
                    }
                }
            }

            if (generatedBookingId <= 0) {
                conn.rollback();
                return -1;
            }

            // 3. Insert into booking_details for each seat
            try (PreparedStatement dStmt = conn.prepareStatement(insertDetailSql)) {
                for (java.util.Map.Entry<Integer, BigDecimal> entry : seatPrices.entrySet()) {
                    dStmt.setInt(1, generatedBookingId);
                    dStmt.setInt(2, entry.getKey());
                    dStmt.setBigDecimal(3, entry.getValue());
                    dStmt.addBatch();
                }
                dStmt.executeBatch();
            }

            // 4. Commit Transaction
            conn.commit();
            return generatedBookingId;

        } catch (SQLException e) {
            System.err.println("[BookingDAO] Transaction failed, rolling back: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public int createBookingWithSeats(Booking booking, List<Integer> seatIds, BigDecimal seatPrice) {
        if (seatIds == null || seatIds.isEmpty()) {
            return -1;
        }

        String lockSeatSql = "SELECT seat_id FROM seats WHERE seat_id = ? FOR UPDATE";

        String checkSeatSql = "SELECT COUNT(*) FROM booking_details bd " +
                              "JOIN bookings b ON bd.booking_id = b.booking_id " +
                              "WHERE b.show_id = ? AND bd.seat_id = ? AND b.status = 'CONFIRMED'";

        String insertBookingSql = "INSERT INTO bookings (user_id, show_id, booking_date, total_amount, status) " +
                                  "VALUES (?, ?, ?, ?, ?)";

        String insertDetailSql = "INSERT INTO booking_details (booking_id, seat_id, price) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Double Booking Check inside transaction with Row Lock
            try (PreparedStatement lockStmt = conn.prepareStatement(lockSeatSql);
                 PreparedStatement checkStmt = conn.prepareStatement(checkSeatSql)) {
                for (int seatId : seatIds) {
                    lockStmt.setInt(1, seatId);
                    try (ResultSet lockRs = lockStmt.executeQuery()) {
                        checkStmt.setInt(1, booking.getShowId());
                        checkStmt.setInt(2, seatId);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                // Seat is already booked!
                                conn.rollback();
                                return -2; // Concurrency conflict
                            }
                        }
                    }
                }
            }

            // 2. Insert into bookings
            int generatedBookingId = -1;
            try (PreparedStatement bStmt = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                bStmt.setInt(1, booking.getUserId());
                bStmt.setInt(2, booking.getShowId());
                Timestamp ts = booking.getBookingDate() != null ?
                        Timestamp.valueOf(booking.getBookingDate()) : new Timestamp(System.currentTimeMillis());
                bStmt.setTimestamp(3, ts);
                bStmt.setBigDecimal(4, booking.getTotalAmount());
                bStmt.setString(5, booking.getStatus() != null ? booking.getStatus() : "CONFIRMED");

                int affected = bStmt.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = bStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            generatedBookingId = rs.getInt(1);
                        }
                    }
                }
            }

            if (generatedBookingId <= 0) {
                conn.rollback();
                return -1;
            }

            // 3. Insert into booking_details for each seat
            try (PreparedStatement dStmt = conn.prepareStatement(insertDetailSql)) {
                for (int seatId : seatIds) {
                    dStmt.setInt(1, generatedBookingId);
                    dStmt.setInt(2, seatId);
                    dStmt.setBigDecimal(3, seatPrice);
                    dStmt.addBatch();
                }
                dStmt.executeBatch();
            }

            // 4. Commit Transaction
            conn.commit();
            return generatedBookingId;

        } catch (SQLException e) {
            System.err.println("[BookingDAO] Transaction failed, rolling back: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public int countBookings() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
