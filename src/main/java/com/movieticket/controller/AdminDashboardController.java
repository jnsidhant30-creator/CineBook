package com.movieticket.controller;

import com.movieticket.dao.*;
import com.movieticket.model.Booking;
import com.movieticket.model.Screen;
import com.movieticket.model.Theatre;
import com.movieticket.model.User;
import com.movieticket.model.Movie;
import com.movieticket.model.Show;
import com.movieticket.util.MovieMonitoringService;
import com.movieticket.util.OmdbService;
import com.movieticket.util.ThemeManager;
import com.movieticket.util.UserSession;
import com.movieticket.util.discovery.SimulatedDiscoveryProvider;
import com.movieticket.view.AdminDashboardFrame;
import com.movieticket.view.AdminAnalyticsView;
import com.movieticket.view.TheatreScreenManagementView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for Admin Dashboard — Handles statistics loading, sidebar routing,
 * and user/booking management event bindings.
 */
public class AdminDashboardController {
    
    private final AdminDashboardFrame view;
    private final MovieDAO movieDAO;
    private final TheatreDAO theatreDAO;
    private final ShowDAO showDAO;
    private final SeatDAO seatDAO;
    private final BookingDAO bookingDAO;
    private final UserDAO userDAO;
    private final ReportDAO reportDAO;
    private final UpcomingMovieDAO upcomingMovieDAO;
    private final NotificationDAO notificationDAO;
    private final AnalyticsDAO analyticsDAO;
    private final com.movieticket.util.PaymentService paymentService;
    private final ScreenDAO screenDAO;
    private final com.movieticket.dao.CinePointsDAO cinePointsDAO;

    // Integrated Sub-Controllers
    private MovieController movieController;
    private TheatreController theatreController;
    private ShowController showController;
    private SeatController seatController;
    private ReportController reportController;
    private UpcomingMoviesController upcomingMoviesController;
    private MovieMonitoringService movieMonitoringService;
    private TheatreScreenController theatreScreenController; // Phase 5
    private AdminReviewController adminReviewController; // Phase 7
    private AdminCouponController adminCouponController;

    public AdminDashboardController(AdminDashboardFrame view) {
        this.view = view;
        
        // 1. Authorization Verification
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            ThemeManager.showError(null, 
                "Access denied. Administrator privileges required.", 
                "Access Denied");
            if (view != null) {
                view.dispose();
            }
            throw new SecurityException("Unauthorized access attempt to Admin Dashboard.");
        }

        // 2. Initialize DAO Layer
        this.movieDAO = new MovieDAO();
        this.theatreDAO = new TheatreDAO();
        this.showDAO = new ShowDAO();
        this.seatDAO = new SeatDAO();
        this.bookingDAO = new BookingDAO();
        this.userDAO = new UserDAO();
        this.reportDAO = new ReportDAO();
        this.upcomingMovieDAO = new UpcomingMovieDAO();
        this.notificationDAO = new NotificationDAO();
        this.analyticsDAO = new AnalyticsDAO();
        this.paymentService = new com.movieticket.util.PaymentService();
        this.screenDAO = new ScreenDAO();
        this.cinePointsDAO = new com.movieticket.dao.CinePointsDAO();
        
        // 3. Sub-controllers
        this.movieController = new MovieController(this.movieDAO);
        if (view.getMovieManagementView() != null) {
            movieController.bindAdminPanel(view.getMovieManagementView());
            movieController.setOnMovieDataChanged(this::loadDashboardStatistics);
        }

        this.theatreController = new TheatreController(this.theatreDAO);
        if (view.getTheatreManagementView() != null) {
            theatreController.bindView(view.getTheatreManagementView());
            theatreController.setOnTheatreDataChanged(this::loadDashboardStatistics);
        }

        // Phase 5: TheatreScreenController
        this.theatreScreenController = new TheatreScreenController(theatreDAO, screenDAO);
        
        // Phase 7
        this.adminReviewController = new AdminReviewController(view);
        
        // Coupon Management
        if (view.getAdminCouponManagementView() != null) {
            this.adminCouponController = new AdminCouponController(view.getAdminCouponManagementView(), new com.movieticket.dao.CouponDAO());
        }

