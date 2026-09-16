package com.movieticket.dao;

import com.movieticket.model.Movie;
import com.movieticket.model.UserPreference;
import com.movieticket.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecommendationDAO {

    public UserPreference getUserPreference(int userId) {
        String query = "SELECT * FROM user_preferences WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserPreference pref = new UserPreference();
                    pref.setUserId(rs.getInt("user_id"));
                    pref.setPreferredGenres(rs.getString("preferred_genres"));
                    pref.setPreferredLanguages(rs.getString("preferred_languages"));
                    return pref;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean saveUserPreference(UserPreference pref) {
        String query = "INSERT INTO user_preferences (user_id, preferred_genres, preferred_languages) " +
                       "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE " +
                       "preferred_genres = VALUES(preferred_genres), " +
                       "preferred_languages = VALUES(preferred_languages)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, pref.getUserId());
            stmt.setString(2, pref.getPreferredGenres());
            stmt.setString(3, pref.getPreferredLanguages());
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getWatchedGenres(int userId) {
        String query = "SELECT m.genre, COUNT(*) as count " +
                       "FROM bookings b " +
                       "JOIN shows s ON b.show_id = s.show_id " +
                       "JOIN movies m ON s.movie_id = m.movie_id " +
                       "WHERE b.user_id = ? AND b.status = 'CONFIRMED' " +
                       "GROUP BY m.genre " +
                       "ORDER BY count DESC";
        List<String> genres = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    genres.add(rs.getString("genre"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }

    public List<String> getWatchedLanguages(int userId) {
        String query = "SELECT m.language, COUNT(*) as count " +
                       "FROM bookings b " +
                       "JOIN shows s ON b.show_id = s.show_id " +
                       "JOIN movies m ON s.movie_id = m.movie_id " +
                       "WHERE b.user_id = ? AND b.status = 'CONFIRMED' " +
                       "GROUP BY m.language " +
                       "ORDER BY count DESC";
        List<String> languages = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    languages.add(rs.getString("language"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return languages;
    }

    public List<Integer> getHighlyRatedMovieIds(int userId) {
        String query = "SELECT movie_id FROM reviews WHERE user_id = ? AND rating >= 4 AND status = 'VISIBLE'";
        List<Integer> movieIds = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movieIds.add(rs.getInt("movie_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieIds;
    }

    public List<String> getTopPreferredGenres() {
        String query = "SELECT preferred_genres FROM user_preferences";
        List<String> allGenres = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String genresStr = rs.getString("preferred_genres");
                if (genresStr != null && !genresStr.isEmpty()) {
                    String[] parts = genresStr.split(",");
                    for (String p : parts) {
                        allGenres.add(p.trim());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        for (String g : allGenres) {
            counts.put(g, counts.getOrDefault(g, 0) + 1);
        }
        
        List<java.util.Map.Entry<String, Integer>> list = new ArrayList<>(counts.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        List<String> sortedGenres = new ArrayList<>();
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            sortedGenres.add(list.get(i).getKey() + " (" + list.get(i).getValue() + ")");
        }
        return sortedGenres;
    }
}
