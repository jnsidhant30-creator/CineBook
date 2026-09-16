package com.movieticket.dao;

import com.movieticket.model.Screen;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Screen entity.
 * Manages CRUD operations for cinema screens within theatres.
 */
public class ScreenDAO {

    public int addScreen(Screen screen) {
        String sql = "INSERT INTO screens (theatre_id, screen_name, screen_type, capacity, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, screen.getTheatreId());
            stmt.setString(2, screen.getScreenName());
            stmt.setString(3, screen.getScreenType());
            stmt.setInt(4, screen.getCapacity());
            stmt.setString(5, screen.getStatus());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ScreenDAO] Error adding screen: " + e.getMessage());
        }
        return -1;
    }

    public Optional<Screen> getScreenById(int screenId) {
        String sql = "SELECT * FROM screens WHERE screen_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, screenId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ScreenDAO] Error fetching screen by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Screen> getAllScreens() {
        List<Screen> list = new ArrayList<>();
        String sql = "SELECT * FROM screens ORDER BY theatre_id ASC, screen_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[ScreenDAO] Error loading all screens: " + e.getMessage());
        }
        return list;
    }

    public List<Screen> getScreensByTheatre(int theatreId) {
        List<Screen> list = new ArrayList<>();
        String sql = "SELECT * FROM screens WHERE theatre_id = ? ORDER BY screen_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theatreId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ScreenDAO] Error fetching screens by theatre: " + e.getMessage());
        }
        return list;
    }

    public List<Screen> getActiveScreensByTheatre(int theatreId) {
        List<Screen> list = new ArrayList<>();
        String sql = "SELECT * FROM screens WHERE theatre_id = ? AND status = 'ACTIVE' ORDER BY screen_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theatreId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ScreenDAO] Error fetching active screens: " + e.getMessage());
        }
        return list;
    }

    public boolean updateScreen(Screen screen) {
        String sql = "UPDATE screens SET screen_name = ?, screen_type = ?, capacity = ?, status = ? WHERE screen_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, screen.getScreenName());
            stmt.setString(2, screen.getScreenType());
            stmt.setInt(3, screen.getCapacity());
            stmt.setString(4, screen.getStatus());
            stmt.setInt(5, screen.getScreenId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ScreenDAO] Error updating screen: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteScreen(int screenId) {
        String sql = "DELETE FROM screens WHERE screen_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, screenId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ScreenDAO] Error deleting screen: " + e.getMessage());
            return false;
        }
    }

    public boolean hasAssociatedShows(int screenId) {
        String sql = "SELECT COUNT(*) FROM shows WHERE screen_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, screenId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[ScreenDAO] Error checking associated shows: " + e.getMessage());
        }
        return false;
    }

    public int countScreens() {
        String sql = "SELECT COUNT(*) FROM screens";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ScreenDAO] Error counting screens: " + e.getMessage());
        }
        return 0;
    }

    private Screen mapRow(ResultSet rs) throws SQLException {
        return new Screen(
                rs.getInt("screen_id"),
                rs.getInt("theatre_id"),
                rs.getString("screen_name"),
                rs.getString("screen_type"),
                rs.getInt("capacity"),
                rs.getString("status")
        );
    }
}
