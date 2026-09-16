package com.movieticket.dao;

import com.movieticket.model.UpcomingMovie;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UpcomingMovieDAO {

    public boolean addUpcomingMovie(UpcomingMovie movie) {
        String sql = "INSERT INTO upcoming_movies (title, release_date, imdb_id, poster_url, genre, description, status, source) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, movie.getTitle());
            stmt.setDate(2, movie.getReleaseDate());
            stmt.setString(3, movie.getImdbId());
            stmt.setString(4, movie.getPosterUrl());
            stmt.setString(5, movie.getGenre());
            stmt.setString(6, movie.getDescription());
            stmt.setString(7, movie.getStatus() != null ? movie.getStatus() : "NEW");
            stmt.setString(8, movie.getSource() != null ? movie.getSource() : "SIMULATED");

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding upcoming movie: " + e.getMessage());
            return false;
        }
    }

    public List<UpcomingMovie> getAllUpcomingMovies() {
        return getUpcomingMoviesBySql("SELECT * FROM upcoming_movies ORDER BY release_date ASC", null);
    }

    public List<UpcomingMovie> getUpcomingMoviesByStatus(String status) {
        String sql = "SELECT * FROM upcoming_movies WHERE status = ? ORDER BY release_date ASC";
        return getUpcomingMoviesBySql(sql, stmt -> {
            try {
                stmt.setString(1, status);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    public UpcomingMovie getUpcomingMovieByImdbId(String imdbId) {
        String sql = "SELECT * FROM upcoming_movies WHERE imdb_id = ?";
        List<UpcomingMovie> list = getUpcomingMoviesBySql(sql, stmt -> {
            try {
                stmt.setString(1, imdbId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return list.isEmpty() ? null : list.get(0);
    }
    
    public UpcomingMovie getUpcomingMovieByTitleAndDate(String title, Date releaseDate) {
        String sql = "SELECT * FROM upcoming_movies WHERE title = ? AND release_date = ?";
        List<UpcomingMovie> list = getUpcomingMoviesBySql(sql, stmt -> {
            try {
                stmt.setString(1, title);
                stmt.setDate(2, releaseDate);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE upcoming_movies SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, status);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating status: " + e.getMessage());
            return false;
        }
    }

    private List<UpcomingMovie> getUpcomingMoviesBySql(String sql, StatementParameterSetter setter) {
        List<UpcomingMovie> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (setter != null) {
                setter.setParameters(stmt);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUpcomingMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching upcoming movies: " + e.getMessage());
        }
        return list;
    }

    private UpcomingMovie mapResultSetToUpcomingMovie(ResultSet rs) throws SQLException {
        return new UpcomingMovie(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getDate("release_date"),
                rs.getString("imdb_id"),
                rs.getString("poster_url"),
                rs.getString("genre"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getTimestamp("discovered_at"),
                rs.getTimestamp("last_checked"),
                rs.getString("source")
        );
    }

    @FunctionalInterface
    private interface StatementParameterSetter {
        void setParameters(PreparedStatement stmt);
    }
}
