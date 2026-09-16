package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.AnimatedCounterLabel;
import com.movieticket.view.components.PanelTransitionManager;
import com.movieticket.view.components.RoundedButton;
import com.movieticket.view.components.RoundedPanel;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * AdminDashboardFrame.java — End-to-End Modernized Admin Operations Portal.
 * Integrates FlatLaf dark look-and-feel, Ikonli FontAwesome vector icons, PanelTransitionManager
 * with smooth fade/slide transitions, RoundedButton navigation, and AnimatedCounterLabel
 * metric cards.
 */
public class AdminDashboardFrame extends JFrame {

    private PanelTransitionManager transitionManager;

    // Sidebar Navigation Buttons
    private JButton btnDashboard;
    private JButton btnMovies;
    private JButton btnTheatres;
    private JButton btnShows;
    private JButton btnSeats;
    private JButton btnUpcoming;
    private JButton btnAnalytics; // Phase 6: Analytics Dashboard
    private JButton btnBookings;
    private JButton btnUsers;
    private JButton btnReports;
    private JButton btnVerifyTicketSidebar;
    private JButton btnReviews; // Phase 7
    private JButton btnCinePoints;
    private JButton btnCoupons;
    private JButton btnLogout;
    private Map<String, JButton> sidebarButtons = new HashMap<>();

    // Quick Action Buttons
    private RoundedButton quickAddMovieBtn;
    private RoundedButton quickAddShowBtn;
    private RoundedButton quickViewBookingsBtn;
    private RoundedButton quickViewReportsBtn;
    private RoundedButton btnViewUpcomingMoviesDashboard;
    private RoundedButton btnSyncTmdb;

    // Header Components
    private JLabel welcomeLabel;

    // Dashboard Statistics Components
    private RoundedButton btnRefresh;
    private RoundedButton btnVerifyTicket;
    private AnimatedCounterLabel lblTotalUsers;
    private AnimatedCounterLabel lblTotalMovies;
    private AnimatedCounterLabel lblTotalTheatres;
    private AnimatedCounterLabel lblTotalScreens;
    private AnimatedCounterLabel lblTotalShows;
    private AnimatedCounterLabel lblTotalSeats;
    private AnimatedCounterLabel lblTotalBookings;
    private AnimatedCounterLabel lblTotalTickets;
    private AnimatedCounterLabel lblTotalConfirmed;
    private AnimatedCounterLabel lblTotalRevenue;

    public AnimatedCounterLabel getLblTotalTickets() { return lblTotalTickets; }
    public AnimatedCounterLabel getLblTotalConfirmed() { return lblTotalConfirmed; }
    public AnimatedCounterLabel getLblTotalRevenue() { return lblTotalRevenue; }
    
    public JLabel getLblNewUpcoming() { return lblNewUpcoming; }
    public JLabel getLblTotalUpcoming() { return lblTotalUpcoming; }
    public JLabel getLblTotalRecent() { return lblTotalRecent; }
    private JLabel lblNewUpcoming;
    private JLabel lblTotalUpcoming;
    private JLabel lblTotalRecent;

    // Integrated Modules
    private MovieManagementView movieManagementView;
    private TheatreManagementView theatreManagementView;
    private TheatreScreenManagementView theatreScreenManagementView; // Phase 5
    private ShowManagementView showManagementView;
    private SeatManagementView seatManagementView;
    private BookingManagementView bookingManagementView;
    private UpcomingMoviesView upcomingMoviesView;
    private UserManagementView userManagementView;
    private ReportManagementView reportManagementView;
    private VerifyTicketView verifyTicketView;
    private AdminAnalyticsView analyticsView;       // Phase 6
    private AdminReviewsView adminReviewsView;
    private AdminCinePointsView adminCinePointsView;
    private AdminCouponManagementView adminCouponManagementView;
    private AuditLogView auditLogView;              // Feature 12: Security Audit Log

