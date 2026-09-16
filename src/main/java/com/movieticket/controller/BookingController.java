package com.movieticket.controller;

import com.movieticket.dao.BookingDAO;
import com.movieticket.dao.MovieDAO;
import com.movieticket.dao.SeatDAO;
import com.movieticket.dao.ShowDAO;
import com.movieticket.dao.TheatreDAO;
import com.movieticket.model.Booking;
import com.movieticket.model.Movie;
import com.movieticket.model.Seat;
import com.movieticket.model.Show;
import com.movieticket.model.Theatre;
import com.movieticket.model.User;
import com.movieticket.dao.UserDAO;
import com.movieticket.util.SeatHoldService;
import com.movieticket.util.UserSession;
import com.movieticket.view.*;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.DigitalTicketDialog;

import javax.swing.*;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Controller managing customer ticket booking and seat selection workflow (Phase 13).
 *
 * Responsibilities:
 * - Role-based authorization & active user session verification
 * - Dynamic seat loading for the selected show from MySQL via SeatDAO & BookingDAO
 * - Real-time seat selection, ticket count, and BigDecimal price calculation
 * - Double-booking concurrency prevention & database transaction management (commit/rollback)
 * - Booking confirmation display with unique booking ID
 * - Safe navigation and state resetting
 */
public class BookingController {

    // DAOs
    private final SeatDAO seatDAO;
    private final BookingDAO bookingDAO;
    private final ShowDAO showDAO;
    private final MovieDAO movieDAO;
    private final TheatreDAO theatreDAO;
    private final UserDAO userDAO;

    // View panels mapping
    private ShowSelectionView showSelectionView;
    private SeatSelectionView seatSelectionView;
    private BookingConfirmationView confirmationView;
    private PaymentSimulationView paymentView;
    private BookingHistoryView historyView;
    private BookingManagementView adminView;
    private SeatManagementView seatManagementView;
    private final NotificationController notificationController;
    private final ReviewController reviewController;
    private final com.movieticket.util.PaymentService paymentService;

    private CardLayout userDashboardCardLayout;
    private JPanel userDashboardContentPanel;

    // Active booking session context
    private Movie activeMovie;
    private Theatre activeTheatre;
    private Show activeShow;
    private List<Seat> currentTheatreSeats = new ArrayList<>();
    private Set<Integer> currentBookedSeatIds = new HashSet<>();
    private final List<Seat> activeSelectedSeats = new ArrayList<>();
    private BigDecimal activeTotalAmount = BigDecimal.ZERO;
    private static final int MAX_SEATS_PER_BOOKING = 10;

    // Seat hold integration
    private final SeatHoldService seatHoldService = new SeatHoldService();
    private javax.swing.Timer holdCountdownTimer;
    private javax.swing.Timer seatMapRefreshTimer;

    public BookingController() {
        this.bookingDAO = new BookingDAO();
        this.seatDAO = new SeatDAO();
        this.showDAO = new ShowDAO();
        this.movieDAO = new MovieDAO();
        this.theatreDAO = new TheatreDAO();
        this.userDAO = new UserDAO();
        this.notificationController = new NotificationController();
        this.reviewController = new ReviewController(new com.movieticket.dao.ReviewDAO(), bookingDAO, showDAO);
        this.paymentService = new com.movieticket.util.PaymentService();
        this.cinePointsService = new com.movieticket.util.CinePointsService();
        this.couponService = new com.movieticket.util.CouponService();
    }

    public BookingController(List<Movie> movieSource, List<Theatre> theatreSource, List<Show> showSource) {
        this();
    }

    /**
     * Connects all booking panels from the User Dashboard.
     */
    public void bindUserWorkflow(UserDashboardFrame dashboard) {
        this.showSelectionView = dashboard.getShowSelectionView();
        this.seatSelectionView = dashboard.getSeatSelectionView();
        this.confirmationView = dashboard.getBookingConfirmationView();
        this.paymentView = dashboard.getPaymentSimulationView();
        this.historyView = dashboard.getBookingHistoryView();
        this.userDashboardCardLayout = dashboard.getCardLayout();
        this.userDashboardContentPanel = dashboard.getMainContentPanel();

        // 1. Show selection actions (Managed by UserDashboardController)

        // 2. Seat selection actions
        seatSelectionView.getConfirmBookingButton().addActionListener(e -> proceedToConfirmation());
        seatSelectionView.getClearSelectionButton().addActionListener(e -> clearSeatSelection());
        seatSelectionView.getBackButton().addActionListener(e -> cancelBookingWorkflow());

        // 3. Confirmation view actions
        confirmationView.getConfirmButton().setText("Proceed to Payment");
        confirmationView.getConfirmButton().addActionListener(e -> proceedToPayment());
        confirmationView.getBackToSeatsButton().addActionListener(e -> {
            userDashboardCardLayout.show(userDashboardContentPanel, "Select Seats");
            startSeatMapPolling(); // Resume polling when going back
        });
        confirmationView.getCancelButton().addActionListener(e -> cancelBookingWorkflow());

        // Payment view actions
        if (paymentView != null) {
            paymentView.getPayNowButton().addActionListener(e -> processPayment());
            paymentView.getCancelButton().addActionListener(e -> cancelPayment());
            paymentView.getUseCinePointsCheckbox().addActionListener(e -> handlePointsToggle());
            paymentView.getApplyCouponButton().addActionListener(e -> handleCouponApply());
            paymentView.getRemoveCouponButton().addActionListener(e -> handleCouponRemove());
        }

        // 4. History view actions
        historyView.getRefreshButton().addActionListener(e -> loadUserBookingHistory());
        historyView.getBackButton().addActionListener(e -> userDashboardCardLayout.show(userDashboardContentPanel, "Browse Movies"));
        historyView.getViewDetailsButton().addActionListener(e -> viewBookingDetails());
        historyView.getCancelUserBookingButton().addActionListener(e -> cancelSelectedUserBooking());
        
        historyView.getBtnRateMovie().addActionListener(e -> handleRateAction(true));
        historyView.getBtnRateTheatre().addActionListener(e -> handleRateAction(false));

        historyView.addSelectionListener(() -> updateReviewButtonsVisibility());
        historyView.addDoubleClickListener(() -> viewBookingDetails());
    }

