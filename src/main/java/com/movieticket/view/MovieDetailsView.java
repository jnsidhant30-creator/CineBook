package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * MovieDetailsView - Proper 2-column layout:
 *   LEFT:  Poster (300-350px wide, fixed)
 *   RIGHT: Movie info (fills remaining space, content wraps)
 *
 * Rules:
 *  - No horizontal overflow
 *  - Title uses 28-32px font, wraps on long names
 *  - Description uses JTextArea with lineWrap=true
 *  - Backdrop hidden (not used to avoid giant image)
 *  - Works at 1366x768, 1600x900, 1920x1080
 */
public class MovieDetailsView extends JPanel {

    private JButton backButton;

    // Poster
    private JLabel posterLabel;

    // Info labels
    private JLabel lblTitle;
    private JLabel lblRating;
    private JLabel lblYearRuntimeGenre;
    private JLabel lblLanguage;
    private JTextArea txtPlot;
    private JLabel lblDirector;
    private JLabel lblWriter;
    private JLabel lblStars;

    // Action Buttons
    private JButton btnWatchTrailer;
    private JButton btnBookTickets;
    private JButton btnFavorite;
    private JButton btnWatchlist;

    // Trailer Panel (kept for controller compatibility)
    private JPanel trailerPanel;

    // Backdrop label kept for API compat but never shown
    private JLabel backdropLabel;

