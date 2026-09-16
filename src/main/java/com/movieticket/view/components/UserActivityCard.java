package com.movieticket.view.components;

import com.movieticket.dao.BookingDAO;
import com.movieticket.dao.WatchlistDAO;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.DatabaseConnection;
import com.movieticket.util.ThemeManager;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserActivityCard.java — Personal Booking Statistics Component for User Dashboard.
 * Displays:
 * 🎬 Movies Watched
 * 🎟 Tickets Booked
 * ❤️ Watchlist Count
 * 🎭 Favorite Genre
 */
public class UserActivityCard extends JPanel {

    private final BookingDAO bookingDAO;
    private final WatchlistDAO watchlistDAO;

    private JLabel lblMoviesWatched;
    private JLabel lblTicketsBooked;
    private JLabel lblWatchlistCount;
    private JLabel lblFavoriteGenre;

    public UserActivityCard() {
        this.bookingDAO = new BookingDAO();
        this.watchlistDAO = new WatchlistDAO();

        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(12, 16, 12, 16));

        initComponents();
    }

    private void initComponents() {
        GlassCardPanel card = new GlassCardPanel(new BorderLayout(10, 10));
        card.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel titleLbl = new JLabel("📊 MY ACTIVITY & STATISTICS", FontIcon.of(FontAwesomeSolid.CHART_LINE, 16, CineBookTheme.ACCENT_GOLD), SwingConstants.LEFT);
        titleLbl.setFont(ThemeManager.getHeadingFont(Font.BOLD, 14));
        titleLbl.setForeground(CineBookTheme.TEXT_PRIMARY);
        titleLbl.setIconTextGap(8);
        card.add(titleLbl, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 0));
        grid.setOpaque(false);

        lblMoviesWatched = new JLabel("0");
        lblTicketsBooked = new JLabel("0");
        lblWatchlistCount = new JLabel("0");
        lblFavoriteGenre = new JLabel("—");

        grid.add(createStatCell("🎬 MOVIES WATCHED", lblMoviesWatched, CineBookTheme.ACCENT_PURPLE));
        grid.add(createStatCell("🎟 TICKETS BOOKED", lblTicketsBooked, CineBookTheme.SUCCESS_COLOR));
        grid.add(createStatCell("❤️ WATCHLIST", lblWatchlistCount, CineBookTheme.DANGER_COLOR));
        grid.add(createStatCell("🎭 FAVORITE GENRE", lblFavoriteGenre, CineBookTheme.ACCENT_GOLD));

        card.add(grid, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    public void loadUserData(int userId) {
        if (userId <= 0) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int moviesCount = 0;
            int ticketsCount = 0;
            int watchlistCount = 0;
            String favGenre = "—";

            @Override
            protected Void doInBackground() {
                watchlistCount = watchlistDAO.getWatchlistCount(userId);

                String sql = "SELECT COUNT(DISTINCT s.movie_id) AS movies_cnt, COUNT(bd.id) AS tix_cnt " +
                             "FROM bookings b " +
                             "JOIN shows s ON b.show_id = s.show_id " +
                             "LEFT JOIN booking_details bd ON b.booking_id = bd.booking_id " +
                             "WHERE b.user_id = ? AND b.status = 'CONFIRMED'";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            moviesCount = rs.getInt("movies_cnt");
                            ticketsCount = rs.getInt("tix_cnt");
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("[UserActivityCard] Error fetching booking stats: " + e.getMessage());
                }

                String genreSql = "SELECT m.genre, COUNT(*) AS cnt FROM bookings b " +
                                  "JOIN shows s ON b.show_id = s.show_id " +
                                  "JOIN movies m ON s.movie_id = m.movie_id " +
                                  "WHERE b.user_id = ? AND b.status = 'CONFIRMED' " +
                                  "GROUP BY m.genre ORDER BY cnt DESC LIMIT 1";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(genreSql)) {
                    stmt.setInt(1, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            favGenre = rs.getString("genre");
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("[UserActivityCard] Error fetching favorite genre: " + e.getMessage());
                }

                return null;
            }

            @Override
            protected void done() {
                lblMoviesWatched.setText(String.valueOf(moviesCount));
                lblTicketsBooked.setText(String.valueOf(ticketsCount));
                lblWatchlistCount.setText(String.valueOf(watchlistCount));
                lblFavoriteGenre.setText(favGenre != null ? favGenre.toUpperCase() : "—");
            }
        };
        worker.execute();
    }

    private JPanel createStatCell(String title, JLabel valueLbl, Color accentColor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(ThemeManager.getSmallFont());
        t.setForeground(CineBookTheme.TEXT_MUTED);

        valueLbl.setFont(ThemeManager.getHeadingFont(Font.BOLD, 18));
        valueLbl.setForeground(accentColor);

        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(valueLbl);
        return p;
    }
}
