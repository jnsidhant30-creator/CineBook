package com.movieticket.dao;

import com.movieticket.model.Seat;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeatDAO {

    public int addSeat(Seat seat) {
        String sql = "INSERT INTO seats (theatre_id, screen_id, seat_number, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, seat.getTheatreId());
            stmt.setInt(2, seat.getScreenId() > 0 ? seat.getScreenId() : getDefaultScreenId(seat.getTheatreId()));
            stmt.setString(3, seat.getSeatNumber().trim());
            stmt.setString(4, seat.getStatus() != null ? seat.getStatus().trim() : "AVAILABLE");

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

    /** Returns the first screen_id for a given theatre, or 1 as fallback. */
    private int getDefaultScreenId(int theatreId) {
        String sql = "SELECT screen_id FROM screens WHERE theatre_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theatreId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // fallback
    }

    public Optional<Seat> getSeatById(int seatId) {
        String sql = "SELECT * FROM seats WHERE seat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, seatId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSeat(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Seat> getAllSeats() {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM seats ORDER BY theatre_id ASC, seat_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                seats.add(mapResultSetToSeat(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    public List<Seat> getSeatsByTheatre(int theatreId) {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE theatre_id = ? ORDER BY seat_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theatreId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapResultSetToSeat(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If theatre has 0 configured seats in DB, auto-generate seats matching theatre capacity
        if (seats.isEmpty() && theatreId > 0) {
            autoGenerateSeatsForTheatre(theatreId);
            // Re-fetch after generation
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, theatreId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        seats.add(mapResultSetToSeat(rs));
                    }
                }
            } catch (SQLException ignored) {}
        }

        return seats;
    }

    private void autoGenerateSeatsForTheatre(int theatreId) {
        int capacity = 20;
        String capSql = "SELECT total_seats FROM theatres WHERE theatre_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(capSql)) {
            stmt.setInt(1, theatreId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    capacity = Math.max(10, rs.getInt("total_seats"));
                }
            }
        } catch (SQLException ignored) {}

        int seatsPerRow = 10;
        int totalRows = (int) Math.ceil((double) capacity / seatsPerRow);
        for (int r = 0; r < totalRows; r++) {
            char rowChar = (char) ('A' + r);
            for (int s = 1; s <= seatsPerRow; s++) {
                int seatIndex = (r * seatsPerRow) + s;
                if (seatIndex > capacity) break;
                String seatNum = "" + rowChar + s;
                addSeat(new Seat(0, theatreId, seatNum, "AVAILABLE"));
            }
        }
    }

    public boolean isSeatExists(int theatreId, String seatNumber) {
        String sql = "SELECT COUNT(*) FROM seats WHERE theatre_id = ? AND UPPER(seat_number) = UPPER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theatreId);
            stmt.setString(2, seatNumber.trim());
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

    public boolean isSeatExistsExcludingId(int theatreId, String seatNumber, int excludeSeatId) {
        String sql = "SELECT COUNT(*) FROM seats WHERE theatre_id = ? AND UPPER(seat_number) = UPPER(?) AND seat_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theatreId);
            stmt.setString(2, seatNumber.trim());
            stmt.setInt(3, excludeSeatId);
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

    public boolean hasAssociatedBookings(int seatId) {
        String sql = "SELECT COUNT(*) FROM booking_details WHERE seat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, seatId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            // If table does not exist or error, assume safe
            return false;
        }
        return false;
    }

    public List<Seat> searchSeats(int theatreId, String query) {
        List<Seat> seats = new ArrayList<>();
        String sql;
        if (theatreId > 0) {
            sql = "SELECT * FROM seats WHERE theatre_id = ? AND (seat_number LIKE ? OR status LIKE ?) ORDER BY seat_number ASC";
        } else {
            sql = "SELECT * FROM seats WHERE seat_number LIKE ? OR status LIKE ? ORDER BY theatre_id ASC, seat_number ASC";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + query.trim() + "%";
            if (theatreId > 0) {
                stmt.setInt(1, theatreId);
                stmt.setString(2, pattern);
                stmt.setString(3, pattern);
            } else {
                stmt.setString(1, pattern);
                stmt.setString(2, pattern);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapResultSetToSeat(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    public boolean updateSeat(Seat seat) {
        String sql = "UPDATE seats SET theatre_id = ?, seat_number = ?, status = ? WHERE seat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, seat.getTheatreId());
            stmt.setString(2, seat.getSeatNumber().trim());
            stmt.setString(3, seat.getStatus() != null ? seat.getStatus().trim() : "AVAILABLE");
            stmt.setInt(4, seat.getSeatId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSeat(int seatId) {
        String sql = "DELETE FROM seats WHERE seat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, seatId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Seat mapResultSetToSeat(ResultSet rs) throws SQLException {
        Seat s = new Seat(
                rs.getInt("seat_id"),
                rs.getInt("theatre_id"),
                rs.getString("seat_number"),
                rs.getString("status")
        );
        // Phase 5: read screen_id safely
        try {
            int screenId = rs.getInt("screen_id");
            if (!rs.wasNull()) s.setScreenId(screenId);
        } catch (SQLException ignore) {}
        return s;
    }

    public int countSeats() throws SQLException {
        String sql = "SELECT COUNT(*) FROM seats";
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