    public MovieDetailsView() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        initComponents();
    }

    private void initComponents() {
        // ---- TOP NAV BAR ----
        JPanel topNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        topNav.setOpaque(false);
        topNav.setBorder(new EmptyBorder(8, 20, 0, 20));

        backButton = new JButton(" Back");
        backButton.setIcon(FontIcon.of(FontAwesomeSolid.ARROW_LEFT, 14, CineBookTheme.TEXT_PRIMARY));
        ThemeManager.styleSecondaryButton(backButton);
        backButton.setPreferredSize(new Dimension(110, 36));
        topNav.add(backButton);

        add(topNav, BorderLayout.NORTH);

        // ---- MAIN SCROLL AREA ----
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(10, 24, 32, 24));

        // Hidden backdrop (API compat; we don't render it to avoid layout breakage)
        backdropLabel = new JLabel();
        backdropLabel.setVisible(false);
        backdropLabel.setPreferredSize(new Dimension(1, 1));
        mainContent.add(backdropLabel);

        // ---- HERO SECTION (Poster LEFT + Info RIGHT) ----
        GlassCardPanel heroCard = new GlassCardPanel(new BorderLayout(0, 0));
        heroCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 4);
        heroCard.setBorder(new EmptyBorder(24, 24, 24, 24));
        // Let heroCard always fill the scroll pane width
        heroCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // -- LEFT: Poster panel (fixed 320px) --
        JPanel posterPanel = new JPanel(new BorderLayout());
        posterPanel.setOpaque(false);
        posterPanel.setPreferredSize(new Dimension(320, 480));
        posterPanel.setMinimumSize(new Dimension(200, 300));
        posterPanel.setMaximumSize(new Dimension(340, 500));

        posterLabel = new JLabel("Loading Poster...", SwingConstants.CENTER);
        posterLabel.setForeground(CineBookTheme.TEXT_MUTED);
        posterLabel.setVerticalAlignment(SwingConstants.TOP);
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        posterPanel.add(posterLabel, BorderLayout.NORTH);

        heroCard.add(posterPanel, BorderLayout.WEST);

        // -- SPACER between columns --
        heroCard.add(Box.createHorizontalStrut(28), BorderLayout.LINE_START);

        // -- RIGHT: Info panel --
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(0, 28, 0, 0));

        // Title: 30px bold, wraps via JTextArea-like trick
        // Use a non-editable JTextArea for the title so it wraps long names naturally
        lblTitle = new JLabel("Movie Title");
        lblTitle.setFont(ThemeManager.getFont(Font.BOLD, 30));
        lblTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Rating row
        lblRating = new JLabel(" 0.0 / 10");
        lblRating.setIcon(FontIcon.of(FontAwesomeSolid.STAR, 18, CineBookTheme.ACCENT_GOLD));
        lblRating.setFont(ThemeManager.getFont(Font.BOLD, 18));
        lblRating.setForeground(CineBookTheme.ACCENT_GOLD);
        lblRating.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Year / Runtime / Genre row
        lblYearRuntimeGenre = new JLabel("Year \u2022 Runtime \u2022 Genre");
        lblYearRuntimeGenre.setFont(ThemeManager.getFont(Font.PLAIN, 15));
        lblYearRuntimeGenre.setForeground(CineBookTheme.TEXT_SECONDARY);
        lblYearRuntimeGenre.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Language
        lblLanguage = new JLabel("Language");
        lblLanguage.setFont(ThemeManager.getFont(Font.PLAIN, 14));
        lblLanguage.setForeground(CineBookTheme.TEXT_SECONDARY);
        lblLanguage.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Plot — JTextArea with wrapping
        txtPlot = new JTextArea("Plot description goes here...");
        txtPlot.setLineWrap(true);
        txtPlot.setWrapStyleWord(true);
        txtPlot.setEditable(false);
        txtPlot.setFocusable(false);
        txtPlot.setOpaque(false);
        txtPlot.setFont(ThemeManager.getFont(Font.PLAIN, 14));
        txtPlot.setForeground(CineBookTheme.TEXT_PRIMARY);
        txtPlot.setBorder(BorderFactory.createEmptyBorder(14, 0, 14, 0));
        txtPlot.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Constrain width so it doesn't grow wider than its parent
        txtPlot.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // -- Action Buttons Row --
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnBookTickets = new JButton(" BOOK TICKETS");
        btnBookTickets.setIcon(FontIcon.of(FontAwesomeSolid.TICKET_ALT, 14, Color.WHITE));
        ThemeManager.stylePrimaryButton(btnBookTickets);
        btnBookTickets.setPreferredSize(new Dimension(180, 42));
        btnBookTickets.setFont(ThemeManager.getFont(Font.BOLD, 13));

        btnWatchTrailer = new JButton(" WATCH TRAILER");
        btnWatchTrailer.setIcon(FontIcon.of(FontAwesomeSolid.PLAY, 14, Color.WHITE));
        ThemeManager.styleSecondaryButton(btnWatchTrailer);
        btnWatchTrailer.setPreferredSize(new Dimension(180, 42));
        btnWatchTrailer.setFont(ThemeManager.getFont(Font.BOLD, 13));

        btnFavorite = new JButton(" FAVORITE");
        btnFavorite.setIcon(FontIcon.of(FontAwesomeSolid.HEART, 14, CineBookTheme.TEXT_PRIMARY));
        ThemeManager.styleSecondaryButton(btnFavorite);
        btnFavorite.setPreferredSize(new Dimension(145, 42));
        btnFavorite.setFont(ThemeManager.getFont(Font.BOLD, 13));

        btnWatchlist = new JButton(" WATCHLIST");
        btnWatchlist.setIcon(FontIcon.of(FontAwesomeSolid.BOOKMARK, 14, CineBookTheme.TEXT_PRIMARY));
        ThemeManager.styleSecondaryButton(btnWatchlist);
        btnWatchlist.setPreferredSize(new Dimension(145, 42));
        btnWatchlist.setFont(ThemeManager.getFont(Font.BOLD, 13));

        actionRow.add(btnBookTickets);
        actionRow.add(btnWatchTrailer);
        actionRow.add(btnFavorite);
        actionRow.add(btnWatchlist);

        // Assemble info panel
        infoPanel.add(lblTitle);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(lblRating);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(lblYearRuntimeGenre);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(lblLanguage);
        infoPanel.add(txtPlot);
        infoPanel.add(actionRow);

        heroCard.add(infoPanel, BorderLayout.CENTER);

        mainContent.add(heroCard);
        mainContent.add(Box.createVerticalStrut(20));

        // ---- CAST & CREW SECTION ----
        GlassCardPanel castCard = new GlassCardPanel(new BorderLayout(0, 0));
        castCard.setBorder(new EmptyBorder(20, 24, 20, 24));
        castCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        castCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel castInner = new JPanel();
        castInner.setLayout(new BoxLayout(castInner, BoxLayout.Y_AXIS));
        castInner.setOpaque(false);

        JLabel castHeader = new JLabel("CAST & CREW");
        castHeader.setFont(ThemeManager.getFont(Font.BOLD, 16));
        castHeader.setForeground(CineBookTheme.TEXT_PRIMARY);
        castHeader.setIcon(FontIcon.of(FontAwesomeSolid.USERS, 16, CineBookTheme.ACCENT_CYAN));
        castHeader.setIconTextGap(10);
        castHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblDirector = new JLabel("Director: ");
        lblDirector.setFont(ThemeManager.getBodyFont());
        lblDirector.setForeground(CineBookTheme.TEXT_SECONDARY);
        lblDirector.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblWriter = new JLabel("Writer: ");
        lblWriter.setFont(ThemeManager.getBodyFont());
        lblWriter.setForeground(CineBookTheme.TEXT_SECONDARY);
        lblWriter.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblStars = new JLabel("Stars: ");
        lblStars.setFont(ThemeManager.getBodyFont());
        lblStars.setForeground(CineBookTheme.TEXT_SECONDARY);
        lblStars.setAlignmentX(Component.LEFT_ALIGNMENT);

        castInner.add(castHeader);
        castInner.add(Box.createVerticalStrut(12));
        castInner.add(lblDirector);
        castInner.add(Box.createVerticalStrut(8));
        castInner.add(lblWriter);
        castInner.add(Box.createVerticalStrut(8));
        castInner.add(lblStars);

        castCard.add(castInner, BorderLayout.CENTER);
        mainContent.add(castCard);

        // Trailer panel kept for controller compat
        trailerPanel = new JPanel(new BorderLayout());
        trailerPanel.setOpaque(false);
        trailerPanel.setVisible(false);
        mainContent.add(trailerPanel);

        // ---- SCROLL PANE ----
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
    }

    // ---- Getters ----
    public JButton getBackButton()      { return backButton; }
    public JButton getBtnWatchTrailer() { return btnWatchTrailer; }
    public JButton getBtnBookTickets()  { return btnBookTickets; }
    public JButton getBtnFavorite()     { return btnFavorite; }
    public JButton getBtnWatchlist()    { return btnWatchlist; }
    public JPanel  getTrailerPanel()    { return trailerPanel; }

    public void setTrailerState(boolean available) {
        btnWatchTrailer.setEnabled(true);
        btnWatchTrailer.setText(" WATCH TRAILER");
        btnWatchTrailer.setIcon(FontIcon.of(FontAwesomeSolid.PLAY, 14, Color.WHITE));
    }

    /**
     * Updates all movie details fields.
     * All String values are sanitised to remove mojibake before display.
     */
    public void updateMovieDetails(String title, String rating, String yearRuntimeGenre,
                                   String language, String plot, String director,
                                   String writer, String stars, Image posterImage) {
        lblTitle.setText(sanitize(title));
        lblRating.setText(" " + sanitize(rating));
        lblYearRuntimeGenre.setText(sanitize(yearRuntimeGenre));
        lblLanguage.setText(sanitize(language));
        txtPlot.setText(sanitize(plot));
        txtPlot.setCaretPosition(0);
        lblDirector.setText("Director: " + sanitize(director));
        lblWriter.setText("Writer: "   + sanitize(writer));
        lblStars.setText("Stars: "     + sanitize(stars));

        if (posterImage != null) {
            Image scaled = posterImage.getScaledInstance(310, 460, Image.SCALE_SMOOTH);
            posterLabel.setIcon(new ImageIcon(scaled));
            posterLabel.setText("");
        } else {
            posterLabel.setIcon(null);
            posterLabel.setText("No Poster Available");
        }

        revalidate();
        repaint();
    }

    /**
     * Replaces common mojibake sequences that arise when Latin-1 bytes are
     * misread as UTF-8 or vice-versa, and removes any remaining replacement
     * characters (U+FFFD).
     */
    private String sanitize(String s) {
        if (s == null || s.isBlank() || "N/A".equals(s.trim())) return s == null ? "" : s;

        return s
            // bullet variants
            .replace("\u00e2\u20ac\u00a2", "\u2022")   // â€¢ -> •
            .replace("â€¢", "\u2022")
            // em-dash
            .replace("\u00e2\u20ac\u201d", "\u2014")    // â€" -> —
            .replace("â€\u201d", "\u2014")
            // en-dash
            .replace("\u00e2\u20ac\u201c", "\u2013")
            // left/right double quotes
            .replace("\u00e2\u20ac\u0153", "\u201c")    // â€œ -> "
            .replace("\u00e2\u20ac\u009d", "\u201d")    // â€  -> "
            // left/right single quotes / apostrophe
            .replace("\u00e2\u20ac\u02dc", "\u2018")    // â€˜ -> '
            .replace("\u00e2\u20ac\u2122", "\u2019")    // â€™ -> '
            // star / black star
            .replace("\u00e2\u00ad\u0090", "\u2605")    // â­ -> ★
            .replace("â­", "\u2605")
            // replacement character
            .replace("\ufffd", "")
            .trim();
    }

    /** Backdrop is intentionally suppressed to prevent layout breakage. */
    public void setBackdropImage(Image backdropImage) {
        // Deliberately not rendering backdrop to avoid pushing layout off-screen.
        backdropLabel.setIcon(null);
        backdropLabel.setVisible(false);
    }

    public void setFavoriteState(boolean isFavorite) {
        if (isFavorite) {
            btnFavorite.setText(" UNFAVORITE");
            btnFavorite.setIcon(FontIcon.of(FontAwesomeSolid.HEART, 14, Color.WHITE));
            btnFavorite.setBackground(CineBookTheme.DANGER_COLOR);
        } else {
            btnFavorite.setText(" FAVORITE");
            btnFavorite.setIcon(FontIcon.of(FontAwesomeSolid.HEART, 14, CineBookTheme.TEXT_PRIMARY));
            ThemeManager.styleSecondaryButton(btnFavorite);
        }
    }

    public void setWatchlistState(boolean isWatchlisted) {
        if (isWatchlisted) {
            btnWatchlist.setText(" IN WATCHLIST");
            btnWatchlist.setIcon(FontIcon.of(FontAwesomeSolid.CHECK, 14, Color.WHITE));
            btnWatchlist.setBackground(CineBookTheme.SUCCESS_COLOR);
        } else {
            btnWatchlist.setText(" WATCHLIST");
            btnWatchlist.setIcon(FontIcon.of(FontAwesomeSolid.BOOKMARK, 14, CineBookTheme.TEXT_PRIMARY));
            ThemeManager.styleSecondaryButton(btnWatchlist);
        }
    }
}
