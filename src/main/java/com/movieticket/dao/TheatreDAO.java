package com.movieticket.dao;

import com.movieticket.model.Theatre;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Theatre entity (Phase 5 — Multi-Theatre update).
 *
 * Implements CRUD operations, city-based filtering, search, live counting,
 * and foreign-key dependency verification with MySQL.
 */
public class TheatreDAO {

    public int addTheatre(Theatre theatre) {
        String sql = "INSERT INTO theatres (theatre_name, location, total_seats, city, contact_number, num_screens, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, theatre.getTheatreName());
            stmt.setString(2, theatre.getLocation());
            stmt.setInt(3, theatre.getTotalSeats());
            stmt.setString(4, theatre.getCity() != null ? theatre.getCity() : "Unknown City");
            stmt.setString(5, theatre.getContactNumber());
            stmt.setInt(6, theatre.getNumScreens() > 0 ? theatre.getNumScreens() : 1);
            stmt.setString(7, theatre.getStatus() != null ? theatre.getStatus() : "ACTIVE");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[TheatreDAO] Error adding theatre: " + e.getMessage());
        }
        return -1;
    }

    public Optional<Theatre> getTheatreById(int theatreId) {
        String sql = "SELECT * FROM theatres WHERE theatre_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theatreId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToTheatre(rs));
            }
        } catch (SQLException e) {
            System.err.println("[TheatreDAO] Error finding theatre by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Theatre> getAllTheatres() {
        List<Theatre> theatres = new ArrayList<>();
        String sql = "SELECT * FROM theatres ORDER BY city ASC, theatre_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) theatres.add(mapResultSetToTheatre(rs));
        } catch (SQLException e) {
            System.err.println("[TheatreDAO] Error loading all theatres: " + e.getMessage());
        }
        return theatres;
    }

    public List<Theatre> getActiveTheatres() {
        List<Theatre> theatres = new ArrayList<>();
        String sql = "SELECT * FROM theatres WHERE status = 'ACTIVE' ORDER BY city ASC, theatre_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) theatres.add(mapResultSetToTheatre(rs));
        } catch (SQLException e) {
            System.err.println("[TheatreDAO] Error loading active theatres: " + e.getMessage());
        }
        return theatres;
    }

    public List<Theatre> getTheatresByCity(String city) {
        if (city == null || city.isBlank() || city.equals("All Cities")) {
            return getActiveTheatres();
        }
        List<Theatre> theatres = new ArrayList<>();
        String sql = "SELECT * FROM theatres WHERE status = 'ACTIVE' AND city = ? ORDER BY theatre_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, city);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) theatres.add(mapResultSetToTheatre(rs));
            }
        } catch (SQLException e) {
            System.err.println("[TheatreDAO] Error fetching theatres by city: " + e.getMessage());
        }
        return theatres;
    }

    public List<String> getDistinctCities() {
        List<String> cities = new ArrayList<>();
        String sql = "SELECT DISTINCT city FROM theatres WHERE status = 'ACTIVE' AND city IS NOT NULL ORDER BY city ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String c = rs.getString("city");
                if (c != null && !c.isBlank()) cities.add(c);
            }
        } catch (SQLException e) {
            System.err.println("[TheatreDAO] Error fetching distinct cities: " + e.getMessage());
        }
        return cities;
    }

    public List<Theatre> searchTheatres(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAllTheatres();
        List<Theatre> theatres = new ArrayList<>();
        String sql = "SELECT * FROM theatres WHERE theatre_name LIKE ? OR location LIKE ? OR city LIKE ? ORDER BY theatre_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String term = "%" + keyword.trim() + "%";
            stmt.setString(1, term);
            stmt.setString(2, term);
            stmt.setString(3, term);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) theatres.add(mapResultSetToTheatre(rs));
            }
        } catch (SQLException e) {
            System.err.println("[TheatreDAO] Error searching theatres: " + e.getMessage());
        }
        return theatres;
    }

    public boolean updateTheatre(Theatre theatre) {
        String sql = "UPDATE theatres SET theatre_name = ?, location = ?, total_seats = ?, city = ?, contact_number = ?, num_screens = ?, status = ? WHERE theatre_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, theatre.getTheatreName());
            stmt.setString(2, theatre.getLocation());
            stmt.setInt(3, theatre.getTotalSeats());
            stmt.setString(4, theatre.getCity());
            stmt.setString(5, theatre.getContactNumber());
            stmt.setInt(6, theatre.getNumScreens());
            stmt.setString(7, theatre.getStatus());
            stmt.setInt(8, theatre.getTheatreId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TheatreDAO] Error updating theatre: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteTheatre(int theatreId) {
        String sql = "DELETE FROM theatres WHERE theatre_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theatreId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TheatreDAO] Error deleting theatre: " + e.getMessage());
            return false;
        }
    }

    public boolean hasAssociatedRecords(int theatreId) {
        String checkShowsSql = "SELECT COUNT(*) FROM shows WHERE theatre_id = ?";
        String checkSeatsSql = "SELECT COUNT(*) FROM seats WHERE theatre_id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(checkShowsSql)) {
                stmt.setInt(1, theatreId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return true;
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(checkSeatsSql)) {
                stmt.setInt(1, theatreId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[TheatreDAO] Error checking associated theatre records: " + e.getMessage());
        }
        return false;
    }

    private Theatre mapResultSetToTheatre(ResultSet rs) throws SQLException {
        Theatre t = new Theatre(
                rs.getInt("theatre_id"),
                rs.getString("theatre_name"),
                rs.getString("location"),
                rs.getInt("total_seats")
        );
        // Safely read Phase 5 columns (may not exist on first call after migration)
        try { t.setCity(rs.getString("city")); } catch (SQLException ignore) {}
        try { t.setContactNumber(rs.getString("contact_number")); } catch (SQLException ignore) {}
        try { t.setNumScreens(rs.getInt("num_screens")); } catch (SQLException ignore) {}
        try { t.setStatus(rs.getString("status")); } catch (SQLException ignore) { t.setStatus("ACTIVE"); }
        return t;
    }

    public int countTheatres() throws SQLException {
        String sql = "SELECT COUNT(*) FROM theatres";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
