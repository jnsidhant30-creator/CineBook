package com.movieticket.view;

import com.movieticket.model.RecommendedMovie;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.MovieCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class RecommendationView extends JPanel {

    private final Window owner;
    private JPanel mainContentPanel;
    private JLabel loadingLabel;

    public RecommendationView(Window owner) {
        this.owner = owner;
        setLayout(new BorderLayout());
        setBackground(CineBookTheme.BG_PRIMARY);

        initUI();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Your Personalized Recommendations");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Main Content Area
        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBackground(CineBookTheme.BG_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(mainContentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CineBookTheme.BG_PRIMARY);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Loading label
        loadingLabel = new JLabel("Loading recommendations...");
        loadingLabel.setFont(ThemeManager.getBodyFont());
        loadingLabel.setForeground(CineBookTheme.TEXT_MUTED);
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setBorder(new EmptyBorder(50, 0, 0, 0));
        
        showLoading();
    }

    public void showLoading() {
        mainContentPanel.removeAll();
        mainContentPanel.add(loadingLabel);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    public void populateRecommendations(List<RecommendedMovie> recommendedForYou, 
                                        List<RecommendedMovie> comingSoon, 
                                        List<RecommendedMovie> popularFallback) {
        mainContentPanel.removeAll();
        
        if (!recommendedForYou.isEmpty()) {
            addSection("Recommended For You", recommendedForYou);
        }
        
        if (!comingSoon.isEmpty()) {
            addSection("Coming Soon - You May Like", comingSoon);
        }

        if (recommendedForYou.isEmpty() && !popularFallback.isEmpty()) {
            addSection("Popular on CineBook", popularFallback);
        }

        if (mainContentPanel.getComponentCount() == 0) {
            JLabel noDataLabel = new JLabel("No recommendations available right now.");
            noDataLabel.setFont(ThemeManager.getBodyFont());
            noDataLabel.setForeground(CineBookTheme.TEXT_MUTED);
            noDataLabel.setBorder(new EmptyBorder(30, 20, 30, 20));
            mainContentPanel.add(noDataLabel);
        }

        mainContentPanel.add(Box.createVerticalGlue());
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private java.util.function.Consumer<com.movieticket.model.Movie> onMovieSelected;

    public void setOnMovieSelected(java.util.function.Consumer<com.movieticket.model.Movie> onMovieSelected) {
        this.onMovieSelected = onMovieSelected;
    }

    private void addSection(String sectionTitle, List<RecommendedMovie> movies) {
        JPanel sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setOpaque(false);
        sectionPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JLabel titleLabel = new JLabel(sectionTitle);
        titleLabel.setFont(ThemeManager.getSectionHeaderFont());
        titleLabel.setForeground(CineBookTheme.TEXT_SECONDARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        sectionPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        cardsPanel.setOpaque(false);

        for (RecommendedMovie recMovie : movies) {
            com.movieticket.model.Movie movie = recMovie.getMovie();
            com.movieticket.view.components.MovieCardPanel card = new com.movieticket.view.components.MovieCardPanel(movie);
            card.setOnClickAction(() -> {
                if (onMovieSelected != null) {
                    onMovieSelected.accept(movie);
                }
            });
            cardsPanel.add(card);
        }

        JScrollPane horizontalScroll = new JScrollPane(cardsPanel);
        horizontalScroll.setBorder(null);
        horizontalScroll.getViewport().setBackground(CineBookTheme.BG_PRIMARY);
        horizontalScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        horizontalScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        horizontalScroll.getHorizontalScrollBar().setUnitIncrement(16);
        
        sectionPanel.add(horizontalScroll, BorderLayout.CENTER);
        mainContentPanel.add(sectionPanel);
    }
}
