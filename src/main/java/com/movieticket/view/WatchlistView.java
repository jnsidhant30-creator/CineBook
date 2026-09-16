package com.movieticket.view;

import com.movieticket.dao.WatchlistDAO;
import com.movieticket.model.Movie;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * WatchlistView.java — User Watchlist / Favorites Management View.
 * Displays user's watchlisted movies with options to book or remove.
 */
public class WatchlistView extends JPanel {

    private final WatchlistDAO watchlistDAO;
    private JPanel gridPanel;
    private JLabel emptyNoticeLabel;
    private Consumer<Movie> onBookMovieCallback;

    public WatchlistView() {
        this.watchlistDAO = new WatchlistDAO();
        setLayout(new BorderLayout(14, 14));
        setOpaque(false);
        setBorder(new EmptyBorder(18, 22, 18, 22));

        initComponents();
    }

    public void setOnBookMovieCallback(Consumer<Movie> callback) {
        this.onBookMovieCallback = callback;
    }

    private void initComponents() {
        // 1. Header Bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("MY WATCHLIST & FAVORITES", FontIcon.of(FontAwesomeSolid.HEART, 22, CineBookTheme.DANGER_COLOR), SwingConstants.LEFT);
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        titleLabel.setIconTextGap(10);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content: Scrollable Grid
        gridPanel = new JPanel(new GridLayout(0, 3, 16, 16));
        gridPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CineBookTheme.BG_PRIMARY);
        add(scrollPane, BorderLayout.CENTER);

        // Empty state notice
        emptyNoticeLabel = new JLabel("<html><div style='text-align: center;'>❤️<br><br><b>Your Watchlist is empty.</b><br><font color='#94A3B8'>Browse movies and click 'Add to Watchlist' to save your favorites here!</font></div></html>", SwingConstants.CENTER);
        emptyNoticeLabel.setFont(ThemeManager.getSectionHeaderFont());
        emptyNoticeLabel.setForeground(CineBookTheme.TEXT_MUTED);
    }

    public void loadWatchlist(int userId) {
        gridPanel.removeAll();
        if (userId <= 0) {
            renderEmptyState();
            return;
        }

        List<Movie> list = watchlistDAO.getUserWatchlist(userId);
        if (list.isEmpty()) {
            renderEmptyState();
            return;
        }

        for (Movie m : list) {
            gridPanel.add(createWatchlistMovieCard(userId, m));
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void renderEmptyState() {
        gridPanel.removeAll();
        gridPanel.setLayout(new BorderLayout());
        gridPanel.add(emptyNoticeLabel, BorderLayout.CENTER);
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createWatchlistMovieCard(int userId, Movie m) {
        GlassCardPanel card = new GlassCardPanel(new BorderLayout(10, 10));
        card.setTopAccent(CineBookTheme.DANGER_COLOR, 3);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setPreferredSize(new Dimension(280, 200));

        // Header Title + Rating Badge
        JPanel topBox = new JPanel(new BorderLayout());
        topBox.setOpaque(false);

        JLabel titleLbl = new JLabel(m.getTitle() != null ? m.getTitle().toUpperCase() : "MOVIE");
        titleLbl.setFont(ThemeManager.getHeadingFont(Font.BOLD, 16));
        titleLbl.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel ratingLbl = new JLabel("★ " + (m.getRating() != null ? m.getRating().toString() : "N/A"));
        ratingLbl.setFont(ThemeManager.getFont(Font.BOLD, 12));
        ratingLbl.setForeground(CineBookTheme.ACCENT_GOLD);

        topBox.add(titleLbl, BorderLayout.WEST);
        topBox.add(ratingLbl, BorderLayout.EAST);
        card.add(topBox, BorderLayout.NORTH);

        // Body Details
        JPanel bodyGrid = new JPanel(new GridLayout(3, 1, 4, 4));
        bodyGrid.setOpaque(false);

        JLabel genreLbl = new JLabel("Genre: " + (m.getGenre() != null ? m.getGenre() : "—"));
        genreLbl.setFont(ThemeManager.getBodyFont());
        genreLbl.setForeground(CineBookTheme.ACCENT_CYAN);

        JLabel langLbl = new JLabel("Language: " + (m.getLanguage() != null ? m.getLanguage() : "—"));
        langLbl.setFont(ThemeManager.getBodyFont());
        langLbl.setForeground(CineBookTheme.TEXT_MUTED);

        JLabel durLbl = new JLabel("Duration: " + m.getDuration() + " mins");
        durLbl.setFont(ThemeManager.getBodyFont());
        durLbl.setForeground(CineBookTheme.TEXT_MUTED);

        bodyGrid.add(genreLbl);
        bodyGrid.add(langLbl);
        bodyGrid.add(durLbl);
        card.add(bodyGrid, BorderLayout.CENTER);

        // Action Row: Book Now & Remove
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);

        JButton btnBook = new JButton("BOOK NOW");
        ThemeManager.stylePrimaryButton(btnBook);
        btnBook.setFont(ThemeManager.getFont(Font.BOLD, 11));
        btnBook.addActionListener(e -> {
            if (onBookMovieCallback != null) {
                onBookMovieCallback.accept(m);
            }
        });

        JButton btnRemove = new JButton("Remove");
        ThemeManager.styleSecondaryButton(btnRemove);
        btnRemove.setFont(ThemeManager.getFont(Font.BOLD, 11));
        btnRemove.addActionListener(e -> {
            watchlistDAO.removeFromWatchlist(userId, m.getMovieId());
            loadWatchlist(userId);
        });

        btnRow.add(btnBook);
        btnRow.add(btnRemove);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }
}
