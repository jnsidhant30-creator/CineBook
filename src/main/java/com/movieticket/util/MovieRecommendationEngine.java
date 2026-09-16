package com.movieticket.util;

import com.movieticket.model.Movie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MovieRecommendationEngine.java — Rule-Based Recommendation Engine.
 * Recommends movies based on user's booking history, watchlist, and recently viewed preferred genres & languages.
 * Fallbacks to top-rated movies if no user activity history exists.
 */
public class MovieRecommendationEngine {

    public static List<Movie> getRecommendationsForUser(int userId, int limit) {
        List<Movie> recommendations = new ArrayList<>();
        if (limit <= 0) limit = 6;

        if (userId <= 0) {
            return getFallbackTopRatedMovies(limit);
        }

        // Step 1: Query User's Preferred Genre & Language from Bookings & Watchlist
        String prefSql = "SELECT m.genre, m.language, COUNT(*) AS freq " +
                         "FROM movies m " +
                         "WHERE m.movie_id IN (" +
                         "    SELECT s.movie_id FROM bookings b JOIN shows s ON b.show_id = s.show_id WHERE b.user_id = ? " +
                         "    UNION " +
                         "    SELECT w.movie_id FROM watchlist w WHERE w.user_id = ? " +
                         ") " +
                         "GROUP BY m.genre, m.language ORDER BY freq DESC LIMIT 1";

        String topGenre = null;
        String topLanguage = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(prefSql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    topGenre = rs.getString("genre");
                    topLanguage = rs.getString("language");
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieRecommendationEngine] Error determining preferences: " + e.getMessage());
        }

        // Step 2: Fetch Matching Recommended Movies
        if (topGenre != null || topLanguage != null) {
            String recSql = "SELECT * FROM movies " +
                            "WHERE (genre = ? OR language = ?) " +
                            "AND movie_id NOT IN (" +
                            "    SELECT s.movie_id FROM bookings b JOIN shows s ON b.show_id = s.show_id WHERE b.user_id = ? AND b.status = 'CONFIRMED'" +
                            ") " +
                            "ORDER BY rating DESC LIMIT ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(recSql)) {
                stmt.setString(1, topGenre != null ? topGenre : "");
                stmt.setString(2, topLanguage != null ? topLanguage : "");
                stmt.setInt(3, userId);
                stmt.setInt(4, limit);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        recommendations.add(mapMovie(rs));
                    }
                }
            } catch (SQLException e) {
                System.err.println("[MovieRecommendationEngine] Error fetching personalized recommendations: " + e.getMessage());
            }
        }

        // Step 3: Fallback if less than limit recommendations found
        if (recommendations.size() < limit) {
            List<Movie> fallback = getFallbackTopRatedMovies(limit - recommendations.size());
            for (Movie fm : fallback) {
                if (recommendations.stream().noneMatch(m -> m.getMovieId() == fm.getMovieId())) {
                    recommendations.add(fm);
                }
            }
        }

        return recommendations;
    }

    public static List<Movie> getFallbackTopRatedMovies(int limit) {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY rating DESC, title ASC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieRecommendationEngine] Error fetching fallback recommendations: " + e.getMessage());
        }
        return list;
    }

    private static Movie mapMovie(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setMovieId(rs.getInt("movie_id"));
        m.setTitle(rs.getString("title"));
        m.setGenre(rs.getString("genre"));
        m.setDuration(rs.getInt("duration"));
        m.setLanguage(rs.getString("language"));
        m.setRating(rs.getBigDecimal("rating"));
        return m;
    }
}
