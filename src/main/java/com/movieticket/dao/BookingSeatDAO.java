package com.movieticket.dao;

import com.movieticket.model.BookingSeat;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingSeatDAO {

    public boolean createBookingSeat(BookingSeat bookingSeat) {
        String sql = "INSERT INTO booking_details (booking_id, seat_id, price) VALUES (?, ?, 0.00)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingSeat.getBookingId());
            stmt.setInt(2, bookingSeat.getSeatId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<BookingSeat> getDetailsByBookingId(int bookingId) {
        List<BookingSeat> seats = new ArrayList<>();
        String sql = "SELECT * FROM booking_details WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapResultSetToBookingSeat(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    public List<BookingSeat> getDetailsByShowId(int showId) {
        List<BookingSeat> seats = new ArrayList<>();
        String sql = "SELECT bs.booking_id, bs.seat_id FROM booking_details bs " +
                     "JOIN bookings b ON bs.booking_id = b.booking_id " +
                     "WHERE b.show_id = ? AND b.status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapResultSetToBookingSeat(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    private BookingSeat mapResultSetToBookingSeat(ResultSet rs) throws SQLException {
        return new BookingSeat(
                rs.getInt("booking_id"),
                rs.getInt("seat_id")
        );
    }
}