    public AdminDashboardFrame() {
        setTitle(ThemeManager.APP_DISPLAY_NAME + " — Admin Operations Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int targetWidth = Math.min(1600, maxBounds.width);
        int targetHeight = Math.min(900, maxBounds.height);
        
        setSize(targetWidth, targetHeight);
        setMinimumSize(new Dimension(1100, 740));
        setLocationRelativeTo(null);
        ThemeManager.applyWindowIcon(this);

        // Root Container: Subtle Cinematic Background Panel
        com.movieticket.view.components.CinematicBackgroundPanel rootPanel = new com.movieticket.view.components.CinematicBackgroundPanel(com.movieticket.view.components.CinematicBackgroundPanel.Intensity.SUBTLE, 0.85f);
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

        // Profile Section (Right Side)
        JPanel profileBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        profileBox.setOpaque(false);

        JLabel avatarIcon = new JLabel(FontIcon.of(FontAwesomeSolid.USER_CIRCLE, 28, CineBookTheme.ACCENT_GOLD));
        
        JPanel profileText = new JPanel();
        profileText.setLayout(new BoxLayout(profileText, BoxLayout.Y_AXIS));
        profileText.setOpaque(false);

        welcomeLabel = new JLabel("Welcome, Admin");
        welcomeLabel.setFont(ThemeManager.getFont(Font.BOLD, 14));
        welcomeLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel roleLabel = new JLabel("Administrator");
        roleLabel.setFont(ThemeManager.getSmallFont());
        roleLabel.setForeground(CineBookTheme.ACCENT_GOLD);

        profileText.add(welcomeLabel);
        profileText.add(roleLabel);

        profileBox.add(avatarIcon);
        profileBox.add(profileText);
        headerPanel.add(profileBox, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Left Sidebar Navigation (#050B16 @ 92% opacity)
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(5, 11, 22, 235));
        sidebarPanel.setPreferredSize(new Dimension(230, 0));
        sidebarPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, CineBookTheme.BORDER_COLOR),
                new EmptyBorder(12, 0, 12, 0)
        ));

        // Navigation Sidebar Buttons with Ikonli Vector Icons
        btnDashboard = createSidebarButton("Dashboard Overview", "Dashboard", FontAwesomeSolid.TACHOMETER_ALT);
        btnMovies    = createSidebarButton("Movie Management", "Movies", FontAwesomeSolid.FILM);
        btnTheatres  = createSidebarButton("Theatre Management", "Theatres", FontAwesomeSolid.BUILDING);
        btnShows     = createSidebarButton("Show Management", "Shows", FontAwesomeSolid.CLOCK);
        btnSeats     = createSidebarButton("Seat Management", "Seats", FontAwesomeSolid.CHAIR);
        btnUpcoming  = createSidebarButton("Upcoming Movies", "Upcoming", FontAwesomeSolid.CALENDAR_ALT);
        btnAnalytics = createSidebarButton("Analytics Dashboard", "Analytics", FontAwesomeSolid.CHART_PIE);
        btnBookings  = createSidebarButton("Booking Management", "Bookings", FontAwesomeSolid.TICKET_ALT);
        btnUsers     = createSidebarButton("User Management", "Users", FontAwesomeSolid.USERS);
        btnReports   = createSidebarButton("Reports & Analytics", "Reports", FontAwesomeSolid.CHART_BAR);
        btnVerifyTicketSidebar = createSidebarButton("Verify Ticket", "Verify Ticket", FontAwesomeSolid.QRCODE);
        btnReviews   = createSidebarButton("Review Moderation", "Reviews", FontAwesomeSolid.STAR);
        btnCinePoints = createSidebarButton("CinePoints Rewards", "CinePoints", FontAwesomeSolid.GIFT);
        btnCoupons   = createSidebarButton("Coupon Management", "Coupons", FontAwesomeSolid.TICKET_ALT);
        JButton btnSecurity = createSidebarButton("Security & Audit Log", "Security", FontAwesomeSolid.SHIELD_ALT);
        btnLogout    = createSidebarButton("Logout", "Logout", FontAwesomeSolid.SIGN_OUT_ALT);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(btnDashboard);
        sidebarPanel.add(btnMovies);
        sidebarPanel.add(btnTheatres);
        sidebarPanel.add(btnShows);
        sidebarPanel.add(btnSeats);
        sidebarPanel.add(btnUpcoming);
        sidebarPanel.add(btnAnalytics);
        sidebarPanel.add(btnBookings);
        sidebarPanel.add(btnUsers);
        sidebarPanel.add(btnReports);
        sidebarPanel.add(btnVerifyTicketSidebar);
        sidebarPanel.add(btnReviews);
        sidebarPanel.add(btnCinePoints);
        sidebarPanel.add(btnCoupons);
        sidebarPanel.add(btnSecurity);
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(btnLogout);

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        add(sidebarPanel, BorderLayout.WEST);

