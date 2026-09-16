package com.movieticket.dao;

import com.movieticket.model.Movie;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * WatchlistDAO.java — Data Access Object for managing User Watchlist / Favorite movies.
 */
public class WatchlistDAO {

    public WatchlistDAO() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS watchlist (" +
                     "watchlist_id INT PRIMARY KEY AUTO_INCREMENT, " +
                     "user_id INT NOT NULL, " +
                     "movie_id INT NOT NULL, " +
                     "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                     "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                     "FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE, " +
                     "UNIQUE KEY uk_user_movie_watchlist (user_id, movie_id)" +
                     ")";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("[WatchlistDAO] Error creating table: " + e.getMessage());
        }
    }

    public boolean addToWatchlist(int userId, int movieId) {
        String sql = "INSERT IGNORE INTO watchlist (user_id, movie_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[WatchlistDAO] Error adding to watchlist: " + e.getMessage());
            return false;
        }
    }

    public boolean removeFromWatchlist(int userId, int movieId) {
        String sql = "DELETE FROM watchlist WHERE user_id = ? AND movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[WatchlistDAO] Error removing from watchlist: " + e.getMessage());
            return false;
        }
    }

    public boolean isWatchlisted(int userId, int movieId) {
        String sql = "SELECT 1 FROM watchlist WHERE user_id = ? AND movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[WatchlistDAO] Error checking watchlist status: " + e.getMessage());
            return false;
        }
    }

    public List<Movie> getUserWatchlist(int userId) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT m.* FROM movies m " +
                     "JOIN watchlist w ON m.movie_id = w.movie_id " +
                     "WHERE w.user_id = ? ORDER BY w.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Movie m = new Movie();
                    m.setMovieId(rs.getInt("movie_id"));
                    m.setTitle(rs.getString("title"));
                    m.setGenre(rs.getString("genre"));
                    m.setDuration(rs.getInt("duration"));
                    m.setLanguage(rs.getString("language"));
                    m.setRating(rs.getBigDecimal("rating"));
                    movies.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("[WatchlistDAO] Error fetching user watchlist: " + e.getMessage());
        }
        return movies;
    }

    public int getWatchlistCount(int userId) {
        String sql = "SELECT COUNT(*) FROM watchlist WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[WatchlistDAO] Error getting count: " + e.getMessage());
        }
        return 0;
    }
}