        if (view.getTheatreScreenManagementView() != null) {
            theatreScreenController.bindView(view.getTheatreScreenManagementView());
            theatreScreenController.setOnDataChanged(this::loadDashboardStatistics);
        }

        this.showController = new ShowController(this.showDAO, this.movieDAO, this.theatreDAO);
        if (view.getShowManagementView() != null) {
            showController.bindView(view.getShowManagementView());
            showController.setOnShowDataChanged(this::loadDashboardStatistics);
        }

        this.seatController = new SeatController(this.seatDAO, this.theatreDAO);
        if (view.getSeatManagementView() != null) {
            seatController.bindView(view.getSeatManagementView());
            seatController.setOnSeatDataChanged(this::loadDashboardStatistics);
        }

        this.reportController = new ReportController(this.reportDAO);
        if (view.getReportManagementView() != null) {
            reportController.bindView(view.getReportManagementView());
            view.getReportManagementView().getBtnBack().addActionListener(e -> {
                loadDashboardStatistics();
                view.showCard("Dashboard");
            });
        }
        
        this.movieMonitoringService = new MovieMonitoringService(
            new SimulatedDiscoveryProvider(),
            upcomingMovieDAO,
            movieDAO,
            userDAO,
            notificationDAO
        );
        
        if (view.getUpcomingMoviesView() != null) {
            this.upcomingMoviesController = new UpcomingMoviesController(
                view.getUpcomingMoviesView(),
                view,
                movieDAO,
                upcomingMovieDAO,
                movieMonitoringService,
                new OmdbService(),
                view.getMovieManagementView()
            );
        }

