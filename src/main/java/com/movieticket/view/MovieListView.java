package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * User Movie Browsing screen â€” Premium Cinema Dashboard.
 * Incorporates a Hero Movie Banner and horizontal scrolling movie cards.
 */
public class MovieListView extends JPanel {

    // Top Search & Welcome Bar
    private JLabel welcomeLabel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton refreshButton;
    private JComboBox<String> genreFilterCombo;
    private JComboBox<String> languageFilterCombo;
    private JComboBox<String> dateFilterCombo;

    // Hero Section
    private JPanel heroPanel;
    private JLabel heroTitleLabel;
    private JLabel heroPlotLabel;
    private JLabel heroMetaLabel;
    private JLabel heroPoster;
    private JButton btnWatchTrailer;
    private JButton btnBookTickets;
    private JButton btnFavorite;

    // Movies Grid
    private com.movieticket.view.components.ResponsiveGridPanel nowPlayingGridPanel;
    private com.movieticket.view.components.ResponsiveGridPanel recommendedGridPanel;

    // Legacy fields needed by controller (temporarily returning nowPlaying grid for backward compatibility)
    private com.movieticket.view.components.ResponsiveGridPanel moviesGridPanel;
    private com.movieticket.view.components.ResponsiveGridPanel topRatedGridPanel;
    private com.movieticket.view.components.ResponsiveGridPanel upcomingGridPanel;
    private com.movieticket.view.components.ResponsiveGridPanel popularGridPanel;

    public MovieListView() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBackground(CineBookTheme.BG_PRIMARY);

