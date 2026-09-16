package com.movieticket.view;

import com.movieticket.controller.TmdbController;
import com.movieticket.model.TmdbMovie;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.TmdbMovieCard;
import com.movieticket.util.WrapLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * TmdbSearchDialog.java — User-facing modal dialog for TMDB search.
 */
public class TmdbSearchDialog extends JDialog {

    private final TmdbController tmdbController;
    private final Consumer<TmdbMovie> onMovieSelected;

    private JTextField searchField;
    private JButton searchButton;
    private JLabel statusLabel;
    private JPanel resultsPanel;

    public TmdbSearchDialog(Frame owner, TmdbController tmdbController, Consumer<TmdbMovie> onMovieSelected) {
        super(owner, "Search Movies (TMDB)", true);
        this.tmdbController = tmdbController;
        this.onMovieSelected = onMovieSelected;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int width = Math.min(950, maxBounds.width - 40);
        int height = Math.min(700, maxBounds.height - 40);
        setSize(width, height);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(owner);

        initComponents();
        getContentPane().setBackground(CineBookTheme.BG_PRIMARY);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(CineBookTheme.BG_PRIMARY);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        root.add(buildTopPanel(), BorderLayout.NORTH);
        root.add(buildCenterPanel(), BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 8));
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Search the TMDB Catalog");
        titleLabel.setFont(ThemeManager.getSectionHeaderFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        
        JLabel subLabel = new JLabel("Find details, cast, and trailers for thousands of movies globally");
        subLabel.setFont(ThemeManager.getBodyFont());
        subLabel.setForeground(CineBookTheme.TEXT_MUTED);
        
        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        titleStack.add(titleLabel);
        titleStack.add(Box.createVerticalStrut(4));
        titleStack.add(subLabel);
        topPanel.add(titleStack, BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        searchRow.setOpaque(false);

        searchField = new JTextField();
        ThemeManager.styleTextField(searchField);
        searchField.setPreferredSize(new Dimension(350, 36));
        searchField.setToolTipText("Enter a movie title (e.g., Inception, Avatar)");
        searchRow.add(searchField);

        searchButton = new JButton("🔍  Search TMDB");
        ThemeManager.stylePrimaryButton(searchButton);
        searchButton.setPreferredSize(new Dimension(160, 36));
        searchRow.add(searchButton);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(ThemeManager.getSmallFont());
        statusLabel.setForeground(CineBookTheme.ACCENT_CYAN);
        searchRow.add(statusLabel);

        topPanel.add(searchRow, BorderLayout.SOUTH);

        searchField.addActionListener(e -> doSearch());
        searchButton.addActionListener(e -> doSearch());

        return topPanel;
    }

    private JScrollPane buildCenterPanel() {
        resultsPanel = new JPanel(new WrapLayout(WrapLayout.LEFT, 15, 15));
        resultsPanel.setBackground(CineBookTheme.BG_PRIMARY);
        
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CineBookTheme.BG_PRIMARY);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private void doSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            statusLabel.setText("⚠ Please enter a movie title.");
            statusLabel.setForeground(CineBookTheme.WARNING_COLOR);
            return;
        }

        setSearching(true);
        resultsPanel.removeAll();
        resultsPanel.revalidate();
        resultsPanel.repaint();
        
        statusLabel.setText("Searching TMDB for \"" + query + "\"…");
        statusLabel.setForeground(CineBookTheme.ACCENT_CYAN);

        tmdbController.searchMovies(query,
            results -> {
                if (results.isEmpty()) {
                    statusLabel.setText("No movies found for \"" + query + "\".");
                    statusLabel.setForeground(CineBookTheme.ACCENT_GOLD);
                } else {
                    statusLabel.setText("Found " + results.size() + " results.");
                    statusLabel.setForeground(CineBookTheme.SUCCESS_COLOR);
                    for (TmdbMovie m : results) {
                        TmdbMovieCard card = new TmdbMovieCard(m);
                        card.setOnClickAction(() -> {
                            if (onMovieSelected != null) {
                                onMovieSelected.accept(m);
                                dispose();
                            }
                        });
                        resultsPanel.add(card);
                    }
                }
                resultsPanel.revalidate();
                resultsPanel.repaint();
            },
            error -> {
                statusLabel.setText("⚠ " + error);
                statusLabel.setForeground(CineBookTheme.DANGER_COLOR);
            },
            () -> setSearching(false)
        );
    }

    private void setSearching(boolean active) {
        searchButton.setEnabled(!active);
        searchButton.setText(active ? "Searching…" : "🔍  Search TMDB");
        searchField.setEnabled(!active);
    }
}
