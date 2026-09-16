package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.PanelTransitionManager;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Customer Portal Frame — Modernized Cinema Booking Portal.
 * Integrates FlatLaf dark look-and-feel, Ikonli FontAwesome vector icons, PanelTransitionManager
 * with smooth fade/slide transitions, and customer profile avatar section.
 */
public class UserDashboardFrame extends JFrame {

    private PanelTransitionManager transitionManager;

    // Navigation Buttons
    private JButton btnBrowseMovies;
    private JButton btnMyBookings;
    private JButton btnWatchlist;
    private JButton btnRecommendations;
    private JButton btnCinePoints;
    private JButton btnProfile;
    private JButton btnNotifications;
    private JComboBox<CineBookTheme.ThemeType> themeComboBox;
    private JButton btnLogout;
    private Map<String, JButton> sidebarButtons = new HashMap<>();

    // Header Notification Controls
    private JLabel welcomeLabel;
    private JButton notificationBellButton;
    private JLabel notificationBadgeLabel;
    private JTextField headerSearchField;

    // Sub-Views
    private MovieListView movieListView;
    private MovieDetailsView movieDetailsView;
    private ShowSelectionView showSelectionView;
    private SeatSelectionView seatSelectionView;
    private BookingConfirmationView bookingConfirmationView;
    private PaymentSimulationView paymentSimulationView;
    private BookingHistoryView bookingHistoryView;
    private WatchlistView watchlistView;
    private RecommendationView recommendationView;
    private CinePointsWalletView cinePointsWalletView;
    private com.movieticket.view.components.UserActivityCard userActivityCard;

