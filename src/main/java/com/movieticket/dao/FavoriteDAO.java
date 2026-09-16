package com.movieticket.dao;

import com.movieticket.model.Movie;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FavoriteDAO.java — Data Access Object for managing User Favorite movies.
 */
public class FavoriteDAO {

    public FavoriteDAO() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS favorites (" +
                     "favorite_id INT PRIMARY KEY AUTO_INCREMENT, " +
                     "user_id INT NOT NULL, " +
                     "movie_id INT NOT NULL, " +
                     "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                     "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                     "FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE, " +
                     "UNIQUE KEY uk_user_movie_favorite (user_id, movie_id)" +
                     ")";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("[FavoriteDAO] Error creating table: " + e.getMessage());
        }
    }

    public boolean addToFavorites(int userId, int movieId) {
        String sql = "INSERT IGNORE INTO favorites (user_id, movie_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FavoriteDAO] Error adding to favorites: " + e.getMessage());
            return false;
        }
    }

    public boolean removeFromFavorites(int userId, int movieId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FavoriteDAO] Error removing from favorites: " + e.getMessage());
            return false;
        }
    }

    public boolean isFavorited(int userId, int movieId) {
        String sql = "SELECT 1 FROM favorites WHERE user_id = ? AND movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[FavoriteDAO] Error checking favorite status: " + e.getMessage());
            return false;
        }
    }

    public List<Movie> getUserFavorites(int userId) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT m.* FROM movies m " +
                     "JOIN favorites f ON m.movie_id = f.movie_id " +
                     "WHERE f.user_id = ? ORDER BY f.created_at DESC";
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
            System.err.println("[FavoriteDAO] Error fetching user favorites: " + e.getMessage());
        }
        return movies;
    }

    public int getFavoriteCount(int userId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[FavoriteDAO] Error getting count: " + e.getMessage());
        }
        return 0;
    }
}
