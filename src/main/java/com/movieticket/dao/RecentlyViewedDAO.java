package com.movieticket.dao;

import com.movieticket.model.Movie;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RecentlyViewedDAO.java — Data Access Object for tracking User's recently viewed movies.
 */
public class RecentlyViewedDAO {

    public RecentlyViewedDAO() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS recently_viewed (" +
                     "viewed_id INT PRIMARY KEY AUTO_INCREMENT, " +
                     "user_id INT NOT NULL, " +
                     "movie_id INT NOT NULL, " +
                     "viewed_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                     "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                     "FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE, " +
                     "UNIQUE KEY uk_user_movie_viewed (user_id, movie_id)" +
                     ")";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("[RecentlyViewedDAO] Error creating table: " + e.getMessage());
        }
    }

    public void recordView(int userId, int movieId) {
        if (userId <= 0 || movieId <= 0) return;
        String sql = "INSERT INTO recently_viewed (user_id, movie_id, viewed_at) VALUES (?, ?, CURRENT_TIMESTAMP) " +
                     "ON DUPLICATE KEY UPDATE viewed_at = CURRENT_TIMESTAMP";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[RecentlyViewedDAO] Error recording movie view: " + e.getMessage());
        }
    }

    public List<Movie> getRecentlyViewedMovies(int userId, int limit) {
        List<Movie> movies = new ArrayList<>();
        if (userId <= 0) return movies;
        if (limit <= 0) limit = 6;

        String sql = "SELECT m.* FROM movies m " +
                     "JOIN recently_viewed rv ON m.movie_id = rv.movie_id " +
                     "WHERE rv.user_id = ? ORDER BY rv.viewed_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
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
            System.err.println("[RecentlyViewedDAO] Error fetching recently viewed: " + e.getMessage());
        }
        return movies;
    }
}