        initComponents();
    }

    private void initComponents() {
        // Main container with vertical scrolling
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(0, 0, 40, 0));

        // 1. Top Panel: Welcome Message + CinePoints
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(32, 40, 0, 40));


        // Welcome Box
        JPanel welcomeBox = new JPanel();
        welcomeBox.setLayout(new BoxLayout(welcomeBox, BoxLayout.Y_AXIS));
        welcomeBox.setOpaque(false);
        welcomeBox.setBorder(new EmptyBorder(15, 30, 0, 0));

        welcomeLabel = new JLabel("Welcome back!");
        welcomeLabel.setFont(ThemeManager.getFont(Font.BOLD, 24));
        welcomeLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Find your next movie experience.");
        subtitleLabel.setFont(ThemeManager.getFont(Font.PLAIN, 14));
        subtitleLabel.setForeground(CineBookTheme.TEXT_MUTED);

        welcomeBox.add(welcomeLabel);
        welcomeBox.add(Box.createVerticalStrut(6));
        welcomeBox.add(subtitleLabel);

        topPanel.add(welcomeBox);
        mainContent.add(topPanel);

        // 2. Search and Filters Bar (Horizontal Row)
        JPanel controlsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        controlsBar.setOpaque(false);
        controlsBar.setBorder(new EmptyBorder(25, 40, 25, 40));



        genreFilterCombo = new JComboBox<>(new String[]{
                "Genre", "Action", "Biography", "Comedy", "Drama", "Horror", "Romance", "Sci-Fi", "Thriller", "Adventure"
        });
        ThemeManager.styleComboBox(genreFilterCombo);
        genreFilterCombo.setPreferredSize(new Dimension(120, 40));
        controlsBar.add(genreFilterCombo);

        languageFilterCombo = new JComboBox<>(new String[]{
                "Language", "English", "Hindi", "Tamil", "Telugu", "Kannada", "Malayalam"
        });
        ThemeManager.styleComboBox(languageFilterCombo);
        languageFilterCombo.setPreferredSize(new Dimension(120, 40));
        controlsBar.add(languageFilterCombo);
        
        dateFilterCombo = new JComboBox<>(new String[]{"Date", "Today", "Tomorrow", "Weekend"});
        ThemeManager.styleComboBox(dateFilterCombo);
        dateFilterCombo.setPreferredSize(new Dimension(120, 40));
        controlsBar.add(dateFilterCombo);


        refreshButton = new JButton();
        refreshButton.setIcon(FontIcon.of(FontAwesomeSolid.SYNC_ALT, 14, Color.WHITE));
        ThemeManager.styleSecondaryButton(refreshButton);
        refreshButton.setPreferredSize(new Dimension(45, 40));
        controlsBar.add(refreshButton);

        mainContent.add(controlsBar);

        // 3. Hero Section (Featured Movie Card)
        JPanel heroContainer = new JPanel(new BorderLayout());
        heroContainer.setOpaque(false);
        heroContainer.setBorder(new EmptyBorder(0, 40, 32, 40));

        heroPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(28, 14, 58), getWidth(), getHeight(), CineBookTheme.BG_CARD);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(CineBookTheme.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        heroPanel.setOpaque(false);
        heroPanel.setPreferredSize(new Dimension(750, 155));
        heroPanel.setMaximumSize(new Dimension(800, 160));

        JPanel heroContent = new JPanel();
        heroContent.setLayout(new BoxLayout(heroContent, BoxLayout.Y_AXIS));
        heroContent.setOpaque(false);
        heroContent.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        heroTitleLabel = new JLabel("");
        heroTitleLabel.setFont(ThemeManager.getFont(Font.BOLD, 22));
        heroTitleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        heroMetaLabel = new JLabel("");
        heroMetaLabel.setIcon(FontIcon.of(FontAwesomeSolid.STAR, 12, CineBookTheme.ACCENT_GOLD));
        heroMetaLabel.setFont(ThemeManager.getFont(Font.PLAIN, 12));
        heroMetaLabel.setForeground(CineBookTheme.ACCENT_GOLD);

        heroPlotLabel = new JLabel("");
        heroPlotLabel.setFont(ThemeManager.getFont(Font.PLAIN, 12));
        heroPlotLabel.setForeground(CineBookTheme.TEXT_MUTED);

        JPanel heroButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        heroButtons.setOpaque(false);
        heroButtons.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        btnWatchTrailer = new JButton(" WATCH TRAILER");
        ThemeManager.styleSecondaryButton(btnWatchTrailer);
        btnWatchTrailer.setPreferredSize(new Dimension(140, 32));
        btnWatchTrailer.setFont(ThemeManager.getFont(Font.BOLD, 11));

        btnBookTickets = new JButton(" BOOK TICKETS");
        btnBookTickets.setIcon(FontIcon.of(FontAwesomeSolid.TICKET_ALT, 12, Color.WHITE));
        ThemeManager.stylePrimaryButton(btnBookTickets);
        btnBookTickets.setPreferredSize(new Dimension(140, 32));
        btnBookTickets.setFont(ThemeManager.getFont(Font.BOLD, 11));
        
        btnFavorite = new JButton(" FAVOURITE");
        btnFavorite.setIcon(FontIcon.of(FontAwesomeSolid.HEART, 12, CineBookTheme.TEXT_MUTED));
        ThemeManager.styleSecondaryButton(btnFavorite);
        btnFavorite.setPreferredSize(new Dimension(120, 32));
        btnFavorite.setFont(ThemeManager.getFont(Font.BOLD, 11));

        heroButtons.add(btnWatchTrailer);
        heroButtons.add(btnBookTickets);
        heroButtons.add(btnFavorite);

        heroContent.add(heroTitleLabel);
        heroContent.add(Box.createVerticalStrut(4));
        heroContent.add(heroMetaLabel);
        heroContent.add(Box.createVerticalStrut(8));
        heroContent.add(heroPlotLabel);
        heroContent.add(heroButtons);
        
        // Dummy Poster Label for mockup aesthetics
        heroPoster = new JLabel();
        heroPoster.setPreferredSize(new Dimension(100, 130));
        heroPoster.setOpaque(true);
        heroPoster.setBackground(Color.DARK_GRAY);
        heroPoster.setBorder(new EmptyBorder(10, 20, 10, 0));

        heroPanel.add(heroPoster, BorderLayout.WEST);
        heroPanel.add(heroContent, BorderLayout.CENTER);
        
        heroContainer.add(heroPanel, BorderLayout.WEST); // Aligned left
        mainContent.add(heroContainer);

        // 4. NOW SHOWING
        nowPlayingGridPanel = new com.movieticket.view.components.ResponsiveGridPanel();
        mainContent.add(createSectionHeader("NOW SHOWING", null));
        mainContent.add(nowPlayingGridPanel);
        mainContent.add(Box.createVerticalStrut(20));
        
        // 5. RECOMMENDED FOR YOU
        recommendedGridPanel = new com.movieticket.view.components.ResponsiveGridPanel();
        mainContent.add(createSectionHeader("RECOMMENDED FOR YOU", null));
        mainContent.add(recommendedGridPanel);
        mainContent.add(Box.createVerticalStrut(30));

        // Legacy compatibility mappings
        moviesGridPanel = nowPlayingGridPanel;
        topRatedGridPanel = recommendedGridPanel;
        upcomingGridPanel = recommendedGridPanel;
        popularGridPanel = nowPlayingGridPanel;

        JScrollPane mainScrollPane = new JScrollPane(mainContent);
        mainScrollPane.setOpaque(false);
        mainScrollPane.getViewport().setOpaque(false);
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(mainScrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createSectionHeader(String titleText, org.kordamp.ikonli.Ikon icon) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 40, 10, 40));
        JLabel title = new JLabel(titleText);
        if (icon != null) {
            title.setIcon(FontIcon.of(icon, 16, CineBookTheme.ACCENT_CYAN));
        }
        title.setFont(ThemeManager.getFont(Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        return header;
    }

    public void setWelcomeName(String name) {
        welcomeLabel.setText("Welcome back, " + name + "!");
    }

    // Getters for controller bindings
    public JTextField getSearchField() { return searchField; }
    public JButton getSearchButton() { return searchButton; }
    public JButton getRefreshButton() { return refreshButton; }
    public JComboBox<String> getGenreFilterCombo() { return genreFilterCombo; }
    public JComboBox<String> getLanguageFilterCombo() { return languageFilterCombo; }
    public JPanel getMoviesGridPanel() { return moviesGridPanel; }
    public JPanel getTopRatedGridPanel() { return topRatedGridPanel; }
    public JPanel getUpcomingGridPanel() { return upcomingGridPanel; }
    public JPanel getPopularGridPanel() { return popularGridPanel; }
    public JPanel getNowPlayingGridPanel() { return nowPlayingGridPanel; }
    public JPanel getHeroPanel() { return heroPanel; }
    public JLabel getHeroTitleLabel() { return heroTitleLabel; }
    public JLabel getHeroPlotLabel() { return heroPlotLabel; }

    public void setHeroMovie(com.movieticket.model.Movie movie, java.awt.Image poster) {
        if (movie == null) {
            heroPanel.setVisible(false);
            return;
        }
        heroPanel.setVisible(true);
        heroTitleLabel.setText(movie.getTitle());
        String rating = movie.getRating() != null ? movie.getRating().toPlainString() : "N/A";
        String meta = " " + rating + "  • " + movie.getGenre() + " • " + movie.getLanguage();
        if (movie.getDuration() > 0) meta += " • " + movie.getDuration() + " min";
        heroMetaLabel.setText(meta);
        
        String plot = "No description available."; // We don't have description in base Movie model, but we can set something
        heroPlotLabel.setText("<html><body style='width: 450px;'>" + plot + "</body></html>");
        
        if (poster != null) {
            heroPoster.setIcon(new ImageIcon(poster));
            heroPoster.setText("");
        } else {
            heroPoster.setIcon(null);
            heroPoster.setText("No Image");
        }
    }

    public JButton getBtnWatchTrailer() { return btnWatchTrailer; }
    public JButton getBtnBookTickets() { return btnBookTickets; }
    public JButton getBtnFavorite() { return btnFavorite; }
}

