package com.movieticket.dao;

import com.movieticket.model.Review;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    public boolean addReview(Review review) {
        String sql = "INSERT INTO reviews (user_id, booking_id, movie_id, theatre_id, rating, review_text) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, review.getUserId());
            stmt.setInt(2, review.getBookingId());

            if (review.getMovieId() != null) {
                stmt.setInt(3, review.getMovieId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            if (review.getTheatreId() != null) {
                stmt.setInt(4, review.getTheatreId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setInt(5, review.getRating());

            if (review.getReviewText() != null && !review.getReviewText().trim().isEmpty()) {
                stmt.setString(6, review.getReviewText().trim());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database Error (addReview): " + e.getMessage());
            return false;
        }
    }

    public boolean hasUserReviewedBooking(int userId, int bookingId, boolean isMovie) {
        String column = isMovie ? "movie_id" : "theatre_id";
        String sql = "SELECT 1 FROM reviews WHERE user_id = ? AND booking_id = ? AND " + column + " IS NOT NULL";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateReviewStatus(int reviewId, String status) {
        String sql = "UPDATE reviews SET status = ? WHERE review_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, reviewId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Review> getVisibleReviewsForMovie(int movieId, int offset, int limit) {
        return getReviewsByTarget("movie_id", movieId, true, offset, limit);
    }
    
    public List<Review> getVisibleReviewsForTheatre(int theatreId, int offset, int limit) {
        return getReviewsByTarget("theatre_id", theatreId, true, offset, limit);
    }

    public List<Review> getAllReviewsForAdmin() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.username, m.title AS movieTitle, t.theatre_name AS theatreName " +
                     "FROM reviews r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "LEFT JOIN movies m ON r.movie_id = m.movie_id " +
                     "LEFT JOIN theatres t ON r.theatre_id = t.theatre_id " +
                     "ORDER BY r.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Review rev = mapResultSetToReview(rs);
                rev.setUsername(rs.getString("username"));
                rev.setMovieTitle(rs.getString("movieTitle"));
                rev.setTheatreName(rs.getString("theatreName"));
                reviews.add(rev);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    private List<Review> getReviewsByTarget(String column, int targetId, boolean onlyVisible, int offset, int limit) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.username FROM reviews r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r." + column + " = ? ";
        if (onlyVisible) {
            sql += "AND r.status = 'VISIBLE' ";
        }
        sql += "ORDER BY r.created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, targetId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Review rev = mapResultSetToReview(rs);
                    rev.setUsername(rs.getString("username"));
                    reviews.add(rev);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public double getAverageRating(String column, int targetId) {
        String sql = "SELECT AVG(rating) FROM reviews WHERE " + column + " = ? AND status = 'VISIBLE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, targetId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Math.round(rs.getDouble(1) * 10.0) / 10.0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    public int getRatingCount(String column, int targetId) {
        String sql = "SELECT COUNT(rating) FROM reviews WHERE " + column + " = ? AND status = 'VISIBLE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, targetId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getInt("review_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setBookingId(rs.getInt("booking_id"));

        int movieId = rs.getInt("movie_id");
        if (!rs.wasNull()) r.setMovieId(movieId);

        int theatreId = rs.getInt("theatre_id");
        if (!rs.wasNull()) r.setTheatreId(theatreId);

        r.setRating(rs.getInt("rating"));
        r.setReviewText(rs.getString("review_text"));
        r.setStatus(rs.getString("status"));
        
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDateTime());
        
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) r.setUpdatedAt(updated.toLocalDateTime());

        return r;
    }
}