        // 3. Main Content Area with PanelTransitionManager
        transitionManager = new PanelTransitionManager();

        movieManagementView = new MovieManagementView();
        theatreManagementView = new TheatreManagementView();
        theatreScreenManagementView = new TheatreScreenManagementView();
        showManagementView = new ShowManagementView();
        seatManagementView = new SeatManagementView();
        upcomingMoviesView = new UpcomingMoviesView();
        bookingManagementView = new BookingManagementView();
        userManagementView = new UserManagementView();
        reportManagementView = new ReportManagementView();
        verifyTicketView = new VerifyTicketView();
        analyticsView = new AdminAnalyticsView();
        adminReviewsView = new AdminReviewsView();
        adminCinePointsView = new AdminCinePointsView();
        adminCouponManagementView = new AdminCouponManagementView();

        auditLogView = new AuditLogView();

        transitionManager.addPanel(createDashboardCard(), "Dashboard");
        transitionManager.addPanel(movieManagementView, "Movies");
        transitionManager.addPanel(theatreManagementView, "Theatres");
        transitionManager.addPanel(theatreScreenManagementView, "MultiTheatre");
        transitionManager.addPanel(showManagementView, "Shows");
        transitionManager.addPanel(seatManagementView, "Seats");
        transitionManager.addPanel(upcomingMoviesView, "Upcoming");
        transitionManager.addPanel(analyticsView, "Analytics");
        transitionManager.addPanel(bookingManagementView, "Bookings");
        transitionManager.addPanel(userManagementView, "Users");
        transitionManager.addPanel(reportManagementView, "Reports");
        transitionManager.addPanel(verifyTicketView, "Verify Ticket");
        transitionManager.addPanel(adminReviewsView, "Reviews");
        transitionManager.addPanel(adminCinePointsView, "CinePoints");
        transitionManager.addPanel(adminCouponManagementView, "Coupons");
        transitionManager.addPanel(auditLogView, "Security");

        add(transitionManager, BorderLayout.CENTER);