        initController();
        initUserAndBookingManagement();
    }
    
    private void initController() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUsername() != null) {
            view.setWelcomeMessage(currentUser.getUsername());
        }
        
        view.setDashboardAction(e -> {
            loadDashboardStatistics();
            view.showCard("Dashboard");
        });
        view.setMoviesAction(e -> {
            if (movieController != null) movieController.refreshAdminTable();
            view.showCard("Movies");
        });
        view.setTheatresAction(e -> {
            if (theatreController != null) theatreController.refreshTable();
            view.showCard("Theatres");
        });
        view.setShowsAction(e -> {
            if (showController != null) {
                showController.reloadDropdowns();
                showController.refreshTable();
            }
            view.showCard("Shows");
        });
        view.setSeatsAction(e -> {
            if (seatController != null) {
                seatController.reloadTheatres();
                seatController.refreshTable();
            }
            view.showCard("Seats");
        });
        view.setBookingsAction(e -> {
            refreshBookingsTable();
            view.showCard("Bookings");
        });
        view.setUsersAction(e -> {
            refreshUsersTable();
            view.showCard("Users");
        });
        view.setReportsAction(e -> {
            if (reportController != null) reportController.loadCurrentReport();
            view.showCard("Reports");
        });
        view.setVerifyTicketSidebarAction(e -> {
            if (view.getVerifyTicketView() != null) view.getVerifyTicketView().clearResult();
            view.showCard("Verify Ticket");
        });
        
        view.setRefreshAction(e -> loadDashboardStatistics());
        view.setLogoutAction(e -> handleLogout());
        
        // Phase 6: Analytics action
        view.setAnalyticsAction(e -> {
            view.showCard("Analytics");
            loadAnalytics();
        });
        
        view.setCinePointsAction(e -> {
            view.showCard("CinePoints");
            loadAdminCinePointsStats();
        });
        
        view.setCouponsAction(e -> {
            view.showCard("Coupons");
        });
        
        view.setSyncTmdbAction(e -> {
            int confirm = JOptionPane.showConfirmDialog(view, "Start background TMDB synchronization?", "Confirm TMDB Sync", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                view.getLblNewUpcoming().setText("TMDB Sync: Starting...");
                new SwingWorker<Void, String>() {
                    @Override
                    protected Void doInBackground() {
                        java.util.List<Movie> allMovies = movieDAO.getAllMovies();
                        new com.movieticket.util.TmdbMovieService().syncLegacyMovies(
                            allMovies, 
                            msg -> publish(msg), 
                            () -> {}
                        );
                        return null;
                    }
                    @Override
                    protected void process(java.util.List<String> chunks) {
                        if (!chunks.isEmpty()) {
                            view.getLblNewUpcoming().setText(chunks.get(chunks.size() - 1));
                        }
                    }
                }.execute();
            }
        });
        
        initVerifyTicket();
        loadDashboardStatistics();
    }

    private void initUserAndBookingManagement() {
        // --- Booking Management Bindings ---
        if (view.getBookingManagementView() != null) {
            view.getBookingManagementView().getSearchButton().addActionListener(e -> {
                String searchStr = view.getBookingManagementView().getSearchField().getText().trim();
                if (searchStr.isEmpty()) {
                    refreshBookingsTable();
                    return;
                }
                try {
                    int bId = Integer.parseInt(searchStr);
                    Optional<Booking> optB = bookingDAO.getBookingById(bId);
                    DefaultTableModel model = view.getBookingManagementView().getTableModel();
                    model.setRowCount(0);
                    if (optB.isPresent()) {
                        Booking b = optB.get();
                        java.util.Optional<com.movieticket.model.Payment> optPay = paymentService.getPaymentByBookingId(b.getBookingId());
                        String payMethod = optPay.map(com.movieticket.model.Payment::getPaymentMethod).orElse("N/A");
                        String txnId = optPay.map(com.movieticket.model.Payment::getTransactionId).orElse("N/A");
                        model.addRow(new Object[]{
                                b.getBookingId(),
                                "User #" + b.getUserId(),
                                "Show #" + b.getShowId(),
                                "Theatre Hall",
                                "Seats",
                                "₹" + b.getTotalAmount().setScale(2).toString(),
                                b.getStatus(),
                                payMethod,
                                txnId
                        });
                    } else {
                        ThemeManager.showInfo(view, "No booking found with ID: " + bId, "Search Result");
                    }
                } catch (NumberFormatException nfe) {
                    ThemeManager.showWarning(view, "Please enter a valid numeric Booking ID.", "Invalid Search");
                }
            });

            view.getBookingManagementView().getCancelBookingButton().addActionListener(e -> {
                JTable table = view.getBookingManagementView().getBookingsTable();
                int row = table.getSelectedRow();
                if (row < 0) {
                    ThemeManager.showWarning(view, "Please select a booking to cancel.", "No Selection");
                    return;
                }
                int bookingId = (int) table.getValueAt(row, 0);
                String currentStatus = (String) table.getValueAt(row, 6);
                if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
                    ThemeManager.showInfo(view, "This booking is already cancelled.", "Notice");
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(view,
                        "Are you sure you want to cancel Booking #" + bookingId + "?",
                        "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = bookingDAO.updateBookingStatus(bookingId, "CANCELLED");
                    if (success) {
                        new com.movieticket.util.CinePointsService().cancelPendingPoints(bookingId);
                        ThemeManager.showInfo(view, "Booking #" + bookingId + " has been cancelled.", "Success");
                        refreshBookingsTable();
                        loadDashboardStatistics();
                    } else {
                        ThemeManager.showError(view, "Failed to cancel booking in database.", "Error");
                    }
                }
            });
        }

        // --- User Management Bindings ---
        if (view.getUserManagementView() != null) {
            JTable userTable = view.getUserManagementView().getUserTable();
            userTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = userTable.getSelectedRow();
                    if (row >= 0) {
                        view.getUserManagementView().getUserIdField().setText(String.valueOf(userTable.getValueAt(row, 0)));
                        view.getUserManagementView().getUsernameField().setText((String) userTable.getValueAt(row, 1));
                        view.getUserManagementView().getRoleCombo().setSelectedItem(userTable.getValueAt(row, 2));
                    }
                }
            });

            view.getUserManagementView().getClearButton().addActionListener(e -> clearUserForm());
            view.getUserManagementView().getRefreshButton().addActionListener(e -> {
                clearUserForm();
                refreshUsersTable();
            });

            view.getUserManagementView().getAddButton().addActionListener(e -> {
                String uname = view.getUserManagementView().getUsernameField().getText().trim();
                String role = (String) view.getUserManagementView().getRoleCombo().getSelectedItem();

                if (uname.isEmpty()) {
                    ThemeManager.showWarning(view, "Username is required.", "Validation Error");
                    return;
                }

                if (userDAO.findByUsername(uname).isPresent()) {
                    ThemeManager.showWarning(view, "Username '" + uname + "' is already taken.", "Duplicate Username");
                    return;
                }

                // Generate a secure temporary password using BCrypt
                String tempPassword = com.movieticket.util.PasswordUtil.hashPassword("CineBook@" + System.currentTimeMillis());
                User newUser = new User(0, uname, tempPassword, role);
                int id = userDAO.createUser(newUser);
                if (id > 0) {
                    com.movieticket.util.AuditService.log(
                        com.movieticket.util.AuditService.USER_CREATED, "USER", id,
                        "Admin created user '" + uname + "' with role '" + role + "'");
                    ThemeManager.showInfo(view, "User '" + uname + "' created.\nTemporary password: CineBook@[timestamp]\nUser must reset their password on first login.", "Success");
                    clearUserForm();
                    refreshUsersTable();
                    loadDashboardStatistics();
                } else {
                    ThemeManager.showError(view, "Failed to create user record.", "Database Error");
                }
            });

            view.getUserManagementView().getUpdateButton().addActionListener(e -> {
                String idStr = view.getUserManagementView().getUserIdField().getText().trim();
                if (idStr.isEmpty()) {
                    ThemeManager.showWarning(view, "Please select a user from the table to update.", "Selection Required");
                    return;
                }
                int uId = Integer.parseInt(idStr);
                String uname = view.getUserManagementView().getUsernameField().getText().trim();
                String newRole = (String) view.getUserManagementView().getRoleCombo().getSelectedItem();

                Optional<User> opt = userDAO.findById(uId);
                if (opt.isPresent()) {
                    User u = opt.get();
                    String oldRole = u.getRole();

                    // Last-admin protection: prevent removing the last ADMIN account
                    if ("ADMIN".equalsIgnoreCase(oldRole) && !"ADMIN".equalsIgnoreCase(newRole)) {
                        if (userDAO.countActiveAdmins() <= 1) {
                            ThemeManager.showWarning(view,
                                "Cannot change role: at least one active administrator must remain.",
                                "Last Admin Protection");
                            return;
                        }
                        int confirm = JOptionPane.showConfirmDialog(view,
                            "You are downgrading an ADMIN account. Continue?",
                            "Confirm Role Change", JOptionPane.YES_NO_OPTION);
                        if (confirm != JOptionPane.YES_OPTION) return;
                    }

                    u.setUsername(uname);
                    u.setRole(newRole);
                    if (userDAO.updateUser(u)) {
                        com.movieticket.util.AuditService.log(
                            com.movieticket.util.AuditService.ROLE_CHANGED, "USER", uId,
                            "Role changed from '" + oldRole + "' to '" + newRole + "' for user '" + uname + "'");
                        ThemeManager.showInfo(view, "User record updated successfully.", "Success");
                        clearUserForm();
                        refreshUsersTable();
                        loadDashboardStatistics();
                    } else {
                        ThemeManager.showError(view, "Failed to update user in database.", "Database Error");
                    }
                }
            });

            view.getUserManagementView().getDeleteButton().addActionListener(e -> {
                String idStr = view.getUserManagementView().getUserIdField().getText().trim();
                if (idStr.isEmpty()) {
                    ThemeManager.showWarning(view, "Please select a user from the table to delete.", "Selection Required");
                    return;
                }
                int uId = Integer.parseInt(idStr);
                String uname = view.getUserManagementView().getUsernameField().getText();

                // Last-admin protection
                Optional<User> opt = userDAO.findById(uId);
                if (opt.isPresent() && "ADMIN".equalsIgnoreCase(opt.get().getRole())) {
                    if (userDAO.countActiveAdmins() <= 1) {
                        ThemeManager.showWarning(view,
                            "Cannot delete the last administrator account.",
                            "Last Admin Protection");
                        return;
                    }
                }

                int confirm = JOptionPane.showConfirmDialog(view,
                        "Are you sure you want to delete user '" + uname + "' (ID: " + uId + ")?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (userDAO.deleteUser(uId)) {
                        com.movieticket.util.AuditService.log(
                            com.movieticket.util.AuditService.USER_BLOCKED, "USER", uId,
                            "Admin deleted user '" + uname + "'");
                        ThemeManager.showInfo(view, "User deleted successfully.", "Success");
                        clearUserForm();
                        refreshUsersTable();
                        loadDashboardStatistics();
                    } else {
                        ThemeManager.showError(view, "Failed to delete user. The user may have existing bookings.", "Error");
                    }
                }
            });
        }
    }

    private void clearUserForm() {
        if (view.getUserManagementView() != null) {
            view.getUserManagementView().getUserIdField().setText("");
            view.getUserManagementView().getUsernameField().setText("");
            view.getUserManagementView().getRoleCombo().setSelectedIndex(0);
            view.getUserManagementView().getUserTable().clearSelection();
        }
    }

    public void refreshBookingsTable() {
        if (view.getBookingManagementView() != null) {
            DefaultTableModel model = view.getBookingManagementView().getTableModel();
            model.setRowCount(0);
            List<Booking> bookings = bookingDAO.getAllBookings();
            for (Booking b : bookings) {
                java.util.Optional<com.movieticket.model.Payment> optPay = paymentService.getPaymentByBookingId(b.getBookingId());
                String payMethod = optPay.map(com.movieticket.model.Payment::getPaymentMethod).orElse("N/A");
                String txnId = optPay.map(com.movieticket.model.Payment::getTransactionId).orElse("N/A");
                model.addRow(new Object[]{
                        b.getBookingId(),
                        "User #" + b.getUserId(),
                        "Show #" + b.getShowId(),
                        "Main Cinema",
                        "Booked Seats",
                        "₹" + b.getTotalAmount().setScale(2).toString(),
                        b.getStatus(),
                        payMethod,
                        txnId
                });
            }
        }
    }

    public void refreshUsersTable() {
        if (view.getUserManagementView() != null) {
            DefaultTableModel model = view.getUserManagementView().getTableModel();
            model.setRowCount(0);
            List<User> users = userDAO.getAllUsers();
            for (User u : users) {
                model.addRow(new Object[]{
                        u.getUserId(),
                        u.getUsername(),
                        u.getRole()
                });
            }
        }
    }
    
    public void loadDashboardStatistics() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            private BigDecimal totalRevenue = BigDecimal.ZERO;

            @Override
            protected int[] doInBackground() throws Exception {
                int userCount = reportDAO.getTotalUsers();
                int movieCount = reportDAO.getTotalMovies();
                int theatreCount = reportDAO.getTotalTheatres();
                int screenCount = reportDAO.getTotalScreens();
                int showCount = reportDAO.getTotalShows();
                int seatCount = reportDAO.getTotalSeats();
                int bookingCount = reportDAO.getTotalBookings();
                int ticketsSold = reportDAO.getTotalTicketsSold();
                int confirmedCount = reportDAO.getTotalConfirmedBookings();
                totalRevenue = reportDAO.getTotalRevenue();

                return new int[]{
                    userCount, movieCount, theatreCount, screenCount, showCount,
                    seatCount, bookingCount, ticketsSold, confirmedCount
                };
            }

            @Override
            protected void done() {
                try {
                    int[] stats = get();
                    view.setTotalUsers(stats[0]);
                    view.setTotalMovies(stats[1]);
                    view.setTotalTheatres(stats[2]);
                    view.setTotalScreens(stats[3]);
                    view.setTotalShows(stats[4]);
                    view.setTotalSeats(stats[5]);
                    view.setTotalBookings(stats[6]);
                    view.setTotalTickets(stats[7]);
                    view.setTotalConfirmed(stats[8]);
                    view.setTotalRevenue(totalRevenue);
                } catch (Exception ex) {
                    System.err.println("[AdminDashboardController] Error loading dashboard statistics:");
                    ex.printStackTrace();
                    ThemeManager.showError(view,
                        "Unable to load dashboard statistics: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()) +
                        "\nPlease check system logs or database connection.",
                        "Dashboard Error");
                }
            }
        };
        worker.execute();
    }
    
    private void initVerifyTicket() {
        if (view.getVerifyTicketView() != null) {
            com.movieticket.view.VerifyTicketView vtView = view.getVerifyTicketView();
            vtView.getVerifyIdBtn().addActionListener(e -> {
                String inputId = vtView.getBookingIdField().getText().trim();
                verifyTicketStr(inputId, vtView);
            });
            vtView.getScanQrBtn().addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select QR Code Image");
                if (fileChooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
                    try {
                        String decoded = new com.movieticket.util.QrCodeService().decodeQrCodeImage(fileChooser.getSelectedFile());
                        if (decoded == null) {
                            vtView.showResult(false, "INVALID", "No QR Code found in the image.");
                            return;
                        }
                        String[] parts = decoded.split("\\|");
                        String extractedId = "";
                        for(String p : parts) {
                            if (p.startsWith("BOOKING_ID=")) {
                                extractedId = p.substring(11);
                                break;
                            }
                        }
                        vtView.getBookingIdField().setText(extractedId);
                        verifyTicketStr(extractedId, vtView);
                    } catch(Exception ex) {
                        vtView.showResult(false, "ERROR", "Failed to decode QR code: " + ex.getMessage());
                    }
                }
            });
        }
    }

    private void verifyTicketStr(String inputId, com.movieticket.view.VerifyTicketView vtView) {
        if (inputId.isEmpty()) return;
        int dbId = -1;
        if (inputId.startsWith("CINE-") || inputId.startsWith("BKG")) {
            String[] tokens = inputId.split("-");
            try {
                if (tokens.length == 3) {
                    dbId = Integer.parseInt(tokens[2]);
                } else if (inputId.startsWith("BKG")) {
                    dbId = Integer.parseInt(inputId.substring(3));
                }
            } catch(Exception ignored){}
        } else {
            try { dbId = Integer.parseInt(inputId); } catch(Exception ignored){}
        }

        if (dbId == -1) {
            vtView.showResult(false, "INVALID", "Invalid Booking ID format.");
            return;
        }

        Optional<Booking> optB = bookingDAO.getBookingById(dbId);
        if (optB.isEmpty()) {
            vtView.showResult(false, "NOT FOUND", "Booking record not found in system.");
            return;
        }

        Booking b = optB.get();
        Optional<Show> optShow = showDAO.getShowById(b.getShowId());
        String movieTitle = "Unknown Movie";
        String date = "—";
        String time = "—";
        if (optShow.isPresent()) {
            date = optShow.get().getShowDate() != null ? optShow.get().getShowDate().toString() : "—";
            time = optShow.get().getShowTime() != null ? optShow.get().getShowTime().toString() : "—";
            Optional<Movie> m = movieDAO.getMovieById(optShow.get().getMovieId());
            if (m.isPresent()) movieTitle = m.get().getTitle();
        }
        
        java.util.List<String> seats = bookingDAO.getSeatNumbersForBooking(b.getBookingId());
        String seatsStr = seats.isEmpty() ? "None" : String.join(", ", seats);

        String detailsHtml = String.format("<b>Movie:</b> %s<br><b>Date:</b> %s<br><b>Time:</b> %s<br><b>Seats:</b> %s<br><b>Amount:</b> Rs. %s",
                movieTitle, date, time, seatsStr, b.getTotalAmount().toString());

        if ("CANCELLED".equalsIgnoreCase(b.getStatus())) {
            vtView.showResult(false, "CANCELLED", detailsHtml);
        } else {
            vtView.showResult(true, "CONFIRMED", detailsHtml);
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(view, 
            "Are you sure you want to logout?", 
            "Confirm Logout", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            UserSession.getInstance().clearSession();
            view.dispose();
            
            AuthenticationController authController = new AuthenticationController(new UserDAO());
            authController.startApplication();
        }
    }

    private void loadAdminCinePointsStats() {
        if (view.getAdminCinePointsView() != null) {
            SwingWorker<int[], Void> worker = new SwingWorker<>() {
                @Override
                protected int[] doInBackground() {
                    int totalIssued = 0;
                    int totalRedeemed = 0;
                    String sqlIssued = "SELECT COALESCE(SUM(points), 0) FROM cinepoints_transactions WHERE transaction_type = 'EARNED'";
                    String sqlRedeemed = "SELECT COALESCE(SUM(points), 0) FROM cinepoints_transactions WHERE transaction_type = 'REDEEMED'";
                    try (java.sql.Connection conn = com.movieticket.util.DatabaseConnection.getConnection()) {
                        try (java.sql.Statement stmt = conn.createStatement();
                             java.sql.ResultSet rs = stmt.executeQuery(sqlIssued)) {
                            if (rs.next()) totalIssued = rs.getInt(1);
                        }
                        try (java.sql.Statement stmt = conn.createStatement();
                             java.sql.ResultSet rs = stmt.executeQuery(sqlRedeemed)) {
                            if (rs.next()) totalRedeemed = rs.getInt(1);
                        }
                    } catch (java.sql.SQLException e) {
                        e.printStackTrace();
                    }
                    return new int[]{totalIssued, totalRedeemed};
                }

                @Override
                protected void done() {
                    try {
                        int[] stats = get();
                        view.getAdminCinePointsView().setMetrics(stats[0], stats[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
    }

    /**
     * Loads analytics data asynchronously using SwingWorker to prevent UI freezes.
     */
    private void loadAnalytics() {
        AdminAnalyticsView analyticsView = view.getAnalyticsView();
        if (analyticsView == null) return;

        analyticsView.setStatus("Loading analytics data…");

        int days = 7;
        String range = (String) analyticsView.getDateRangeCombo().getSelectedItem();
        if (range != null) {
            if (range.contains("14")) days = 14;
            else if (range.contains("30")) days = 30;
            else if (range.contains("90")) days = 90;
        }
        final int finalDays = days;

        // Wire refresh button
        analyticsView.getBtnRefreshAnalytics().addActionListener(e -> loadAnalytics());

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private BigDecimal totalRevenue, todayRevenue, monthRevenue;
            private int totalBookings, todayBookings, ticketsSold;
            private Map<String, Integer> dailyCounts, topMovies, seatTypes;
            private Map<String, BigDecimal> theatreRevenue;
            private Map<String, Double> theatreOccupancy;
            private Map<String, Double> topRatedMovies;
            private java.util.List<String> topPreferredGenres;

            @Override
            protected Void doInBackground() {
                try {
                    totalRevenue  = analyticsDAO.getTotalRevenue();
                    todayRevenue  = analyticsDAO.getRevenueToday();
                    monthRevenue  = analyticsDAO.getRevenueThisMonth();
                    totalBookings = analyticsDAO.getTotalConfirmedBookings();
                    todayBookings = analyticsDAO.getBookingsToday();
                    ticketsSold   = analyticsDAO.getTotalTicketsSold();
                    dailyCounts   = analyticsDAO.getDailyBookingCounts(finalDays);
                    topMovies     = analyticsDAO.getTopMoviesByTickets(8);
                    theatreRevenue  = analyticsDAO.getTheatreRevenue();
                    theatreOccupancy = analyticsDAO.getTheatreOccupancy();
                    seatTypes     = analyticsDAO.getBookingsBySeatType();
                    topRatedMovies = analyticsDAO.getTopRatedMovies(8);
                    topPreferredGenres = new com.movieticket.dao.RecommendationDAO().getTopPreferredGenres();
                } catch (Exception e) {
                    System.err.println("[Analytics] Error loading: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                analyticsView.updateKpis(totalRevenue, todayRevenue, monthRevenue, totalBookings, todayBookings, ticketsSold);
                analyticsView.updateDailyChart(dailyCounts);
                analyticsView.updateTopMovies(topMovies);
                analyticsView.updateTheatreRevenue(theatreRevenue, theatreOccupancy);
                analyticsView.updateSeatTypeBreakdown(seatTypes);
                if (topRatedMovies != null) {
                    analyticsView.updateTopRatedMovies(topRatedMovies);
                }
                if (topPreferredGenres != null) {
                    analyticsView.updateTopGenres(topPreferredGenres);
                }
                String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));
                analyticsView.setLastUpdated("Last updated: " + now);
                analyticsView.setStatus("Analytics loaded successfully.");
            }
        };
        worker.execute();
    }
}