    /**
     * Connects the master admin view.
     */
    public void bindAdminPanel(BookingManagementView view) {
        this.adminView = view;
    }

    /**
     * Connects the seat visual layout grid in admin dashboard.
     */
    public void bindSeatManagementView(SeatManagementView view) {
        this.seatManagementView = view;
    }

    /**
     * Starts seat selection workflow directly for a selected show.
     */
    public void startBookingFlowForShow(Show show, Movie movie, Theatre theatre) {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(userDashboardContentPanel,
                    "Please log in to book tickets.", "Authentication Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (show == null) {
            JOptionPane.showMessageDialog(userDashboardContentPanel,
                    "Selected show is invalid or no longer available.", "Invalid Show", JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.activeShow = show;
        this.activeMovie = movie != null ? movie : movieDAO.getMovieById(show.getMovieId()).orElse(null);
        this.activeTheatre = theatre != null ? theatre : theatreDAO.getTheatreById(show.getTheatreId()).orElse(null);

        loadSeatGridForActiveShow();
        userDashboardCardLayout.show(userDashboardContentPanel, "Select Seats");
    }

    private void loadSeatGridForActiveShow() {
        if (activeShow == null || seatSelectionView == null) return;
        
        stopSeatMapPolling();

        // 1. Fetch physical seats for theatre
        currentTheatreSeats = seatDAO.getSeatsByTheatre(activeShow.getTheatreId());

        // 2. Fetch booked seat IDs for this show
        List<Integer> bookedIds = bookingDAO.getBookedSeatIdsForShow(activeShow.getShowId());
        currentBookedSeatIds = new HashSet<>(bookedIds);

        // 3. Clear active selections and load holds
        int userId = UserSession.getInstance().getUserId();
        Set<Integer> heldByMe = seatHoldService.getMyHeldSeatIds(activeShow.getShowId(), userId);
        Set<Integer> heldOther = seatHoldService.getOtherUsersHeldSeatIds(activeShow.getShowId(), userId);

        activeSelectedSeats.clear();
        for (Seat s : currentTheatreSeats) {
            if (heldByMe.contains(s.getSeatId())) activeSelectedSeats.add(s);
        }
        activeTotalAmount = BigDecimal.ZERO;

        // 4. Build dynamic seat map
        seatSelectionView.buildSeatMap(currentTheatreSeats, currentBookedSeatIds, heldByMe, heldOther, e -> handleSeatToggled());

        // 5. Update summary header
        String movieTitle = activeMovie != null ? activeMovie.getTitle() : "Movie #" + activeShow.getMovieId();
        String theatreName = activeTheatre != null ? activeTheatre.getTheatreName() : "Theatre #" + activeShow.getTheatreId();
        String screen = activeShow.getScreenNumber() != null ? activeShow.getScreenNumber() : "Screen 1";
        String dateTime = activeShow.getShowDate() + " " + activeShow.getShowTime();

        seatSelectionView.updateSummary(movieTitle, theatreName, screen, dateTime, "None Selected", 0, BigDecimal.ZERO);
        
        if (!activeSelectedSeats.isEmpty()) {
            updateSummaryForSelections();
            if (holdCountdownTimer == null || !holdCountdownTimer.isRunning()) {
                startHoldCountdown();
            }
        }
        
        startSeatMapPolling();
    }

    private void handleSeatToggled() {
        List<Seat> newlySelected = seatSelectionView.getSelectedSeats();

        if (newlySelected.size() > MAX_SEATS_PER_BOOKING) {
            JOptionPane.showMessageDialog(userDashboardContentPanel,
                    "You can select up to " + MAX_SEATS_PER_BOOKING + " seats per booking.",
                    "Seat Limit Reached", JOptionPane.WARNING_MESSAGE);
            seatSelectionView.revertSelectionTo(activeSelectedSeats);
            return;
        }

        int userId = UserSession.getInstance().getUserId();
        
        List<Integer> addedIds = new ArrayList<>();
        for (Seat s : newlySelected) {
            if (!activeSelectedSeats.contains(s)) addedIds.add(s.getSeatId());
        }
        
        List<Integer> removedIds = new ArrayList<>();
        for (Seat s : activeSelectedSeats) {
            if (!newlySelected.contains(s)) removedIds.add(s.getSeatId());
        }

        if (!removedIds.isEmpty()) {
            seatHoldService.releaseSeats(activeShow.getShowId(), removedIds, userId);
            activeSelectedSeats.removeIf(s -> removedIds.contains(s.getSeatId()));
            if (activeSelectedSeats.isEmpty()) stopHoldCountdown();
        }

        if (!addedIds.isEmpty()) {
            List<Integer> failed = seatHoldService.tryHoldSeats(activeShow.getShowId(), addedIds, userId);
            if (!failed.isEmpty()) {
                JOptionPane.showMessageDialog(userDashboardContentPanel, 
                    "One or more selected seats are no longer available.", "Seat Unavailable", JOptionPane.WARNING_MESSAGE);
                newlySelected.removeIf(s -> failed.contains(s.getSeatId()));
                seatSelectionView.revertSelectionTo(activeSelectedSeats);
            } else {
                for (Seat s : newlySelected) {
                    if (addedIds.contains(s.getSeatId()) && !failed.contains(s.getSeatId())) {
                        activeSelectedSeats.add(s);
                    }
                }
                if (holdCountdownTimer == null || !holdCountdownTimer.isRunning()) {
                    startHoldCountdown();
                } else {
                    startHoldCountdown(); // reset to 5 minutes
                }
            }
        }
        
        seatSelectionView.refreshSeatStates(currentBookedSeatIds, seatHoldService.getMyHeldSeatIds(activeShow.getShowId(), userId), seatHoldService.getOtherUsersHeldSeatIds(activeShow.getShowId(), userId));
        updateSummaryForSelections();
    }

    private void updateSummaryForSelections() {
        BigDecimal sum = BigDecimal.ZERO;
        
        java.util.Map<String, java.util.List<String>> catMap = new java.util.HashMap<>();
        java.util.Map<String, BigDecimal> catSubtotal = new java.util.HashMap<>();
        
        for (Seat s : activeSelectedSeats) {
            String cat = s.getSeatType() != null ? s.getSeatType().toUpperCase() : "REGULAR";
            BigDecimal finalPrice = calculateSeatCategoryPrice(s, activeShow, currentTheatreSeats.size(), currentBookedSeatIds.size());
            sum = sum.add(finalPrice);
            catMap.computeIfAbsent(cat, k -> new ArrayList<>()).add(s.getSeatNumber());
            catSubtotal.put(cat, catSubtotal.getOrDefault(cat, BigDecimal.ZERO).add(finalPrice));
        }
        activeTotalAmount = sum;

        String movieTitle = activeMovie != null ? activeMovie.getTitle() : "—";
        String theatreName = activeTheatre != null ? activeTheatre.getTheatreName() : "—";
        String screen = activeShow != null && activeShow.getScreenNumber() != null ? activeShow.getScreenNumber() : "1";
        String dateTime = activeShow != null ? activeShow.getShowDate() + " " + activeShow.getShowTime() : "—";

        StringBuilder html = new StringBuilder("<html><body style='color:#F8FAFC;font-family:sans-serif;font-size:10px;'>");
        for (java.util.Map.Entry<String, java.util.List<String>> entry : catMap.entrySet()) {
            html.append("<b style='color:#94A3B8;'>").append(entry.getKey()).append(":</b><br/>");
            html.append(String.join(", ", entry.getValue())).append("<br/>");
            int count = entry.getValue().size();
            BigDecimal subtotal = catSubtotal.get(entry.getKey());
            BigDecimal perTicket = count > 0 ? subtotal.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
            html.append("<span style='color:#38BDF8;'>").append(count).append(" × ₹").append(perTicket).append(" = ₹").append(subtotal).append("</span><br/><br/>");
        }
        
        double occPct = currentTheatreSeats.isEmpty() ? 0 : (currentBookedSeatIds.size() * 100.0) / currentTheatreSeats.size();
        html.append("<b style='color:#94A3B8;'>Surcharges Applied:</b><br/>");
        boolean isWeekend = false;
        if (activeShow != null && activeShow.getShowDate() != null) {
            java.time.DayOfWeek day = activeShow.getShowDate().getDayOfWeek();
            isWeekend = (day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY);
        }
        html.append("Weekend: ").append(isWeekend ? "+10%" : "0%").append("<br/>");
        String demandStr = "+0%";
        if (occPct > 80) demandStr = "+20%";
        else if (occPct > 60) demandStr = "+10%";
        else if (occPct > 30) demandStr = "+5%";
        html.append("Demand (").append(String.format("%.1f", occPct)).append("% full): ").append(demandStr).append("<br/>");
        
        html.append("</body></html>");

        seatSelectionView.updateSummary(movieTitle, theatreName, screen, dateTime, html.toString(), activeSelectedSeats.size(), activeTotalAmount);
    }
    
    private void startSeatMapPolling() {
        stopSeatMapPolling();
        seatMapRefreshTimer = new javax.swing.Timer(SeatHoldService.REFRESH_INTERVAL_SECONDS * 1000, e -> {
            if (activeShow != null && seatSelectionView.isShowing()) {
                int userId = UserSession.getInstance().getUserId();
                List<Integer> bookedIds = bookingDAO.getBookedSeatIdsForShow(activeShow.getShowId());
                currentBookedSeatIds = new HashSet<>(bookedIds);
                Set<Integer> heldByMe = seatHoldService.getMyHeldSeatIds(activeShow.getShowId(), userId);
                Set<Integer> heldOther = seatHoldService.getOtherUsersHeldSeatIds(activeShow.getShowId(), userId);
                
                boolean dropped = activeSelectedSeats.removeIf(s -> !heldByMe.contains(s.getSeatId()));
                if (dropped) {
                    updateSummaryForSelections();
                    if (activeSelectedSeats.isEmpty()) stopHoldCountdown();
                }

                seatSelectionView.refreshSeatStates(currentBookedSeatIds, heldByMe, heldOther);
            } else if (activeShow == null || !seatSelectionView.isShowing()) {
                stopSeatMapPolling();
            }
        });
        seatMapRefreshTimer.start();
    }
    
    private void stopSeatMapPolling() {
        if (seatMapRefreshTimer != null && seatMapRefreshTimer.isRunning()) {
            seatMapRefreshTimer.stop();
            seatMapRefreshTimer = null;
        }
    }

    private BigDecimal calculateSeatCategoryPrice(Seat seat, Show show, int totalSeats, int bookedSeats) {
        if (show == null) return BigDecimal.ZERO;
        
        BigDecimal basePrice = show.getTicketPrice() != null ? show.getTicketPrice() : BigDecimal.ZERO;
        String type = seat.getSeatType() != null ? seat.getSeatType().toUpperCase() : "REGULAR";
        
        BigDecimal catPrice = basePrice;
        if ("PREMIUM".equals(type)) {
            catPrice = show.getPremiumPrice() != null ? show.getPremiumPrice() : basePrice.add(new BigDecimal("50.00"));
        } else if ("VIP".equals(type)) {
            catPrice = show.getVipPrice() != null ? show.getVipPrice() : basePrice.add(new BigDecimal("100.00"));
        }
        
        // 1. Weekend Surcharge (+10%)
        if (show.getShowDate() != null) {
            java.time.DayOfWeek day = show.getShowDate().getDayOfWeek();
            if (day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY) {
                catPrice = catPrice.add(catPrice.multiply(new BigDecimal("0.10")));
            }
        }
        
        // 2. Demand Surcharge
        if (totalSeats > 0) {
            double occ = (bookedSeats * 100.0) / totalSeats;
            if (occ > 80) {
                catPrice = catPrice.add(catPrice.multiply(new BigDecimal("0.20")));
            } else if (occ > 60) {
                catPrice = catPrice.add(catPrice.multiply(new BigDecimal("0.10")));
            } else if (occ > 30) {
                catPrice = catPrice.add(catPrice.multiply(new BigDecimal("0.05")));
            }
        }
        
        return catPrice.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private void clearSeatSelection() {
        activeSelectedSeats.clear();
        activeTotalAmount = BigDecimal.ZERO;
        seatSelectionView.clearSelection();
    }

    private void proceedToConfirmation() {
        if (activeSelectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(seatSelectionView, "Please select at least one seat to proceed.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (activeShow == null) {
            JOptionPane.showMessageDialog(seatSelectionView, "Show session is invalid. Please select a show again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        stopSeatMapPolling(); // Pause polling while in confirmation

        // Populate Confirmation Card
        String movieTitle = activeMovie != null ? activeMovie.getTitle() : "Movie #" + activeShow.getMovieId();
        String theatreName = activeTheatre != null ? activeTheatre.getTheatreName() : "Theatre #" + activeShow.getTheatreId();
        String screen = activeShow.getScreenNumber() != null ? activeShow.getScreenNumber() : "Screen 1";

        List<String> seatNames = new ArrayList<>();
        for (Seat s : activeSelectedSeats) {
            String cat = s.getSeatType() != null ? s.getSeatType() : "REGULAR";
            seatNames.add(s.getSeatNumber() + " (" + cat + ")");
        }

        confirmationView.getMovieLabel().setText(movieTitle);
        confirmationView.getTheatreLabel().setText(theatreName);
        confirmationView.getScreenLabel().setText(screen);
        confirmationView.getDateLabel().setText(activeShow.getShowDate() != null ? activeShow.getShowDate().toString() : "—");
        confirmationView.getTimeLabel().setText(activeShow.getShowTime() != null ? activeShow.getShowTime().toString() : "—");
        confirmationView.getSeatsLabel().setText(String.join(", ", seatNames));
        confirmationView.getTicketCountLabel().setText(String.valueOf(activeSelectedSeats.size()));
        confirmationView.getPricePerTicketLabel().setText("Base ₹" + activeShow.getTicketPrice().setScale(2) + " (Dynamic category pricing applied)");
        confirmationView.getTotalAmountLabel().setText("₹" + activeTotalAmount.setScale(2));

        userDashboardCardLayout.show(userDashboardContentPanel, "Confirm Booking");
    }

    /** Starts a Swing countdown timer for the seat hold duration. */
    private void startHoldCountdown() {
        stopHoldCountdown();
        int userId = UserSession.getInstance().getUserId();
        final long[] secondsLeft = {seatHoldService.getSecondsRemaining(activeShow.getShowId(), userId)};
        if (secondsLeft[0] <= 0) secondsLeft[0] = SeatHoldService.HOLD_DURATION_MINUTES * 60L;
        
        holdCountdownTimer = new javax.swing.Timer(1000, e -> {
            secondsLeft[0]--;
            if (secondsLeft[0] <= 0) {
                stopHoldCountdown();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(userDashboardContentPanel,
                        "Your seat hold has expired. Please select seats again.",
                        "Hold Expired", JOptionPane.WARNING_MESSAGE);
                    releaseCurrentHold();
                    activeSelectedSeats.clear();
                    updateSummaryForSelections();
                    if (seatSelectionView != null) {
                        seatSelectionView.clearSelection();
                        seatSelectionView.setHoldCountdownText(" ");
                    }
                    userDashboardCardLayout.show(userDashboardContentPanel, "Select Seats");
                });
            } else {
                long mm = secondsLeft[0] / 60;
                long ss = secondsLeft[0] % 60;
                String timeStr = String.format("%02d:%02d", mm, ss);
                if (seatSelectionView != null) {
                    seatSelectionView.setHoldCountdownText("Hold Expires In: " + timeStr);
                }
                if (confirmationView != null) {
                    try {
                        java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(confirmationView);
                        if (win instanceof javax.swing.JFrame jf) {
                            String base = jf.getTitle().replaceAll(" \\[Hold: .*\\]", "");
                            jf.setTitle(base + " [Hold: " + timeStr + "]");
                        }
                    } catch (Exception ignored) {}
                }
            }
        });
        holdCountdownTimer.start();
    }

    private void stopHoldCountdown() {
        if (holdCountdownTimer != null && holdCountdownTimer.isRunning()) {
            holdCountdownTimer.stop();
            holdCountdownTimer = null;
        }
        if (seatSelectionView != null) {
            seatSelectionView.setHoldCountdownText(" ");
        }
        try {
            java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(confirmationView);
            if (win instanceof javax.swing.JFrame jf) {
                String base = jf.getTitle().replaceAll(" \\[Hold: .*\\]", "");
                jf.setTitle(base);
            }
        } catch (Exception ignored) {}
    }

    private void releaseCurrentHold() {
        if (activeShow == null || activeSelectedSeats.isEmpty()) return;
        int userId = UserSession.getInstance().getUserId();
        List<Integer> seatIds = new ArrayList<>();
        for (Seat s : activeSelectedSeats) seatIds.add(s.getSeatId());
        seatHoldService.releaseSeats(activeShow.getShowId(), seatIds, userId);
    }

    private com.movieticket.model.Booking activeBooking;
    private java.util.Map<Integer, BigDecimal> activeSeatPrices;
    private final com.movieticket.util.CinePointsService cinePointsService;
    private final com.movieticket.util.CouponService couponService;
    
    private com.movieticket.model.Coupon appliedCoupon = null;
    private BigDecimal couponDiscount = BigDecimal.ZERO;

    private void proceedToPayment() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(confirmationView,
                    "Session expired. Please log in again.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (activeShow == null || activeSelectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(confirmationView,
                    "Booking session data is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal verifiedTotal = BigDecimal.ZERO;
        activeSeatPrices = new java.util.HashMap<>();
        for (Seat s : activeSelectedSeats) {
            BigDecimal price = calculateSeatCategoryPrice(s, activeShow, currentTheatreSeats.size(), currentBookedSeatIds.size());
            verifiedTotal = verifiedTotal.add(price);
            activeSeatPrices.put(s.getSeatId(), price);
        }

        activeBooking = new Booking(
                0,
                currentUser.getUserId(),
                activeShow.getShowId(),
                LocalDateTime.now(),
                verifiedTotal,
                "PENDING"
        );

        int bookingId = bookingDAO.createBookingWithSeatPrices(activeBooking, activeSeatPrices);

        if (bookingId == -2) {
            JOptionPane.showMessageDialog(confirmationView,
                    "One or more selected seats are no longer available for this show.\nPlease select different seats.",
                    "Seat Unavailable", JOptionPane.WARNING_MESSAGE);
            loadSeatGridForActiveShow();
            userDashboardCardLayout.show(userDashboardContentPanel, "Select Seats");
            return;
        }

        if (bookingId <= 0) {
            JOptionPane.showMessageDialog(confirmationView,
                    "Booking could not be initialized due to a database error.\nPlease try again.",
                    "Booking Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        activeBooking.setBookingId(bookingId);

        // Switch to Payment View
        String movieTitle = activeMovie != null ? activeMovie.getTitle() : "Movie #" + activeShow.getMovieId();
        String theatreName = activeTheatre != null ? activeTheatre.getTheatreName() : "Theatre #" + activeShow.getTheatreId();
        String screen = activeShow.getScreenNumber() != null ? activeShow.getScreenNumber() : "Screen 1";

        List<String> seatNames = new ArrayList<>();
        for (Seat s : activeSelectedSeats) {
            String cat = s.getSeatType() != null ? s.getSeatType() : "REGULAR";
            seatNames.add(s.getSeatNumber() + " (" + cat + ")");
        }

        paymentView.getMovieLabel().setText(movieTitle);
        paymentView.getTheatreLabel().setText(theatreName);
        paymentView.getDateLabel().setText(activeShow.getShowDate() != null ? activeShow.getShowDate().toString() : "—");
        paymentView.getTimeLabel().setText(activeShow.getShowTime() != null ? activeShow.getShowTime().toString() : "—");
        paymentView.getSeatsLabel().setText(String.join(", ", seatNames));
        
        appliedCoupon = null;
        couponDiscount = BigDecimal.ZERO;
        paymentView.getCouponCodeField().setText("");
        paymentView.getCouponCodeField().setEnabled(true);
        paymentView.getApplyCouponButton().setVisible(true);
        paymentView.getRemoveCouponButton().setVisible(false);
        paymentView.getCouponDiscountLabel().setText("-₹0.00");
        paymentView.getCouponMessageLabel().setText(" ");
        
        paymentView.getTotalAmountLabel().setText("₹" + verifiedTotal.setScale(2));

        int pointsAvailable = cinePointsService.getBalance(currentUser.getUserId());
        if (pointsAvailable >= 100) {
            paymentView.getUseCinePointsCheckbox().setVisible(true);
            paymentView.getUseCinePointsCheckbox().setText("Use CinePoints (Balance: " + pointsAvailable + ")");
            paymentView.getUseCinePointsCheckbox().setSelected(false);
        } else {
            paymentView.getUseCinePointsCheckbox().setVisible(false);
            paymentView.getUseCinePointsCheckbox().setSelected(false);
        }
        paymentView.getCinePointsDiscountLabel().setText("-₹0.00");

        userDashboardCardLayout.show(userDashboardContentPanel, "Payment");
    }

    private void handleCouponApply() {
        if (activeBooking == null) return;
        String code = paymentView.getCouponCodeField().getText().trim();
        int userId = UserSession.getInstance().getUserId();
        
        com.movieticket.util.CouponService.CouponValidationResult result = couponService.validateCoupon(code, userId, activeBooking.getTotalAmount());
        
        if (result.isValid()) {
            appliedCoupon = result.getCoupon();
            couponDiscount = couponService.calculateDiscount(appliedCoupon, activeBooking.getTotalAmount());
            
            paymentView.getCouponMessageLabel().setText(result.getMessage());
            paymentView.getCouponMessageLabel().setForeground(com.movieticket.util.CineBookTheme.SUCCESS_COLOR);
            paymentView.getCouponDiscountLabel().setText("-₹" + couponDiscount.setScale(2));
            
            paymentView.getCouponCodeField().setEnabled(false);
            paymentView.getApplyCouponButton().setVisible(false);
            paymentView.getRemoveCouponButton().setVisible(true);
            
            // Re-evaluate CinePoints checkbox state based on allowCinepoints flag
            if (!appliedCoupon.isAllowCinepoints()) {
                paymentView.getUseCinePointsCheckbox().setSelected(false);
                paymentView.getUseCinePointsCheckbox().setEnabled(false);
                paymentView.getUseCinePointsCheckbox().setToolTipText("CinePoints cannot be combined with this coupon.");
            } else {
                paymentView.getUseCinePointsCheckbox().setEnabled(true);
                paymentView.getUseCinePointsCheckbox().setToolTipText(null);
            }
            
            recalculateFinalTotal();
        } else {
            paymentView.getCouponMessageLabel().setText(result.getMessage());
            paymentView.getCouponMessageLabel().setForeground(com.movieticket.util.CineBookTheme.DANGER_COLOR);
            paymentView.getCouponDiscountLabel().setText("-₹0.00");
            appliedCoupon = null;
            couponDiscount = BigDecimal.ZERO;
            recalculateFinalTotal();
        }
    }

    private void handleCouponRemove() {
        appliedCoupon = null;
        couponDiscount = BigDecimal.ZERO;
        
        paymentView.getCouponCodeField().setText("");
        paymentView.getCouponCodeField().setEnabled(true);
        paymentView.getApplyCouponButton().setVisible(true);
        paymentView.getRemoveCouponButton().setVisible(false);
        
        paymentView.getCouponMessageLabel().setText("Coupon removed.");
        paymentView.getCouponMessageLabel().setForeground(com.movieticket.util.CineBookTheme.TEXT_MUTED);
        paymentView.getCouponDiscountLabel().setText("-₹0.00");
        
        paymentView.getUseCinePointsCheckbox().setEnabled(true);
        paymentView.getUseCinePointsCheckbox().setToolTipText(null);
        
        recalculateFinalTotal();
    }

    private void handlePointsToggle() {
        if (activeBooking == null) return;
        if (appliedCoupon != null && !appliedCoupon.isAllowCinepoints() && paymentView.getUseCinePointsCheckbox().isSelected()) {
            paymentView.getUseCinePointsCheckbox().setSelected(false);
            JOptionPane.showMessageDialog(paymentView, "CinePoints cannot be combined with this coupon.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        recalculateFinalTotal();
    }

    private void recalculateFinalTotal() {
        if (activeBooking == null) return;
        
        boolean usePoints = paymentView.getUseCinePointsCheckbox().isSelected();
        int userId = UserSession.getInstance().getUserId();
        int pointsAvailable = cinePointsService.getBalance(userId);
        
        BigDecimal remainingAmount = activeBooking.getTotalAmount().subtract(couponDiscount);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) remainingAmount = BigDecimal.ZERO;
        
        BigDecimal pointsDiscount = BigDecimal.ZERO;
        if (usePoints) {
            int pointsToRedeem = cinePointsService.calculateMaxRedeemablePoints(pointsAvailable, remainingAmount);
            pointsDiscount = cinePointsService.calculateDiscount(pointsToRedeem);
        }
        
        paymentView.getCinePointsDiscountLabel().setText("-₹" + pointsDiscount.setScale(2));
        
        BigDecimal finalAmount = remainingAmount.subtract(pointsDiscount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) finalAmount = BigDecimal.ZERO;
        
        paymentView.getTotalAmountLabel().setText("₹" + finalAmount.setScale(2));
    }

    private void processPayment() {
        if (activeBooking == null) return;
        
        String method = (String) paymentView.getPaymentMethodCombo().getSelectedItem();
        if ("UPI".equals(method) && paymentView.getUpiIdField().getText().trim().isEmpty()) {
            ThemeManager.showWarning(paymentView, "Please enter your UPI ID.", "Missing Information");
            return;
        }

        paymentView.showProcessingState();

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private com.movieticket.model.Payment payment;
            @Override
            protected Boolean doInBackground() throws Exception {
                // Backend re-calculation for security
                int userId = UserSession.getInstance().getUserId();
                BigDecimal backendCouponDiscount = BigDecimal.ZERO;
                
                if (appliedCoupon != null) {
                    com.movieticket.util.CouponService.CouponValidationResult validation = couponService.validateCoupon(appliedCoupon.getCode(), userId, activeBooking.getTotalAmount());
                    if (validation.isValid()) {
                        backendCouponDiscount = couponService.calculateDiscount(appliedCoupon, activeBooking.getTotalAmount());
                    } else {
                        // Coupon became invalid during checkout
                        throw new IllegalStateException(validation.getMessage());
                    }
                }
                
                BigDecimal remainingAfterCoupon = activeBooking.getTotalAmount().subtract(backendCouponDiscount);
                if (remainingAfterCoupon.compareTo(BigDecimal.ZERO) < 0) remainingAfterCoupon = BigDecimal.ZERO;

                BigDecimal pointsDiscount = BigDecimal.ZERO;
                int pointsToRedeem = 0;
                boolean usePoints = paymentView.getUseCinePointsCheckbox().isSelected();
                if (usePoints) {
                    if (appliedCoupon != null && !appliedCoupon.isAllowCinepoints()) {
                        throw new IllegalStateException("CinePoints cannot be combined with the applied coupon.");
                    }
                    int pointsAvailable = cinePointsService.getBalance(userId);
                    pointsToRedeem = cinePointsService.calculateMaxRedeemablePoints(pointsAvailable, remainingAfterCoupon);
                    pointsDiscount = cinePointsService.calculateDiscount(pointsToRedeem);
                }
                
                BigDecimal finalAmount = remainingAfterCoupon.subtract(pointsDiscount);
                if (finalAmount.compareTo(BigDecimal.ZERO) < 0) finalAmount = BigDecimal.ZERO;

                payment = paymentService.initializePayment(activeBooking.getBookingId(), finalAmount, method);
                Thread.sleep(2000); // Simulate processing delay
                boolean success = payment != null && paymentService.completePayment(payment, "SUCCESS");

                if (success) {
                    if (appliedCoupon != null) {
                        couponService.recordUsage(appliedCoupon.getId(), userId, activeBooking.getBookingId(), backendCouponDiscount);
                    }
                    if (pointsToRedeem > 0) {
                        cinePointsService.redeemPoints(userId, pointsToRedeem, activeBooking.getBookingId());
                    }
                    cinePointsService.awardPoints(userId, activeBooking.getBookingId(), activeShow.getShowId(), activeShow.getShowDate().atTime(activeShow.getShowTime()), finalAmount);
                }
                return success;
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        paymentView.showSuccessState(() -> completeBookingSuccess(payment));
                    } else {
                        paymentView.showFailedState(() -> cancelPayment());
                    }
                } catch (Exception ex) {
                    if (ex.getCause() instanceof IllegalStateException) {
                        JOptionPane.showMessageDialog(paymentView, ex.getCause().getMessage(), "Payment Error", JOptionPane.ERROR_MESSAGE);
                    }
                    paymentView.showFailedState(() -> cancelPayment());
                }
            }
        };
        worker.execute();
    }

    private void cancelPayment() {
        if (activeBooking != null) {
            bookingDAO.updateBookingStatus(activeBooking.getBookingId(), "CANCELLED");
        }
        releaseCurrentHold();
        clearSeatSelection();
        paymentView.resetState();
        userDashboardCardLayout.show(userDashboardContentPanel, "Browse Movies");
    }

    private void completeBookingSuccess(com.movieticket.model.Payment payment) {
        stopHoldCountdown();
        int userId = UserSession.getInstance().getUserId();
        List<Integer> seatIds = new ArrayList<>();
        for (Seat s : activeSelectedSeats) seatIds.add(s.getSeatId());
        seatHoldService.confirmBooking(activeShow.getShowId(), seatIds, userId);

        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String formattedBookingId = String.format("CINE-%s-%04d", datePart, activeBooking.getBookingId());
        
        List<String> seatNames = new ArrayList<>();
        for (Seat s : activeSelectedSeats) {
            String cat = s.getSeatType() != null ? s.getSeatType() : "REGULAR";
            seatNames.add(s.getSeatNumber() + " (" + cat + ")");
        }

        activeBooking.setStatus("CONFIRMED");

        DigitalTicketDialog dialog = new DigitalTicketDialog(
                SwingUtilities.getWindowAncestor(paymentView),
                activeBooking,
                activeMovie,
                activeTheatre,
                activeShow,
                String.join(", ", seatNames),
                formattedBookingId
        );
        dialog.setVisible(true);

        User currentCustomer = UserSession.getInstance().getCurrentUser();
        if (currentCustomer != null) {
            new NotificationController().sendBookingConfirmation(
                    currentCustomer.getUserId(),
                    activeBooking.getBookingId(),
                    activeMovie != null ? activeMovie.getTitle() : "Movie #" + activeShow.getMovieId(),
                    String.join(", ", seatNames),
                    activeShow.getShowDate() != null ? activeShow.getShowDate().toString() : "Today",
                    activeShow.getShowTime() != null ? activeShow.getShowTime().toString() : "Showtime"
            );
        }

        clearSeatSelection();
        paymentView.resetState();
        userDashboardCardLayout.show(userDashboardContentPanel, "Browse Movies");
    }

    private void cancelBookingWorkflow() {
        stopSeatMapPolling();
        releaseCurrentHold();
        stopHoldCountdown();
        clearSeatSelection();
        userDashboardCardLayout.show(userDashboardContentPanel, "Browse Movies");
    }

    /**
     * Loads the current logged-in user's booking history from MySQL (Phase 14).
     * Strictly queries database with WHERE user_id = ? ORDER BY booking_date DESC.
     */
    public void loadUserBookingHistory() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            if (historyView != null) historyView.setEmptyStateVisible(true);
            return;
        }

        if (historyView == null) return;

        historyView.clearBookings();

        try {
            List<Booking> userBookings = bookingDAO.getBookingsByUser(currentUser.getUserId());

            if (userBookings.isEmpty()) {
                historyView.setEmptyStateVisible(true);
                return;
            }

            historyView.setEmptyStateVisible(false);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (Booking b : userBookings) {
                Optional<Show> optShow = showDAO.getShowById(b.getShowId());
                String movieTitle = "—";
                String theatreName = "—";
                String screen = "Screen 1";
                String showDate = "—";
                String showTime = "—";

                if (optShow.isPresent()) {
                    Show show = optShow.get();
                    Optional<Movie> optMovie = movieDAO.getMovieById(show.getMovieId());
                    movieTitle = optMovie.map(Movie::getTitle).orElse("Movie #" + show.getMovieId());

                    Optional<Theatre> optTheatre = theatreDAO.getTheatreById(show.getTheatreId());
                    theatreName = optTheatre.map(Theatre::getTheatreName).orElse("Theatre #" + show.getTheatreId());

                    screen = show.getScreenNumber() != null ? show.getScreenNumber() : "Screen 1";
                    showDate = show.getShowDate() != null ? show.getShowDate().toString() : "—";
                    showTime = show.getShowTime() != null ? show.getShowTime().toString() : "—";
                }

                List<String> seatNumbers = bookingDAO.getSeatNumbersForBooking(b.getBookingId());
                String seatsStr = seatNumbers.isEmpty() ? "—" : String.join(", ", seatNumbers);
                int ticketCount = seatNumbers.size();

                String formattedDate = b.getBookingDate() != null ? b.getBookingDate().format(dtf) : "—";
                String formattedAmount = b.getTotalAmount() != null ? "₹" + b.getTotalAmount().setScale(2) : "₹0.00";

                Optional<com.movieticket.model.Payment> optPayment = paymentService.getPaymentByBookingId(b.getBookingId());
                String paymentMethod = optPayment.map(com.movieticket.model.Payment::getPaymentMethod).orElse("N/A");
                String txnId = optPayment.map(com.movieticket.model.Payment::getTransactionId).orElse("N/A");

                historyView.addBookingCard(
                        "BKG" + b.getBookingId(),
                        formattedDate,
                        movieTitle,
                        theatreName,
                        screen,
                        showDate,
                        showTime,
                        seatsStr,
                        ticketCount,
                        formattedAmount,
                        b.getStatus(),
                        paymentMethod,
                        txnId
                );
            }
        } catch (Exception ex) {
            System.err.println("[BookingController] Error loading user booking history: " + ex.getMessage());
            JOptionPane.showMessageDialog(historyView,
                    "Unable to load booking history.\nPlease check the database connection.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateReviewButtonsVisibility() {
        String selectedId = historyView.getSelectedBookingId();
        if (selectedId == null) {
            historyView.getBtnRateMovie().setVisible(false);
            historyView.getBtnRateTheatre().setVisible(false);
            return;
        }

        int bookingId;
        try {
            bookingId = Integer.parseInt(selectedId.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return;
        }

        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        Optional<Booking> optBooking = bookingDAO.getBookingByIdAndUser(bookingId, currentUser.getUserId());
        if (optBooking.isPresent()) {
            Booking booking = optBooking.get();
            boolean canReview = reviewController.canUserReview(currentUser.getUserId(), booking);
            
            historyView.getBtnRateMovie().setVisible(canReview);
            historyView.getBtnRateTheatre().setVisible(canReview);
            
            if (canReview) {
                com.movieticket.dao.ReviewDAO revDao = new com.movieticket.dao.ReviewDAO();
                boolean ratedMovie = revDao.hasUserReviewedBooking(currentUser.getUserId(), booking.getBookingId(), true);
                boolean ratedTheatre = revDao.hasUserReviewedBooking(currentUser.getUserId(), booking.getBookingId(), false);
                
                historyView.getBtnRateMovie().setText(ratedMovie ? "Movie Rated ✅" : "Rate Movie");
                historyView.getBtnRateMovie().setEnabled(!ratedMovie);
                
                historyView.getBtnRateTheatre().setText(ratedTheatre ? "Theatre Rated ✅" : "Rate Theatre");
                historyView.getBtnRateTheatre().setEnabled(!ratedTheatre);
            }
        }
    }

    private void handleRateAction(boolean isMovie) {
        String selectedId = historyView.getSelectedBookingId();
        if (selectedId == null) return;

        int bookingId = Integer.parseInt(selectedId.replaceAll("[^0-9]", ""));

        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        Optional<Booking> optBooking = bookingDAO.getBookingByIdAndUser(bookingId, currentUser.getUserId());
        if (optBooking.isPresent()) {
            Booking booking = optBooking.get();
            Optional<Show> optShow = showDAO.getShowById(booking.getShowId());
            if (optShow.isPresent()) {
                Show show = optShow.get();
                if (isMovie) {
                    Optional<Movie> optMovie = new com.movieticket.dao.MovieDAO().getMovieById(show.getMovieId());
                    String targetName = optMovie.map(Movie::getTitle).orElse("Movie");
                    reviewController.openSubmitReviewDialog(SwingUtilities.getWindowAncestor(historyView), booking, show.getMovieId(), null, targetName);
                } else {
                    Optional<com.movieticket.model.Theatre> optTheatre = new com.movieticket.dao.TheatreDAO().getTheatreById(show.getTheatreId());
                    String targetName = optTheatre.map(com.movieticket.model.Theatre::getTheatreName).orElse("Theatre");
                    reviewController.openSubmitReviewDialog(SwingUtilities.getWindowAncestor(historyView), booking, null, show.getTheatreId(), targetName);
                }
                updateReviewButtonsVisibility();
            }
        }
    }

    /**
     * Displays detailed modal popup for selected booking with strict ownership validation (Phase 14).
     */
    public void viewBookingDetails() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(historyView, "Please log in to view booking details.",
                    "Authentication Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedId = historyView.getSelectedBookingId();
        if (selectedId == null) {
            JOptionPane.showMessageDialog(historyView, "Please select a booking from the list to view its details.",
                    "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int bookingId;
        try {
            bookingId = Integer.parseInt(selectedId.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(historyView, "Invalid booking record selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Strict ownership verification at DAO/database level
        Optional<Booking> optBooking = bookingDAO.getBookingByIdAndUser(bookingId, currentUser.getUserId());
        if (optBooking.isEmpty()) {
            JOptionPane.showMessageDialog(historyView, "Access denied. Booking not found or does not belong to you.",
                    "Security Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Booking b = optBooking.get();
        Optional<Show> optShow = showDAO.getShowById(b.getShowId());

        String movieTitle = "—";
        String movieMeta = "";
        String theatreName = "—";
        String screen = "Screen 1";
        String showDate = "—";
        String showTime = "—";

        if (optShow.isPresent()) {
            Show show = optShow.get();
            Optional<Movie> optMovie = movieDAO.getMovieById(show.getMovieId());
            if (optMovie.isPresent()) {
                Movie m = optMovie.get();
                movieTitle = m.getTitle();
                movieMeta = String.format(" (%s | %s | %d mins)", m.getGenre(), m.getLanguage(), m.getDuration());
            } else {
                movieTitle = "Movie #" + show.getMovieId();
            }

            Optional<Theatre> optTheatre = theatreDAO.getTheatreById(show.getTheatreId());
            theatreName = optTheatre.map(Theatre::getTheatreName).orElse("Theatre #" + show.getTheatreId());
            screen = show.getScreenNumber() != null ? show.getScreenNumber() : "Screen 1";
            showDate = show.getShowDate() != null ? show.getShowDate().toString() : "—";
            showTime = show.getShowTime() != null ?
                    show.getShowTime() + (show.getEndTime() != null ? " - " + show.getEndTime() : "") : "—";
        }

        List<String> seats = bookingDAO.getSeatNumbersForBooking(b.getBookingId());
        String seatsStr = seats.isEmpty() ? "—" : String.join(", ", seats);
        String datePart = b.getBookingDate() != null ? b.getBookingDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")) : "20260910";
        String formattedBookingId = String.format("CINE-%s-%04d", datePart, b.getBookingId());

        DigitalTicketDialog dialog = new DigitalTicketDialog(
                SwingUtilities.getWindowAncestor(historyView),
                b,
                optShow.flatMap(s -> movieDAO.getMovieById(s.getMovieId())).orElse(new Movie(0, movieTitle, "Unknown", 0, "Unknown", new BigDecimal(0), null)),
                optShow.flatMap(s -> theatreDAO.getTheatreById(s.getTheatreId())).orElse(new Theatre(0, theatreName, "", 0)),
                optShow.orElse(new Show()),
                seatsStr,
                formattedBookingId
        );
        dialog.setVisible(true);
    }

    /**
     * Cancels a user's selected booking after confirmation (Feature 14).
     */
    public void cancelSelectedUserBooking() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(historyView, "Please log in to cancel bookings.",
                    "Authentication Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedId = historyView.getSelectedBookingId();
        if (selectedId == null) {
            JOptionPane.showMessageDialog(historyView, "Please select a booking from the list to cancel.",
                    "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int bookingId;
        try {
            bookingId = Integer.parseInt(selectedId.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(historyView, "Invalid booking record selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Optional<Booking> optBooking = bookingDAO.getBookingByIdAndUser(bookingId, currentUser.getUserId());
        if (optBooking.isEmpty()) {
            JOptionPane.showMessageDialog(historyView, "Access denied. Booking not found or does not belong to you.",
                    "Security Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Booking b = optBooking.get();
        if ("CANCELLED".equalsIgnoreCase(b.getStatus())) {
            JOptionPane.showMessageDialog(historyView, "Booking BKG" + bookingId + " is already cancelled.",
                    "Already Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object[] options = {"YES, CANCEL BOOKING", "KEEP BOOKING"};
        int choice = JOptionPane.showOptionDialog(
                historyView,
                "Are you sure you want to cancel booking BKG" + bookingId + "?\nThis action will release your reserved seats for other customers.",
                "Confirm Booking Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[1]
        );

        if (choice == 0) {
            boolean updated = bookingDAO.updateBookingStatus(bookingId, "CANCELLED");
            if (updated) {
                cinePointsService.cancelPendingPoints(bookingId);
                JOptionPane.showMessageDialog(historyView,
                        "Booking BKG" + bookingId + " has been successfully cancelled.\nYour reserved seats are now available again.",
                        "Booking Cancelled", JOptionPane.INFORMATION_MESSAGE);
                loadUserBookingHistory();

                // Send Booking Cancellation Notification
                User currentCustomer = UserSession.getInstance().getCurrentUser();
                if (currentCustomer != null) {
                    new NotificationController().sendBookingCancellation(
                            currentCustomer.getUserId(),
                            bookingId,
                            "Movie Booking"
                    );
                }
            } else {
                JOptionPane.showMessageDialog(historyView,
                        "Unable to cancel booking due to a database error.",
                        "Cancellation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