    public UserDashboardFrame() {
        setTitle(ThemeManager.APP_DISPLAY_NAME + " — Customer Booking Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int targetWidth = Math.min(1600, maxBounds.width);
        int targetHeight = Math.min(900, maxBounds.height);
        
        setSize(targetWidth, targetHeight);
        setMinimumSize(new Dimension(1280, 720));
        setLocationRelativeTo(null);
        ThemeManager.applyWindowIcon(this);

        // Root Container: Cinematic Background Panel (Stronger cinematic lighting for customer portal)
        com.movieticket.view.components.CinematicBackgroundPanel rootPanel = new com.movieticket.view.components.CinematicBackgroundPanel(com.movieticket.view.components.CinematicBackgroundPanel.Intensity.STRONG, 0.70f);
        rootPanel.setLayout(new BorderLayout());
        setContentPane(rootPanel);

        // 1. Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(8, 20, 38, 240));
        headerPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CineBookTheme.BORDER_COLOR),
                new EmptyBorder(14, 25, 14, 25)
        ));

        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleBox.setOpaque(false);

        JLabel miniLogo = new JLabel(ThemeManager.getLogoIcon(28, 28));
        JLabel titleLabel = new JLabel(ThemeManager.APP_DISPLAY_NAME.toUpperCase());
        titleLabel.setFont(ThemeManager.getFont(Font.BOLD, 22));
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Movie Ticket Management System");
        subtitleLabel.setFont(ThemeManager.getSmallFont());
        subtitleLabel.setForeground(CineBookTheme.TEXT_MUTED);

        JPanel brandTitleBox = new JPanel();
        brandTitleBox.setLayout(new BoxLayout(brandTitleBox, BoxLayout.Y_AXIS));
        brandTitleBox.setOpaque(false);
        brandTitleBox.add(titleLabel);
        brandTitleBox.add(subtitleLabel);

        titleBox.add(miniLogo);
        titleBox.add(brandTitleBox);
        headerPanel.add(titleBox, BorderLayout.WEST);
        
        // Add Header Search Box
        JPanel centerHeaderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        centerHeaderPanel.setOpaque(false);
        headerSearchField = new JTextField();
        ThemeManager.styleTextField(headerSearchField);
        headerSearchField.setPreferredSize(new Dimension(300, 38));
        headerSearchField.putClientProperty("JTextField.placeholderText", "Search...");
        centerHeaderPanel.add(headerSearchField);
        headerPanel.add(centerHeaderPanel, BorderLayout.CENTER);

        // Profile & Notification Section (Right Side)
        JPanel profileBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        profileBox.setOpaque(false);

        notificationBellButton = new JButton();
        notificationBellButton.setIcon(FontIcon.of(FontAwesomeSolid.BELL, 20, CineBookTheme.TEXT_PRIMARY));
        notificationBellButton.setFocusPainted(false);
        notificationBellButton.setContentAreaFilled(false);
        notificationBellButton.setBorderPainted(false);
        notificationBellButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notificationBellButton.setToolTipText("Notifications");

        notificationBadgeLabel = new JLabel(" 0 ", SwingConstants.CENTER);
        notificationBadgeLabel.setFont(ThemeManager.getSmallFont());
        notificationBadgeLabel.setForeground(Color.WHITE);
        notificationBadgeLabel.setOpaque(true);
        notificationBadgeLabel.setBackground(CineBookTheme.ACCENT_PURPLE);
        notificationBadgeLabel.setBorder(new EmptyBorder(2, 6, 2, 6));
        notificationBadgeLabel.setVisible(false);

        JPanel bellPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        bellPanel.setOpaque(false);
        bellPanel.add(notificationBellButton);
        bellPanel.add(notificationBadgeLabel);

        JLabel avatarIcon = new JLabel(FontIcon.of(FontAwesomeSolid.USER_CIRCLE, 28, CineBookTheme.ACCENT_GOLD));

        JPanel profileText = new JPanel();
        profileText.setLayout(new BoxLayout(profileText, BoxLayout.Y_AXIS));
        profileText.setOpaque(false);

        welcomeLabel = new JLabel("Customer");
        welcomeLabel.setFont(ThemeManager.getFont(Font.BOLD, 14));
        welcomeLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel roleLabel = new JLabel("Profile");
        roleLabel.setFont(ThemeManager.getSmallFont());
        roleLabel.setForeground(CineBookTheme.ACCENT_GOLD);

        profileText.add(welcomeLabel);
        profileText.add(roleLabel);

        profileBox.add(bellPanel);
        profileBox.add(Box.createHorizontalStrut(10));
        profileBox.add(avatarIcon);
        profileBox.add(profileText);
        headerPanel.add(profileBox, BorderLayout.EAST);

        // Removed userActivityCard

        add(headerPanel, BorderLayout.NORTH);

        // 2. Sidebar Panel (#050B16 @ 92% opacity)
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(5, 11, 22, 235));
        sidebarPanel.setPreferredSize(new Dimension(190, 0));
        sidebarPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, CineBookTheme.BORDER_COLOR),
                new EmptyBorder(12, 0, 12, 0)
        ));

        // Navigation Sidebar Buttons
        btnBrowseMovies = createSidebarButton("Browse", "Browse Movies", FontAwesomeSolid.FILM);
        btnRecommendations = createSidebarButton("Recommended", "Recommendations", FontAwesomeSolid.STAR);
        btnMyBookings   = createSidebarButton("Bookings", "My Bookings", FontAwesomeSolid.TICKET_ALT);
        btnWatchlist    = createSidebarButton("Watchlist", "My Watchlist", FontAwesomeSolid.HEART);
        btnCinePoints   = createSidebarButton("CinePoints", "CinePoints", FontAwesomeSolid.GIFT);
        btnProfile      = createSidebarButton("Profile", "Profile", FontAwesomeSolid.USER);
        btnNotifications= createSidebarButton("Notifications", "Notifications", FontAwesomeSolid.BELL);

        btnLogout = new JButton("Logout");
        ThemeManager.styleSecondaryButton(btnLogout);
        btnLogout.setMaximumSize(new Dimension(150, 40));
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebarPanel.add(btnBrowseMovies);
        sidebarPanel.add(btnRecommendations);
        sidebarPanel.add(btnMyBookings);
        sidebarPanel.add(btnWatchlist);
        sidebarPanel.add(btnCinePoints);
        sidebarPanel.add(btnProfile);
        sidebarPanel.add(btnNotifications);
        sidebarPanel.add(Box.createVerticalGlue());
        
        // Theme Switcher
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        themePanel.setOpaque(false);
        JLabel themeLabel = new JLabel(FontIcon.of(FontAwesomeSolid.ADJUST, 16, CineBookTheme.TEXT_MUTED));
        themeComboBox = new JComboBox<>(CineBookTheme.ThemeType.values());
        ThemeManager.styleComboBox(themeComboBox);
        themeComboBox.setPreferredSize(new Dimension(140, 30));
        
        // Custom renderer is not needed if the enum's toString() is overridden, which we did!
        themeComboBox.setSelectedItem(CineBookTheme.getCurrentTheme());
        themeComboBox.addActionListener(e -> {
            CineBookTheme.ThemeType selected = (CineBookTheme.ThemeType) themeComboBox.getSelectedItem();
            if (selected != null) {
                CineBookTheme.setTheme(selected);
            }
        });
        themePanel.add(themeLabel);
        themePanel.add(themeComboBox);
        
        sidebarPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        sidebarPanel.add(themePanel);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnLogout);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        add(sidebarPanel, BorderLayout.WEST);

        // 3. Instantiate Real Views
        movieListView = new MovieListView();
        movieDetailsView = new MovieDetailsView();
        showSelectionView = new ShowSelectionView();
        seatSelectionView = new SeatSelectionView();
        bookingConfirmationView = new BookingConfirmationView();
        paymentSimulationView = new PaymentSimulationView();
        bookingHistoryView = new BookingHistoryView();
        watchlistView = new WatchlistView();
        recommendationView = new RecommendationView(this);
        cinePointsWalletView = new CinePointsWalletView();

        // 4. Main Content Area with PanelTransitionManager
        transitionManager = new PanelTransitionManager();

        transitionManager.addPanel(movieListView, "Browse Movies");
        transitionManager.addPanel(movieDetailsView, "Movie Details");
        transitionManager.addPanel(showSelectionView, "Select Show");
        transitionManager.addPanel(seatSelectionView, "Select Seats");
        transitionManager.addPanel(bookingConfirmationView, "Confirm Booking");
        transitionManager.addPanel(paymentSimulationView, "Payment");
        transitionManager.addPanel(bookingHistoryView, "My Bookings");
        transitionManager.addPanel(watchlistView, "My Watchlist");
        transitionManager.addPanel(recommendationView, "Recommendations");
        transitionManager.addPanel(cinePointsWalletView, "CinePoints");

        add(transitionManager, BorderLayout.CENTER);

        setActiveSidebarButton("Browse Movies");
    }

    private JButton createSidebarButton(String text, String key, Ikon iconCode) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(180, 44));
        button.setFont(ThemeManager.getLabelFont());
        button.setForeground(CineBookTheme.TEXT_MUTED);
        button.setBackground(CineBookTheme.BG_SECONDARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (iconCode != null) {
            button.setIcon(FontIcon.of(iconCode, 18, CineBookTheme.TEXT_MUTED));
            button.setIconTextGap(12);
        }

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!activeCardKey.equalsIgnoreCase(key)) {
                    button.setBackground(CineBookTheme.BG_CARD);
                    button.setForeground(CineBookTheme.TEXT_PRIMARY);
                    button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 7)); // 3px shift
                    if (button.getIcon() instanceof FontIcon) {
                        ((FontIcon) button.getIcon()).setIconColor(CineBookTheme.ACCENT_CYAN);
                    }
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!activeCardKey.equalsIgnoreCase(key)) {
                    button.setBackground(CineBookTheme.BG_SECONDARY);
                    button.setForeground(CineBookTheme.TEXT_MUTED);
                    button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
                    if (button.getIcon() instanceof FontIcon) {
                        ((FontIcon) button.getIcon()).setIconColor(CineBookTheme.TEXT_MUTED);
                    }
                }
            }
        });

        sidebarButtons.put(key, button);
        return button;
    }

    private String activeCardKey = "Browse Movies";

    public void setActiveSidebarButton(String key) {
        this.activeCardKey = key;
        for (Map.Entry<String, JButton> entry : sidebarButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equalsIgnoreCase(key)) {
                btn.setBackground(CineBookTheme.BG_CARD_HOVER);
                btn.setForeground(CineBookTheme.TEXT_PRIMARY);
                btn.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, CineBookTheme.ACCENT_PURPLE));
                if (btn.getIcon() instanceof FontIcon) {
                    ((FontIcon) btn.getIcon()).setIconColor(CineBookTheme.ACCENT_PURPLE);
                }
            } else {
                btn.setBackground(CineBookTheme.BG_SECONDARY);
                btn.setForeground(CineBookTheme.TEXT_MUTED);
                btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
                if (btn.getIcon() instanceof FontIcon) {
                    ((FontIcon) btn.getIcon()).setIconColor(CineBookTheme.TEXT_MUTED);
                }
            }
        }
    }

    public void showCard(String cardName) {
        if (sidebarButtons.containsKey(cardName)) {
            setActiveSidebarButton(cardName);
        }
        transitionManager.showCardAnimated(cardName);
    }

    public void setWelcomeMessage(String name) {
        welcomeLabel.setText(name);
        if (movieListView != null) {
            movieListView.setWelcomeName(name);
        }
    }

    // View Getters
    public MovieListView getMovieListView() { return movieListView; }
    public MovieDetailsView getMovieDetailsView() { return movieDetailsView; }
    public ShowSelectionView getShowSelectionView() { return showSelectionView; }
    public SeatSelectionView getSeatSelectionView() { return seatSelectionView; }
    public BookingConfirmationView getBookingConfirmationView() { return bookingConfirmationView; }
    public PaymentSimulationView getPaymentSimulationView() { return paymentSimulationView; }
    public BookingHistoryView getBookingHistoryView() { return bookingHistoryView; }
    public WatchlistView getWatchlistView() { return watchlistView; }
    public RecommendationView getRecommendationView() { return recommendationView; }
    public CinePointsWalletView getCinePointsWalletView() { return cinePointsWalletView; }
    public com.movieticket.view.components.UserActivityCard getUserActivityCard() { return userActivityCard; }
    public CardLayout getCardLayout() { return transitionManager.getCardLayout(); }
    public JPanel getMainContentPanel() { return transitionManager; }

    public JTextField getHeaderSearchField() { return headerSearchField; }
    public JButton getNotificationBellButton() { return notificationBellButton; }
    
    public void setUnreadNotificationCount(int count) {
        if (count > 0) {
            notificationBadgeLabel.setText(" " + count + " ");
            notificationBadgeLabel.setVisible(true);
            notificationBellButton.setToolTipText(count + " Unread Notifications");
        } else {
            notificationBadgeLabel.setVisible(false);
            notificationBellButton.setToolTipText("Notifications");
        }
    }

    // Action Listeners
    public void setBrowseMoviesAction(ActionListener listener) { btnBrowseMovies.addActionListener(listener); }
    public void setRecommendationsAction(ActionListener listener) { btnRecommendations.addActionListener(listener); }
    public void setMyBookingsAction(ActionListener listener) { btnMyBookings.addActionListener(listener); }
    public void setWatchlistAction(ActionListener listener) { btnWatchlist.addActionListener(listener); }
    public void setCinePointsAction(ActionListener listener) { btnCinePoints.addActionListener(listener); }
    public void setProfileAction(ActionListener listener) { btnProfile.addActionListener(listener); }
    public void setNotificationsAction(ActionListener listener) { btnNotifications.addActionListener(listener); }
    public void setLogoutAction(ActionListener listener) { btnLogout.addActionListener(listener); }
}