        setActiveSidebarButton("Dashboard");
    }

    private JButton createSidebarButton(String text, String key, Ikon iconCode) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(230, 44));
        button.setFont(ThemeManager.getLabelFont());
        button.setForeground(CineBookTheme.TEXT_MUTED);
        button.setBackground(CineBookTheme.BG_SECONDARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 20));
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
                    button.setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 17)); // 3px right shift
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
                    button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 20));
                    if (button.getIcon() instanceof FontIcon) {
                        ((FontIcon) button.getIcon()).setIconColor(CineBookTheme.TEXT_MUTED);
                    }
                }
            }
        });

        sidebarButtons.put(key, button);
        return button;
    }

    private String activeCardKey = "Dashboard";

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
                btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 20));
                if (btn.getIcon() instanceof FontIcon) {
                    ((FontIcon) btn.getIcon()).setIconColor(CineBookTheme.TEXT_MUTED);
                }
            }
        }
    }

    private JPanel createDashboardCard() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(CineBookTheme.BG_PRIMARY);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CineBookTheme.BG_PRIMARY);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setBackground(CineBookTheme.BG_PRIMARY);

        JLabel headerLabel = new JLabel("Dashboard Overview");
        headerLabel.setFont(ThemeManager.getPageTitleFont());
        headerLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subheader = new JLabel("Welcome back! Here's what's happening today across CineBook cinemas.");
        subheader.setFont(ThemeManager.getBodyFont());
        subheader.setForeground(CineBookTheme.TEXT_MUTED);

        titleBox.add(headerLabel);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(subheader);
        topPanel.add(titleBox, BorderLayout.WEST);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topActions.setOpaque(false);

        btnVerifyTicket = new RoundedButton("Verify Ticket", FontAwesomeSolid.QRCODE, CineBookTheme.SUCCESS_COLOR, new Color(34, 197, 94, 220));
        btnVerifyTicket.setPreferredSize(new Dimension(150, 38));
        btnVerifyTicket.addActionListener(e -> new com.movieticket.view.components.TicketVerificationDialog(this).setVisible(true));

        btnRefresh = new RoundedButton("Refresh Metrics", FontAwesomeSolid.SYNC_ALT, CineBookTheme.ACCENT_PURPLE, CineBookTheme.ACCENT_PURPLE_HOVER);
        btnRefresh.setPreferredSize(new Dimension(160, 38));

        topActions.add(btnVerifyTicket);
        topActions.add(btnRefresh);
        topPanel.add(topActions, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center Content Scrollable Panel
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setBackground(CineBookTheme.BG_PRIMARY);

        // 1. Primary 4 Statistics Cards Row
        JPanel primaryStatsGrid = new JPanel(new GridLayout(1, 4, 16, 16));
        primaryStatsGrid.setBackground(CineBookTheme.BG_PRIMARY);
        primaryStatsGrid.setMaximumSize(new Dimension(2000, 110));

        lblTotalMovies   = createStatCard(primaryStatsGrid, "TOTAL MOVIES", CineBookTheme.ACCENT_PURPLE, FontAwesomeSolid.FILM);
        lblTotalTheatres = createStatCard(primaryStatsGrid, "TOTAL THEATRES", CineBookTheme.ACCENT_BLUE, FontAwesomeSolid.BUILDING);
        lblTotalShows    = createStatCard(primaryStatsGrid, "TOTAL SHOWS", CineBookTheme.ACCENT_GOLD, FontAwesomeSolid.CLOCK);
        lblTotalBookings  = createStatCard(primaryStatsGrid, "TOTAL BOOKINGS", CineBookTheme.SUCCESS_COLOR, FontAwesomeSolid.TICKET_ALT);

        centerContent.add(primaryStatsGrid);
        centerContent.add(Box.createVerticalStrut(16));

        // 2. Secondary Metrics Row
        JPanel secondaryStatsGrid = new JPanel(new GridLayout(1, 6, 12, 12));
        secondaryStatsGrid.setBackground(CineBookTheme.BG_PRIMARY);
        secondaryStatsGrid.setMaximumSize(new Dimension(2000, 95));

        lblTotalUsers     = createStatCard(secondaryStatsGrid, "USERS", CineBookTheme.TEXT_MUTED, FontAwesomeSolid.USERS);
        lblTotalScreens   = createStatCard(secondaryStatsGrid, "SCREENS", CineBookTheme.ACCENT_SKY, FontAwesomeSolid.DESKTOP);
        lblTotalSeats     = createStatCard(secondaryStatsGrid, "SEATS", CineBookTheme.ACCENT_INDIGO, FontAwesomeSolid.CHAIR);
        lblTotalTickets   = createStatCard(secondaryStatsGrid, "TICKETS", CineBookTheme.ACCENT_INFO, FontAwesomeSolid.TICKET_ALT);
        lblTotalConfirmed = createStatCard(secondaryStatsGrid, "CONFIRMED", CineBookTheme.SUCCESS_COLOR, FontAwesomeSolid.CHECK_CIRCLE);
        lblTotalRevenue   = createStatCard(secondaryStatsGrid, "REVENUE", CineBookTheme.SUCCESS_COLOR, FontAwesomeSolid.MONEY_BILL_WAVE);

        centerContent.add(secondaryStatsGrid);
        centerContent.add(Box.createVerticalStrut(20));

        // 3. Bookings Overview Area Chart Panel (Matching Reference Image)
        RoundedPanel chartCard = new RoundedPanel(new BorderLayout(10, 10), 12, CineBookTheme.BG_CARD);
        chartCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        chartCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);
        chartCard.setMaximumSize(new Dimension(2000, 240));

        JPanel chartHeader = new JPanel(new BorderLayout());
        chartHeader.setOpaque(false);

        JLabel chartTitle = new JLabel("Bookings Overview (This Week)");
        chartTitle.setFont(ThemeManager.getSectionHeaderFont());
        chartTitle.setForeground(CineBookTheme.TEXT_PRIMARY);

        JComboBox<String> timeFilter = new JComboBox<>(new String[]{"This Week", "This Month", "This Year"});
        ThemeManager.styleComboBox(timeFilter);
        timeFilter.setPreferredSize(new Dimension(120, 30));

        chartHeader.add(chartTitle, BorderLayout.WEST);
        chartHeader.add(timeFilter, BorderLayout.EAST);

        com.movieticket.view.components.BookingsOverviewChart chartComponent = new com.movieticket.view.components.BookingsOverviewChart();
        chartCard.add(chartHeader, BorderLayout.NORTH);
        chartCard.add(chartComponent, BorderLayout.CENTER);

        centerContent.add(chartCard);
        centerContent.add(Box.createVerticalStrut(16));

        // 3. Seat Demand Heatmap Component
        RoundedPanel heatmapCard = new RoundedPanel(new BorderLayout(), 12, CineBookTheme.BG_CARD);
        heatmapCard.setTopAccent(CineBookTheme.ACCENT_CYAN, 3);
        heatmapCard.setMaximumSize(new Dimension(2000, 360));
        com.movieticket.view.components.SeatDemandHeatmapView heatmapComponent = new com.movieticket.view.components.SeatDemandHeatmapView();
        heatmapCard.add(heatmapComponent, BorderLayout.CENTER);

        centerContent.add(heatmapCard);
        centerContent.add(Box.createVerticalStrut(20));

        // 3. Bottom Section: Top Movies Ranking & Quick Actions (Two Columns)
        JPanel bottomGrid = new JPanel(new GridLayout(1, 2, 20, 20));
        bottomGrid.setBackground(CineBookTheme.BG_PRIMARY);

        // LEFT COLUMN: Top Movies Performance (Pure Typographic Ranking - NO POSTERS)
        RoundedPanel topMoviesPanel = new RoundedPanel(new BorderLayout(10, 10), 12, CineBookTheme.BG_CARD);
        topMoviesPanel.setBorder(new EmptyBorder(16, 18, 16, 18));
        topMoviesPanel.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);

        JLabel rankingTitle = new JLabel("Top Performing Movies");
        rankingTitle.setFont(ThemeManager.getSectionHeaderFont());
        rankingTitle.setForeground(CineBookTheme.TEXT_PRIMARY);

        JPanel rankingList = new JPanel(new GridLayout(3, 1, 10, 10));
        rankingList.setOpaque(false);

        rankingList.add(createRankItem("01", "Inception", "Sci-Fi • Thriller", "85 Bookings", 85, CineBookTheme.ACCENT_PURPLE));
        rankingList.add(createRankItem("02", "Interstellar", "Sci-Fi • Adventure", "72 Bookings", 72, CineBookTheme.ACCENT_BLUE));
        rankingList.add(createRankItem("03", "Dangal", "Biography • Drama", "65 Bookings", 65, CineBookTheme.ACCENT_GOLD));

        topMoviesPanel.add(rankingTitle, BorderLayout.NORTH);
        topMoviesPanel.add(rankingList, BorderLayout.CENTER);

        // RIGHT COLUMN: Quick Actions Panel
        RoundedPanel quickActionsPanel = new RoundedPanel(new BorderLayout(10, 10), 12, CineBookTheme.BG_CARD);
        quickActionsPanel.setBorder(new EmptyBorder(16, 18, 16, 18));
        quickActionsPanel.setTopAccent(CineBookTheme.ACCENT_BLUE, 3);

        JLabel actionsTitle = new JLabel("Quick Management Actions");
        actionsTitle.setFont(ThemeManager.getSectionHeaderFont());
        actionsTitle.setForeground(CineBookTheme.TEXT_PRIMARY);

        JPanel actionsGrid = new JPanel(new GridLayout(2, 2, 12, 12));
        actionsGrid.setOpaque(false);

        quickAddMovieBtn = new RoundedButton("+ Add Movie", FontAwesomeSolid.FILM, CineBookTheme.ACCENT_PURPLE, CineBookTheme.ACCENT_PURPLE_HOVER);
        quickAddShowBtn  = new RoundedButton("+ Add Show", FontAwesomeSolid.CLOCK, CineBookTheme.ACCENT_BLUE, CineBookTheme.ACCENT_BLUE.darker());
        quickViewBookingsBtn = new RoundedButton("View Bookings", FontAwesomeSolid.TICKET_ALT, CineBookTheme.BG_CARD_HOVER, CineBookTheme.BORDER_LIGHT);
        quickViewReportsBtn  = new RoundedButton("View Reports", FontAwesomeSolid.CHART_BAR, CineBookTheme.BG_CARD_HOVER, CineBookTheme.BORDER_LIGHT);

        actionsGrid.add(quickAddMovieBtn);
        actionsGrid.add(quickAddShowBtn);
        actionsGrid.add(quickViewBookingsBtn);
        actionsGrid.add(quickViewReportsBtn);

        quickActionsPanel.add(actionsTitle, BorderLayout.NORTH);
        quickActionsPanel.add(actionsGrid, BorderLayout.CENTER);

        quickAddMovieBtn.addActionListener(e -> showCard("Movies"));
        quickAddShowBtn.addActionListener(e -> showCard("Shows"));
        btnShows.addActionListener(e -> showCard("Shows"));
        btnSeats.addActionListener(e -> showCard("Seats"));
        btnUpcoming.addActionListener(e -> showCard("Upcoming"));
        btnBookings.addActionListener(e -> showCard("Bookings"));
        btnUsers.addActionListener(e -> showCard("Users"));
        quickViewBookingsBtn.addActionListener(e -> showCard("Bookings"));
        quickViewReportsBtn.addActionListener(e -> showCard("Reports"));

        bottomGrid.add(topMoviesPanel);
        bottomGrid.add(quickActionsPanel);

        centerContent.add(bottomGrid);
        centerContent.add(Box.createVerticalStrut(20));

        // 4. Upcoming Movies Discovery Summary Card
        RoundedPanel upcomingCard = new RoundedPanel(new BorderLayout(10, 10), 12, CineBookTheme.BG_CARD);
        upcomingCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        upcomingCard.setTopAccent(CineBookTheme.ACCENT_CYAN, 3);
        upcomingCard.setMaximumSize(new Dimension(2000, 110));

        JPanel upcomingHeader = new JPanel(new BorderLayout());
        upcomingHeader.setOpaque(false);
        JLabel upcomingTitle = new JLabel("Movie Discovery & Monitoring");
        upcomingTitle.setFont(ThemeManager.getSectionHeaderFont());
        upcomingTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        
        JLabel attribution = new JLabel(" Powered by TMDB");
        attribution.setFont(ThemeManager.getSmallFont());
        attribution.setForeground(CineBookTheme.ACCENT_CYAN);
        
        JPanel attributionBox = new JPanel();
        attributionBox.setLayout(new BoxLayout(attributionBox, BoxLayout.Y_AXIS));
        attributionBox.setOpaque(false);
        attributionBox.add(upcomingTitle);
        attributionBox.add(attribution);

        btnViewUpcomingMoviesDashboard = new RoundedButton("View Upcoming Movies", FontAwesomeSolid.SEARCH, CineBookTheme.ACCENT_CYAN, CineBookTheme.ACCENT_CYAN.darker());
        btnViewUpcomingMoviesDashboard.addActionListener(e -> showCard("Upcoming"));
        
        btnSyncTmdb = new RoundedButton("Sync TMDB", FontAwesomeSolid.SYNC, CineBookTheme.ACCENT_PURPLE, CineBookTheme.ACCENT_PURPLE_HOVER);
        
        JPanel upcomingActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        upcomingActions.setOpaque(false);
        upcomingActions.add(btnSyncTmdb);
        upcomingActions.add(btnViewUpcomingMoviesDashboard);
        
        upcomingHeader.add(attributionBox, BorderLayout.WEST);
        upcomingHeader.add(upcomingActions, BorderLayout.EAST);

        JPanel upcomingStats = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 0));
        upcomingStats.setOpaque(false);
        
        lblNewUpcoming = new JLabel("New: 0");
        lblNewUpcoming.setFont(ThemeManager.getHeadingFont(Font.BOLD, 16));
        lblNewUpcoming.setForeground(CineBookTheme.SUCCESS_COLOR);
        
        lblTotalUpcoming = new JLabel("Upcoming: 0");
        lblTotalUpcoming.setFont(ThemeManager.getHeadingFont(Font.BOLD, 16));
        lblTotalUpcoming.setForeground(CineBookTheme.ACCENT_CYAN);
        
        lblTotalRecent = new JLabel("Recent: 0");
        lblTotalRecent.setFont(ThemeManager.getHeadingFont(Font.BOLD, 16));
        lblTotalRecent.setForeground(CineBookTheme.TEXT_PRIMARY);

        upcomingStats.add(lblNewUpcoming);
        upcomingStats.add(lblTotalUpcoming);
        upcomingStats.add(lblTotalRecent);

        upcomingCard.add(upcomingHeader, BorderLayout.NORTH);
        upcomingCard.add(upcomingStats, BorderLayout.CENTER);
        
        centerContent.add(upcomingCard);

        mainPanel.add(centerContent, BorderLayout.CENTER);
        return mainPanel;
    }

    private AnimatedCounterLabel createStatCard(JPanel parent, String title, Color accentColor, Ikon iconCode) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(6, 6), 12, CineBookTheme.BG_CARD);
        card.setTopAccent(accentColor, 4);
        card.setHoverElevationEnabled(true);
        card.setBorder(new EmptyBorder(14, 10, 14, 10));

        JPanel cardHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        cardHeader.setOpaque(false);

        if (iconCode != null) {
            JLabel iconLabel = new JLabel(FontIcon.of(iconCode, 16, accentColor));
            cardHeader.add(iconLabel);
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeManager.getFont(Font.BOLD, 11));
        titleLabel.setForeground(CineBookTheme.TEXT_MUTED);
        cardHeader.add(titleLabel);

        AnimatedCounterLabel valueLabel = new AnimatedCounterLabel("0");
        valueLabel.setFont(ThemeManager.getFont(Font.BOLD, 24));
        valueLabel.setForeground(accentColor);

        card.add(cardHeader, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        parent.add(card);
        return valueLabel;
    }

    private JPanel createRankItem(String rankNum, String movieTitle, String metaText, String bookingsText, int percent, Color barColor) {
        JPanel item = new JPanel(new BorderLayout(10, 4));
        item.setOpaque(false);

        JLabel rankLabel = new JLabel(rankNum);
        rankLabel.setFont(ThemeManager.getFont(Font.BOLD, 16));
        rankLabel.setForeground(barColor);

        JPanel titleMetaBox = new JPanel();
        titleMetaBox.setLayout(new BoxLayout(titleMetaBox, BoxLayout.Y_AXIS));
        titleMetaBox.setOpaque(false);

        JLabel nameLbl = new JLabel(movieTitle);
        nameLbl.setFont(ThemeManager.getFont(Font.BOLD, 14));
        nameLbl.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel metaLbl = new JLabel(metaText);
        metaLbl.setFont(ThemeManager.getSmallFont());
        metaLbl.setForeground(CineBookTheme.TEXT_MUTED);

        titleMetaBox.add(nameLbl);
        titleMetaBox.add(metaLbl);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setForeground(barColor);
        progressBar.setBackground(CineBookTheme.BG_PRIMARY);
        progressBar.setPreferredSize(new Dimension(100, 8));
        progressBar.setBorderPainted(false);

        // Animate chart reveal from 0% to percent
        com.movieticket.util.AnimationUtils.animate(com.movieticket.util.AnimationUtils.SLOW, (rawProgress, easedProgress) -> {
            int currentVal = (int) (percent * easedProgress);
            progressBar.setValue(currentVal);
        }, null);

        JLabel countLbl = new JLabel(bookingsText);
        countLbl.setFont(ThemeManager.getFont(Font.BOLD, 12));
        countLbl.setForeground(CineBookTheme.TEXT_PRIMARY);

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBox.setOpaque(false);
        rightBox.add(progressBar);
        rightBox.add(countLbl);

        item.add(rankLabel, BorderLayout.WEST);
        item.add(titleMetaBox, BorderLayout.CENTER);
        item.add(rightBox, BorderLayout.EAST);

        return item;
    }

    public void showCard(String cardName) {
        setActiveSidebarButton(cardName);
        transitionManager.showCardAnimated(cardName);
    }

    public void setWelcomeMessage(String name) {
        welcomeLabel.setText("Welcome, " + name);
    }

    // Setters for Statistics Values using AnimatedCounterLabel count-up
    public void setTotalUsers(int count) { if (lblTotalUsers != null) lblTotalUsers.setValueAnimated(count); }
    public void setTotalMovies(int count) { if (lblTotalMovies != null) lblTotalMovies.setValueAnimated(count); }
    public void setTotalTheatres(int count) { if (lblTotalTheatres != null) lblTotalTheatres.setValueAnimated(count); }
    public void setTotalScreens(int count) { if (lblTotalScreens != null) lblTotalScreens.setValueAnimated(count); }
    public void setTotalShows(int count) { if (lblTotalShows != null) lblTotalShows.setValueAnimated(count); }
    public void setTotalSeats(int count) { if (lblTotalSeats != null) lblTotalSeats.setValueAnimated(count); }
    public void setTotalBookings(int count) { if (lblTotalBookings != null) lblTotalBookings.setValueAnimated(count); }
    public void setTotalTickets(int count) { if (lblTotalTickets != null) lblTotalTickets.setValueAnimated(count); }
    public void setTotalConfirmed(int count) { if (lblTotalConfirmed != null) lblTotalConfirmed.setValueAnimated(count); }
    public void setTotalRevenue(BigDecimal revenue) { if (lblTotalRevenue != null) lblTotalRevenue.setCurrencyAnimated(revenue); }

    public void setViewUpcomingMoviesAction(ActionListener listener) { btnViewUpcomingMoviesDashboard.addActionListener(listener); }
    public void setVerifyTicketSidebarAction(ActionListener listener) { btnVerifyTicketSidebar.addActionListener(listener); }
    public void setSyncTmdbAction(ActionListener listener) { btnSyncTmdb.addActionListener(listener); }

    // View Getters
    public JButton getBtnReports() { return btnReports; }
    public JButton getBtnVerifyTicketSidebar() { return btnVerifyTicketSidebar; }
    public JButton getBtnReviews() { return btnReviews; }
    public JButton getBtnCoupons() { return btnCoupons; }
    public JButton getBtnLogout() { return btnLogout; }

    public MovieManagementView getMovieManagementView() { return movieManagementView; }
    public TheatreManagementView getTheatreManagementView() { return theatreManagementView; }
    public TheatreScreenManagementView getTheatreScreenManagementView() { return theatreScreenManagementView; }
    public ShowManagementView getShowManagementView() { return showManagementView; }
    public SeatManagementView getSeatManagementView() { return seatManagementView; }
    public UpcomingMoviesView getUpcomingMoviesView() { return upcomingMoviesView; }
    public BookingManagementView getBookingManagementView() { return bookingManagementView; }
    public UserManagementView getUserManagementView() { return userManagementView; }
    public ReportManagementView getReportManagementView() { return reportManagementView; }
    public VerifyTicketView getVerifyTicketView() { return verifyTicketView; }
    public AdminAnalyticsView getAnalyticsView() { return analyticsView; }
    public AdminReviewsView getAdminReviewsView() { return adminReviewsView; }
    public AdminCinePointsView getAdminCinePointsView() { return adminCinePointsView; }
    public AdminCouponManagementView getAdminCouponManagementView() { return adminCouponManagementView; }

    // Action Listeners
    public void setDashboardAction(ActionListener listener) { btnDashboard.addActionListener(listener); }
    public void setMoviesAction(ActionListener listener) { btnMovies.addActionListener(listener); }
    public void setTheatresAction(ActionListener listener) { btnTheatres.addActionListener(listener); }
    public void setAnalyticsAction(ActionListener listener) { btnAnalytics.addActionListener(listener); }
    public void setShowsAction(ActionListener listener) { btnShows.addActionListener(listener); }
    public void setSeatsAction(ActionListener listener) { btnSeats.addActionListener(listener); }
    public void setBookingsAction(ActionListener listener) { btnBookings.addActionListener(listener); }
    public void setUsersAction(ActionListener listener) { btnUsers.addActionListener(listener); }
    public void setReportsAction(ActionListener listener) { btnReports.addActionListener(listener); }
    public void setReviewsSidebarAction(ActionListener listener) { btnReviews.addActionListener(listener); }
    public void setCinePointsAction(ActionListener listener) { btnCinePoints.addActionListener(listener); }
    public void setCouponsAction(ActionListener listener) { btnCoupons.addActionListener(listener); }
    public void setLogoutAction(ActionListener listener) { btnLogout.addActionListener(listener); }
    public void setRefreshAction(ActionListener listener) { btnRefresh.addActionListener(listener); }
}
