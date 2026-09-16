package com.movieticket.dao;

import com.movieticket.model.Show;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShowDAO {

    public int addShow(Show show) {
        String sql = "INSERT INTO shows (movie_id, theatre_id, screen_id, show_date, show_time, ticket_price, premium_price, vip_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, show.getMovieId());
            stmt.setInt(2, show.getTheatreId());
            stmt.setInt(3, show.getScreenId());
            stmt.setDate(4, Date.valueOf(show.getShowDate()));
            stmt.setTime(5, Time.valueOf(show.getShowTime()));
            stmt.setBigDecimal(6, show.getTicketPrice());
            stmt.setObject(7, show.getPremiumPrice(), java.sql.Types.DECIMAL);
            stmt.setObject(8, show.getVipPrice(), java.sql.Types.DECIMAL);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("=== DATABASE ERROR DIAGNOSTICS ===");
            System.err.println("Exception Class: " + e.getClass().getName());
            System.err.println("Full Message: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("MySQL Error Code: " + e.getErrorCode());
            System.err.println("SQL Query: " + sql);
            System.err.println("movie_id: " + show.getMovieId());
            System.err.println("theatre_id: " + show.getTheatreId());
            System.err.println("screen_id: " + show.getScreenId());
            System.err.println("show_date: " + show.getShowDate());
            System.err.println("start_time: " + show.getShowTime());
            System.err.println("end_time: " + show.getEndTime());
            System.err.println("price: " + show.getTicketPrice());
            System.err.println("=== FULL STACK TRACE ===");
            e.printStackTrace();
        }
        return -1;
    }

    public Optional<Show> getShowById(int showId) {
        String sql = "SELECT * FROM shows WHERE show_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToShow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Show> getAllShows() {
        List<Show> shows = new ArrayList<>();
        String sql = "SELECT * FROM shows";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) shows.add(mapResultSetToShow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shows;
    }

    public List<Show> getShowsByMovie(int movieId) {
        List<Show> shows = new ArrayList<>();
        String sql = "SELECT * FROM shows WHERE movie_id = ? ORDER BY show_date ASC, show_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) shows.add(mapResultSetToShow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shows;
    }

    public List<Show> getShowsByMovieAndTheatre(int movieId, int theatreId) {
        List<Show> shows = new ArrayList<>();
        String sql = "SELECT * FROM shows WHERE movie_id = ? AND theatre_id = ? ORDER BY show_date ASC, show_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            stmt.setInt(2, theatreId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) shows.add(mapResultSetToShow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shows;
    }

    public List<Show> getShowsByTheatre(int theatreId) {
        List<Show> shows = new ArrayList<>();
        String sql = "SELECT * FROM shows WHERE theatre_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theatreId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) shows.add(mapResultSetToShow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shows;
    }

    public List<Show> getShowsByScreen(int screenId) {
        List<Show> shows = new ArrayList<>();
        String sql = "SELECT * FROM shows WHERE screen_id = ? ORDER BY show_date ASC, show_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, screenId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) shows.add(mapResultSetToShow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shows;
    }

    public List<Show> getShowsByTheatreAndDate(int theatreId, LocalDate showDate) {
        List<Show> shows = new ArrayList<>();
        String sql = "SELECT * FROM shows WHERE theatre_id = ? AND show_date = ? ORDER BY show_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theatreId);
            stmt.setDate(2, Date.valueOf(showDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) shows.add(mapResultSetToShow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shows;
    }

    public List<Show> searchShows(String query) {
        List<Show> shows = new ArrayList<>();
        String sql = "SELECT s.* FROM shows s " +
                     "JOIN movies m ON s.movie_id = m.movie_id " +
                     "JOIN theatres t ON s.theatre_id = t.theatre_id " +
                     "WHERE m.title LIKE ? OR t.theatre_name LIKE ? OR CAST(s.show_date AS CHAR) LIKE ? " +
                     "ORDER BY s.show_date DESC, s.show_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + query.trim() + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) shows.add(mapResultSetToShow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shows;
    }

    /**
     * Check for time overlaps on the same screen.
     * Returns true if any existing show on the same screen/date overlaps with the given time range.
     */
    public boolean hasTimeOverlapOnScreen(int screenId, LocalDate showDate, java.time.LocalTime startTime,
                                          java.time.LocalTime endTime, int excludeShowId) {
        String sql = "SELECT COUNT(*) FROM shows " +
                     "WHERE screen_id = ? AND show_date = ? AND show_id != ? " +
                     "AND show_time < ? AND (end_time IS NULL OR end_time > ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, screenId);
            stmt.setDate(2, Date.valueOf(showDate));
            stmt.setInt(3, excludeShowId);
            stmt.setTime(4, Time.valueOf(endTime));
            stmt.setTime(5, Time.valueOf(startTime));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasAssociatedBookings(int showId) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE show_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateShow(Show show) {
        String sql = "UPDATE shows SET movie_id = ?, theatre_id = ?, screen_id = ?, show_date = ?, show_time = ?, ticket_price = ?, premium_price = ?, vip_price = ? WHERE show_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, show.getMovieId());
            stmt.setInt(2, show.getTheatreId());
            stmt.setInt(3, show.getScreenId());
            stmt.setDate(4, Date.valueOf(show.getShowDate()));
            stmt.setTime(5, Time.valueOf(show.getShowTime()));
            stmt.setBigDecimal(6, show.getTicketPrice());
            stmt.setObject(7, show.getPremiumPrice(), java.sql.Types.DECIMAL);
            stmt.setObject(8, show.getVipPrice(), java.sql.Types.DECIMAL);
            stmt.setInt(9, show.getShowId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteShow(int showId) {
        String sql = "DELETE FROM shows WHERE show_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Show mapResultSetToShow(ResultSet rs) throws SQLException {
        Show show = new Show(
                rs.getInt("show_id"),
                rs.getInt("movie_id"),
                rs.getInt("theatre_id"),
                rs.getDate("show_date").toLocalDate(),
                rs.getTime("show_time").toLocalTime(),
                rs.getBigDecimal("ticket_price")
        );
        show.setPremiumPrice(rs.getBigDecimal("premium_price"));
        show.setVipPrice(rs.getBigDecimal("vip_price"));
        // Phase 5: screen_id
        try {
            int screenId = rs.getInt("screen_id");
            if (!rs.wasNull()) show.setScreenId(screenId);
        } catch (SQLException ignore) {}
        return show;
    }

    public int countShows() throws SQLException {
        String sql = "SELECT COUNT(*) FROM shows";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
