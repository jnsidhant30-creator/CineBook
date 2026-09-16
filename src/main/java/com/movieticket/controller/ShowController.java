package com.movieticket.controller;

import com.movieticket.dao.MovieDAO;
import com.movieticket.dao.NotifyMeDAO;
import com.movieticket.dao.ShowDAO;
import com.movieticket.dao.TheatreDAO;
import com.movieticket.model.Movie;
import com.movieticket.model.Show;
import com.movieticket.model.Theatre;
import com.movieticket.model.User;
import com.movieticket.util.UserSession;
import com.movieticket.view.ShowManagementView;
import com.movieticket.view.ConflictResolutionDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for Show Management Module (Phase 10).
 *
 * Responsibilities:
 * - Admin role verification and authorization
 * - Validate all show input fields (Movie, Theatre, Screen, Date, Start Time, End Time, Ticket Price, Status)
 * - Date validation (YYYY-MM-DD) and past date scheduling prevention
 * - Time validation (HH:mm) and ensuring end time > start time
 * - Scheduling conflict detection (prevents overlapping shows on the same theatre screen on the same date)
 * - Safe show deletion protecting referential integrity (prevents deleting shows with active bookings)
 * - CRUD operations coordinated with ShowDAO, MovieDAO, TheatreDAO
 * - Multi-field search across Movie Title, Theatre Name, and Show Date
 * - Dynamic synchronization with Admin Dashboard statistics
 */
public class ShowController {

    private final ShowDAO showDAO;
    private final MovieDAO movieDAO;
    private final TheatreDAO theatreDAO;
    private final NotifyMeDAO notifyMeDAO;

    private ShowManagementView view;
    private Runnable onShowDataChanged;

    // In-memory cache for UI dropdown mapping
    private List<Movie> cachedMovies = new ArrayList<>();
    private List<Theatre> cachedTheatres = new ArrayList<>();

    // Formatting
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ShowController() {
        this.showDAO = new ShowDAO();
        this.movieDAO = new MovieDAO();
        this.theatreDAO = new TheatreDAO();
        this.notifyMeDAO = new NotifyMeDAO();
    }

    public ShowController(ShowDAO showDAO, MovieDAO movieDAO, TheatreDAO theatreDAO) {
        this.showDAO = showDAO != null ? showDAO : new ShowDAO();
        this.movieDAO = movieDAO != null ? movieDAO : new MovieDAO();
        this.theatreDAO = theatreDAO != null ? theatreDAO : new TheatreDAO();
        this.notifyMeDAO = new NotifyMeDAO();
    }

    /**
     * Backwards-compatible constructor for User Dashboard/Booking workflow.
     */
    public ShowController(List<Movie> movieSource, List<Theatre> theatreSource) {
        this();
        if (movieSource != null) this.cachedMovies = movieSource;
        if (theatreSource != null) this.cachedTheatres = theatreSource;
    }

    public void setOnShowDataChanged(Runnable onShowDataChanged) {
        this.onShowDataChanged = onShowDataChanged;
    }

    public List<Show> getShows() {
        return showDAO.getAllShows();
    }

    /**
     * Connects and initializes the Show Management View for Admin Dashboard.
     */
    public void bindView(ShowManagementView view) {
        this.view = view;
        reloadDropdowns();
        refreshTable();
        clearForm();

        // Clear existing action listeners to prevent duplicates
        for (java.awt.event.ActionListener al : view.getAddButton().getActionListeners()) {
            view.getAddButton().removeActionListener(al);
        }
        for (java.awt.event.ActionListener al : view.getUpdateButton().getActionListeners()) {
            view.getUpdateButton().removeActionListener(al);
        }
        for (java.awt.event.ActionListener al : view.getDeleteButton().getActionListeners()) {
            view.getDeleteButton().removeActionListener(al);
        }
        for (java.awt.event.ActionListener al : view.getClearButton().getActionListeners()) {
            view.getClearButton().removeActionListener(al);
        }
        if (view.getSearchButton() != null) {
            for (java.awt.event.ActionListener al : view.getSearchButton().getActionListeners()) {
                view.getSearchButton().removeActionListener(al);
            }
            view.getSearchButton().addActionListener(e -> searchShows());
        }
        if (view.getRefreshButton() != null) {
            for (java.awt.event.ActionListener al : view.getRefreshButton().getActionListeners()) {
                view.getRefreshButton().removeActionListener(al);
            }
            view.getRefreshButton().addActionListener(e -> {
                reloadDropdowns();
                refreshTable();
            });
        }

        // Action Listeners
        view.getAddButton().addActionListener(e -> addShow());
        view.getUpdateButton().addActionListener(e -> updateShow());
        view.getDeleteButton().addActionListener(e -> deleteShow());
        view.getClearButton().addActionListener(e -> clearForm());

        // Theatre selection change listener to dynamically update screens
        view.getTheatreCombo().addActionListener(e -> {
            updateScreenOptionsForSelectedTheatre();
            updateTimeline();
        });

        // Movie selection change listener to auto-propose end time if start time is present
        view.getMovieCombo().addActionListener(e -> autoCalculateEndTime());
        
        // Timeline listeners
        if (view.getScreenCombo() != null) {
            view.getScreenCombo().addActionListener(e -> updateTimeline());
        }
        if (view.getDateField() != null) {
            view.getDateField().getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { updateTimeline(); }
                public void removeUpdate(DocumentEvent e) { updateTimeline(); }
                public void changedUpdate(DocumentEvent e) { updateTimeline(); }
            });
        }

        // Mouse click on table row to populate form
        view.getShowsTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.getShowsTable().getSelectedRow();
                if (row != -1) {
                    populateFormFromRow(row);
                }
            }
        });
    }

    /**
     * Reloads movies and theatres from MySQL and populates combo boxes.
     */
    public void reloadDropdowns() {
        if (view == null) return;

        try {
            cachedMovies = movieDAO.getAllMovies();
            cachedTheatres = theatreDAO.getAllTheatres();

            view.getMovieCombo().removeAllItems();
            if (cachedMovies.isEmpty()) {
                view.getMovieCombo().addItem("No movies available");
            } else {
                for (Movie m : cachedMovies) {
                    view.getMovieCombo().addItem(m.getMovieId() + " - " + m.getTitle() + " (" + m.getDuration() + " min)");
                }
            }

            view.getTheatreCombo().removeAllItems();
            if (cachedTheatres.isEmpty()) {
                view.getTheatreCombo().addItem("No theatres available");
            } else {
                for (Theatre t : cachedTheatres) {
                    view.getTheatreCombo().addItem(t.getTheatreId() + " - " + t.getTheatreName() + " (" + t.getLocation() + ")");
                }
            }

            updateScreenOptionsForSelectedTheatre();
        } catch (Exception ex) {
            System.err.println("[ShowController] Error loading dropdowns: " + ex.getMessage());
        }
    }

    /**
     * Dynamically updates the screen dropdown based on selected theatre.
     */
    private void updateScreenOptionsForSelectedTheatre() {
        if (view == null || view.getTheatreCombo().getSelectedItem() == null) return;

        String theatreSel = (String) view.getTheatreCombo().getSelectedItem();
        if (theatreSel.startsWith("No theatres")) return;
        
        Theatre selectedTheatre = getSelectedTheatre();
        if (selectedTheatre == null) return;

        view.getScreenCombo().removeAllItems();
        com.movieticket.dao.ScreenDAO screenDAO = new com.movieticket.dao.ScreenDAO();
        List<com.movieticket.model.Screen> screens = screenDAO.getScreensByTheatre(selectedTheatre.getTheatreId());
        
        for (com.movieticket.model.Screen s : screens) {
            view.getScreenCombo().addItem(s);
        }
        
        if (view.getScreenCombo().getItemCount() > 0) {
            view.getScreenCombo().setSelectedIndex(0);
        }
    }

    /**
     * Auto calculates end time based on selected movie duration and entered start time.
     */
    private void autoCalculateEndTime() {
        if (view == null) return;
        String startTimeText = view.getStartTimeField().getText().trim();
        if (startTimeText.isEmpty()) return;

        try {
            LocalTime startTime = parseTime(startTimeText);
            Movie movie = getSelectedMovie();
            if (movie != null && movie.getDuration() > 0) {
                LocalTime endTime = startTime.plusMinutes(movie.getDuration());
                view.getEndTimeField().setText(endTime.format(TIME_FORMATTER));
            }
        } catch (Exception ignored) {
            // Ignore format issues during live calculation
        }
    }

    /**
     * Validates input, detects scheduling conflicts, and adds a new show to MySQL.
     */
    public void addShow() {
        if (view == null) return;

        // 1. Admin Role Verification
        if (!isAdminSession()) {
            JOptionPane.showMessageDialog(view, "Access denied. Administrator privileges required.",
                    "Unauthorized", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Validate Selection and Form Inputs
        Movie selectedMovie = getSelectedMovie();
        if (selectedMovie == null) {
            JOptionPane.showMessageDialog(view, "Please select a valid movie.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Theatre selectedTheatre = getSelectedTheatre();
        if (selectedTheatre == null) {
            JOptionPane.showMessageDialog(view, "Please select a valid theatre.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        com.movieticket.model.Screen screenObj = (com.movieticket.model.Screen) view.getScreenCombo().getSelectedItem();
        if (screenObj == null) {
            JOptionPane.showMessageDialog(view, "Please select a screen.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String screen = screenObj.getScreenName();

        String dateText = view.getDateField().getText().trim();
        String startTimeText = view.getStartTimeField().getText().trim();
        String endTimeText = view.getEndTimeField().getText().trim();
        String priceText = view.getPriceField().getText().trim();
        String premiumPriceText = view.getPremiumPriceField().getText().trim();
        String vipPriceText = view.getVipPriceField().getText().trim();
        String status = (String) view.getStatusCombo().getSelectedItem();
        if (status == null) status = "ACTIVE";

        if (dateText.isEmpty() || startTimeText.isEmpty() || endTimeText.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter all required fields.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Date Validation
        LocalDate showDate;
        try {
            showDate = LocalDate.parse(dateText, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(view, "Invalid date format. Please use YYYY-MM-DD (e.g. 2026-08-20).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (showDate.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(view, "Show date cannot be in the past.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 4. Time Validation
        LocalTime startTime;
        try {
            startTime = parseTime(startTimeText);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(view, "Invalid start time format. Please use HH:MM (e.g. 14:30).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalTime endTime;
        try {
            endTime = parseTime(endTimeText);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(view, "Invalid end time format. Please use HH:MM (e.g. 17:00).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!endTime.isAfter(startTime)) {
            JOptionPane.showMessageDialog(view, "End time must be after start time.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 5. Price Validation
        BigDecimal ticketPrice, premiumPrice = null, vipPrice = null;
        try {
            double priceVal = Double.parseDouble(priceText);
            if (priceVal <= 0) {
                JOptionPane.showMessageDialog(view, "Ticket Price must be greater than zero.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ticketPrice = BigDecimal.valueOf(priceVal);

            if (!premiumPriceText.isEmpty()) {
                double premVal = Double.parseDouble(premiumPriceText);
                if (premVal <= priceVal) {
                    JOptionPane.showMessageDialog(view, "Premium Price must be greater than Regular Price.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                premiumPrice = BigDecimal.valueOf(premVal);
            }
            if (!vipPriceText.isEmpty()) {
                double vipVal = Double.parseDouble(vipPriceText);
                if (vipVal <= priceVal) {
                    JOptionPane.showMessageDialog(view, "VIP Price must be greater than Regular Price.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                vipPrice = BigDecimal.valueOf(vipVal);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Prices must be valid positive numbers.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 6. Scheduling Conflict Detection
        Show conflictingShow = hasScheduleConflict(selectedTheatre.getTheatreId(), screen, showDate, startTime, endTime, 0);
        if (conflictingShow != null) {
            int duration = getMovieDuration(selectedMovie.getMovieId());
            List<LocalTime> suggestions = findAvailableSlots(selectedTheatre.getTheatreId(), screen, showDate, duration);
            
            ConflictResolutionDialog dialog = new ConflictResolutionDialog((JFrame) SwingUtilities.getWindowAncestor(view),
                    conflictingShow, selectedMovie.getTitle(), startTime, endTime, suggestions);
            dialog.setVisible(true);
            
            if (dialog.getResult() == ConflictResolutionDialog.ConflictResult.CHOOSE_ANOTHER_TIME) {
                if (dialog.getSelectedSlot() != null) {
                    view.getStartTimeField().setText(dialog.getSelectedSlot().toString());
                    LocalTime newEnd = dialog.getSelectedSlot().plusMinutes(duration > 0 ? duration : 120);
                    view.getEndTimeField().setText(newEnd.toString());
                    JOptionPane.showMessageDialog(view, "Time slot updated. Please click 'Add Show' again to confirm.", "Time Updated", JOptionPane.INFORMATION_MESSAGE);
                }
            } else if (dialog.getResult() == ConflictResolutionDialog.ConflictResult.CHOOSE_ANOTHER_SCREEN) {
                view.getScreenCombo().requestFocus();
            }
            return;
        }

        int screenId = screenObj.getScreenId();
        
        if (screenId <= 0) {
            JOptionPane.showMessageDialog(view, "Invalid screen ID selected.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Show show = new Show(0, selectedMovie.getMovieId(), selectedTheatre.getTheatreId(),
                showDate, startTime, endTime, screen, status, ticketPrice);
        show.setScreenId(screenId);
        show.setPremiumPrice(premiumPrice);
        show.setVipPrice(vipPrice);

        int generatedId = showDAO.addShow(show);
        if (generatedId > 0) {
            JOptionPane.showMessageDialog(view, "Show added successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Notify users who requested to be notified when this movie becomes available
            notifyMeDAO.notifySubscribedUsers(selectedMovie.getMovieId(), selectedMovie.getTitle());
            
            clearForm();
            refreshTable();
            notifyDataChanged();
        } else {
            JOptionPane.showMessageDialog(view, "Unable to add show. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Validates input, checks conflicts, and updates an existing show in MySQL.
     */
    public void updateShow() {
        if (view == null) return;

        if (!isAdminSession()) {
            JOptionPane.showMessageDialog(view, "Access denied. Administrator privileges required.",
                    "Unauthorized", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idText = view.getShowIdField().getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please select a show from the table to update.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int showId;
        try {
            showId = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Invalid Show ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Movie selectedMovie = getSelectedMovie();
        if (selectedMovie == null) {
            JOptionPane.showMessageDialog(view, "Please select a valid movie.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Theatre selectedTheatre = getSelectedTheatre();
        if (selectedTheatre == null) {
            JOptionPane.showMessageDialog(view, "Please select a valid theatre.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        com.movieticket.model.Screen screenObj = (com.movieticket.model.Screen) view.getScreenCombo().getSelectedItem();
        if (screenObj == null) {
            JOptionPane.showMessageDialog(view, "Please select a screen.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String screen = screenObj.getScreenName();

        String dateText = view.getDateField().getText().trim();
        String startTimeText = view.getStartTimeField().getText().trim();
        String endTimeText = view.getEndTimeField().getText().trim();
        String priceText = view.getPriceField().getText().trim();
        String premiumPriceText = view.getPremiumPriceField().getText().trim();
        String vipPriceText = view.getVipPriceField().getText().trim();
        String status = (String) view.getStatusCombo().getSelectedItem();
        if (status == null) status = "ACTIVE";

        if (dateText.isEmpty() || startTimeText.isEmpty() || endTimeText.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter all required fields.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate showDate;
        try {
            showDate = LocalDate.parse(dateText, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(view, "Invalid date format. Please use YYYY-MM-DD.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalTime startTime;
        try {
            startTime = parseTime(startTimeText);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(view, "Invalid start time format. Please use HH:MM.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalTime endTime;
        try {
            endTime = parseTime(endTimeText);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(view, "Invalid end time format. Please use HH:MM.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!endTime.isAfter(startTime)) {
            JOptionPane.showMessageDialog(view, "End time must be after start time.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal ticketPrice, premiumPrice = null, vipPrice = null;
        try {
            double priceVal = Double.parseDouble(priceText);
            if (priceVal <= 0) {
                JOptionPane.showMessageDialog(view, "Ticket Price must be greater than zero.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ticketPrice = BigDecimal.valueOf(priceVal);

            if (!premiumPriceText.isEmpty()) {
                double premVal = Double.parseDouble(premiumPriceText);
                if (premVal <= priceVal) {
                    JOptionPane.showMessageDialog(view, "Premium Price must be greater than Regular Price.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                premiumPrice = BigDecimal.valueOf(premVal);
            }
            if (!vipPriceText.isEmpty()) {
                double vipVal = Double.parseDouble(vipPriceText);
                if (vipVal <= priceVal) {
                    JOptionPane.showMessageDialog(view, "VIP Price must be greater than Regular Price.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                vipPrice = BigDecimal.valueOf(vipVal);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Prices must be valid positive numbers.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check scheduling conflicts excluding the current show being edited
        Show conflictingShow = hasScheduleConflict(selectedTheatre.getTheatreId(), screen, showDate, startTime, endTime, showId);
        if (conflictingShow != null) {
            int duration = getMovieDuration(selectedMovie.getMovieId());
            List<LocalTime> suggestions = findAvailableSlots(selectedTheatre.getTheatreId(), screen, showDate, duration);
            
            ConflictResolutionDialog dialog = new ConflictResolutionDialog((JFrame) SwingUtilities.getWindowAncestor(view),
                    conflictingShow, selectedMovie.getTitle(), startTime, endTime, suggestions);
            dialog.setVisible(true);
            
            if (dialog.getResult() == ConflictResolutionDialog.ConflictResult.CHOOSE_ANOTHER_TIME) {
                if (dialog.getSelectedSlot() != null) {
                    view.getStartTimeField().setText(dialog.getSelectedSlot().toString());
                    LocalTime newEnd = dialog.getSelectedSlot().plusMinutes(duration > 0 ? duration : 120);
                    view.getEndTimeField().setText(newEnd.toString());
                    JOptionPane.showMessageDialog(view, "Time slot updated. Please click 'Update Show' again to confirm.", "Time Updated", JOptionPane.INFORMATION_MESSAGE);
                }
            } else if (dialog.getResult() == ConflictResolutionDialog.ConflictResult.CHOOSE_ANOTHER_SCREEN) {
                view.getScreenCombo().requestFocus();
            }
            return;
        }

        int screenId = screenObj.getScreenId();
        
        if (screenId <= 0) {
            JOptionPane.showMessageDialog(view, "Invalid screen ID selected.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Show show = new Show(showId, selectedMovie.getMovieId(), selectedTheatre.getTheatreId(),
                showDate, startTime, endTime, screen, status, ticketPrice);
        show.setScreenId(screenId);
        show.setPremiumPrice(premiumPrice);
        show.setVipPrice(vipPrice);

        boolean updated = showDAO.updateShow(show);
        if (updated) {
            JOptionPane.showMessageDialog(view, "Show updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
            notifyDataChanged();
        } else {
            JOptionPane.showMessageDialog(view, "Unable to update show. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Checks referential integrity and safely deletes a show from MySQL.
     */
    public void deleteShow() {
        if (view == null) return;

        if (!isAdminSession()) {
            JOptionPane.showMessageDialog(view, "Access denied. Administrator privileges required.",
                    "Unauthorized", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idText = view.getShowIdField().getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please select a show from the table to delete.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int showId;
        try {
            showId = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Invalid Show ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if any bookings reference this show
        if (showDAO.hasAssociatedBookings(showId)) {
            int proceed = JOptionPane.showConfirmDialog(view,
                    "This show has existing bookings. Cancelling it may affect customers.\nDo you want to proceed with cancelling this show?",
                    "Existing Bookings Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (proceed != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(view,
                    "Are you sure you want to delete this show?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        boolean deleted = showDAO.deleteShow(showId);
        if (deleted) {
            JOptionPane.showMessageDialog(view, "Show deleted successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
            notifyDataChanged();
        } else {
            JOptionPane.showMessageDialog(view, "Unable to delete show. Please check database connection.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Detects overlapping scheduling conflicts for the same theatre, screen, and date.
     * Includes a 15-minute buffer time after existing shows.
     * Returns the conflicting Show if found, or null if no conflict.
     */
    public Show hasScheduleConflict(int theatreId, String screenNumber, LocalDate showDate, LocalTime newStart, LocalTime newEnd, int excludeShowId) {
        List<Show> existingShows = showDAO.getShowsByTheatreAndDate(theatreId, showDate);
        for (Show existing : existingShows) {
            if (excludeShowId > 0 && existing.getShowId() == excludeShowId) continue;
            if (!existing.getScreenNumber().equals(screenNumber)) continue;

            LocalTime exStart = existing.getShowTime();
            LocalTime exEnd = existing.getEndTime();
            if (exEnd == null) {
                int durationMinutes = getMovieDuration(existing.getMovieId());
                exEnd = exStart.plusMinutes(durationMinutes > 0 ? durationMinutes : 120);
            }
            
            // Add 15-minute buffer
            exEnd = exEnd.plusMinutes(15);

            // Overlap check: existingStart < newEnd && existingEnd > newStart
            if (exStart.isBefore(newEnd) && exEnd.isAfter(newStart)) {
                return existing;
            }
        }
        return null;
    }

    /**
     * Calculates available time slots for a specific screen on a date, accounting for 15-min buffers.
     */
    public List<LocalTime> findAvailableSlots(int theatreId, String screenNumber, LocalDate date, int durationMinutes) {
        List<LocalTime> suggested = new ArrayList<>();
        List<Show> existingShows = showDAO.getShowsByTheatreAndDate(theatreId, date);
        existingShows.removeIf(s -> !s.getScreenNumber().equals(screenNumber));
        existingShows.sort(Comparator.comparing(Show::getShowTime));
        
        LocalTime currentStart = LocalTime.of(8, 0); // Cinema opens at 8 AM
        LocalTime endOfDay = LocalTime.of(23, 59).minusMinutes(durationMinutes);
        
        for (Show existing : existingShows) {
            LocalTime exStart = existing.getShowTime();
            // Try to fit between currentStart and exStart
            if (!currentStart.plusMinutes(durationMinutes).isAfter(exStart)) {
                suggested.add(currentStart);
            }
            
            LocalTime exEnd = existing.getEndTime();
            if (exEnd == null) {
                exEnd = exStart.plusMinutes(getMovieDuration(existing.getMovieId()));
            }
            currentStart = exEnd.plusMinutes(15); // 15 min buffer after show
        }
        
        // After last show
        if (!currentStart.isAfter(endOfDay)) {
            suggested.add(currentStart);
        }
        
        return suggested;
    }

    public void clearForm() {
        if (view == null) return;
        view.getShowIdField().setText("");
        view.getDateField().setText("");
        view.getStartTimeField().setText("");
        view.getEndTimeField().setText("");
        view.getPriceField().setText("");
        view.getPremiumPriceField().setText("");
        view.getVipPriceField().setText("");
        if (view.getMovieCombo().getItemCount() > 0) view.getMovieCombo().setSelectedIndex(0);
        if (view.getTheatreCombo().getItemCount() > 0) view.getTheatreCombo().setSelectedIndex(0);
        if (view.getScreenCombo().getItemCount() > 0) view.getScreenCombo().setSelectedIndex(0);
        if (view.getStatusCombo().getItemCount() > 0) view.getStatusCombo().setSelectedIndex(0);
        view.getShowsTable().clearSelection();
    }

    private void populateFormFromRow(int row) {
        if (view == null) return;
        DefaultTableModel model = view.getTableModel();

        view.getShowIdField().setText(String.valueOf(model.getValueAt(row, 0)));
        String movieTitle = String.valueOf(model.getValueAt(row, 1));
        String theatreName = String.valueOf(model.getValueAt(row, 2));
        String screen = String.valueOf(model.getValueAt(row, 3));
        view.getDateField().setText(String.valueOf(model.getValueAt(row, 4)));
        view.getStartTimeField().setText(String.valueOf(model.getValueAt(row, 5)));
        view.getEndTimeField().setText(String.valueOf(model.getValueAt(row, 6)));
        view.getPriceField().setText(String.valueOf(model.getValueAt(row, 6))); // wait, index 6 is Time in new table!
        // Table columns: "ID", "Movie Title", "Theatre", "Screen", "Date", "Time", "Price", "Premium", "VIP", "Status"
        view.getDateField().setText(String.valueOf(model.getValueAt(row, 4)));
        // split time
        String timeStr = String.valueOf(model.getValueAt(row, 5));
        if (timeStr.contains(" - ")) {
            String[] times = timeStr.split(" - ");
            view.getStartTimeField().setText(times[0]);
            view.getEndTimeField().setText(times[1]);
        }
        view.getPriceField().setText(String.valueOf(model.getValueAt(row, 6)));
        
        Object premObj = model.getValueAt(row, 7);
        view.getPremiumPriceField().setText(premObj != null && !"-".equals(premObj) ? String.valueOf(premObj) : "");
        
        Object vipObj = model.getValueAt(row, 8);
        view.getVipPriceField().setText(vipObj != null && !"-".equals(vipObj) ? String.valueOf(vipObj) : "");

        String status = String.valueOf(model.getValueAt(row, 9));

        // Select matching movie in combo
        for (int i = 0; i < view.getMovieCombo().getItemCount(); i++) {
            if (view.getMovieCombo().getItemAt(i).contains(movieTitle)) {
                view.getMovieCombo().setSelectedIndex(i);
                break;
            }
        }

        // Select matching theatre in combo
        for (int i = 0; i < view.getTheatreCombo().getItemCount(); i++) {
            if (view.getTheatreCombo().getItemAt(i).contains(theatreName)) {
                view.getTheatreCombo().setSelectedIndex(i);
                break;
            }
        }

        // Select matching screen in combo
        for (int i = 0; i < view.getScreenCombo().getItemCount(); i++) {
            com.movieticket.model.Screen sc = view.getScreenCombo().getItemAt(i);
            if (sc != null && sc.getScreenName().equalsIgnoreCase(screen)) {
                view.getScreenCombo().setSelectedIndex(i);
                break;
            }
        }

        // Select matching status in combo
        for (int i = 0; i < view.getStatusCombo().getItemCount(); i++) {
            if (view.getStatusCombo().getItemAt(i).equalsIgnoreCase(status)) {
                view.getStatusCombo().setSelectedIndex(i);
                break;
            }
        }
    }

    public void searchShows() {
        if (view == null) return;
        String query = view.getSearchField().getText().trim();
        if (query.isEmpty()) {
            refreshTable();
            return;
        }

        List<Show> results = showDAO.searchShows(query);
        populateTable(results);

        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(view, "No shows found.",
                    "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void refreshTable() {
        if (view == null) return;
        List<Show> allShows = showDAO.getAllShows();
        populateTable(allShows);
        updateTimeline();
    }

    private void updateTimeline() {
        if (view == null || view.getTimelinePanel() == null) return;
        try {
            Theatre selectedTheatre = getSelectedTheatre();
            com.movieticket.model.Screen screenObj = (com.movieticket.model.Screen) view.getScreenCombo().getSelectedItem();
            String dateStr = view.getDateField().getText().trim();
            
            if (selectedTheatre != null && screenObj != null && dateStr.length() == 10) {
                java.time.LocalDate showDate = java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
                List<Show> existingShows = showDAO.getShowsByTheatreAndDate(selectedTheatre.getTheatreId(), showDate);
                existingShows.removeIf(s -> !s.getScreenNumber().equals(screenObj.getScreenName()));
                view.getTimelinePanel().setShows(existingShows);
            } else {
                view.getTimelinePanel().setShows(new java.util.ArrayList<>());
            }
        } catch (Exception ex) {
            view.getTimelinePanel().setShows(new java.util.ArrayList<>());
        }
    }

    private void populateTable(List<Show> shows) {
        if (view == null) return;
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);

        for (Show s : shows) {
            String mTitle = getMovieTitle(s.getMovieId());
            String tName = getTheatreName(s.getTheatreId());
            int duration = getMovieDuration(s.getMovieId());
            LocalTime startTime = s.getShowTime();
            LocalTime endTime = s.getEndTime() != null ? s.getEndTime() : startTime.plusMinutes(duration > 0 ? duration : 120);

            model.addRow(new Object[]{
                    s.getShowId(),
                    mTitle,
                    tName,
                    s.getScreenNumber() != null ? s.getScreenNumber() : "Screen 1",
                    s.getShowDate(),
                    startTime.format(TIME_FORMATTER) + " - " + endTime.format(TIME_FORMATTER),
                    s.getTicketPrice(),
                    s.getPremiumPrice() != null ? s.getPremiumPrice() : "-",
                    s.getVipPrice() != null ? s.getVipPrice() : "-",
                    s.getStatus()
            });
        }
    }

    private Movie getSelectedMovie() {
        if (view == null || view.getMovieCombo().getSelectedItem() == null) return null;
        String movieSel = (String) view.getMovieCombo().getSelectedItem();
        if (movieSel.startsWith("No movies")) return null;

        try {
            int movieId = Integer.parseInt(movieSel.split(" - ")[0].trim());
            for (Movie m : cachedMovies) {
                if (m.getMovieId() == movieId) return m;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Theatre getSelectedTheatre() {
        if (view == null || view.getTheatreCombo().getSelectedItem() == null) return null;
        String theatreSel = (String) view.getTheatreCombo().getSelectedItem();
        if (theatreSel.startsWith("No theatres")) return null;

        try {
            int theatreId = Integer.parseInt(theatreSel.split(" - ")[0].trim());
            for (Theatre t : cachedTheatres) {
                if (t.getTheatreId() == theatreId) return t;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getMovieTitle(int movieId) {
        for (Movie m : cachedMovies) {
            if (m.getMovieId() == movieId) return m.getTitle();
        }
        return "Movie #" + movieId;
    }

    private int getMovieDuration(int movieId) {
        for (Movie m : cachedMovies) {
            if (m.getMovieId() == movieId) return m.getDuration();
        }
        return 120;
    }

    private String getTheatreName(int theatreId) {
        for (Theatre t : cachedTheatres) {
            if (t.getTheatreId() == theatreId) return t.getTheatreName();
        }
        return "Theatre #" + theatreId;
    }

    private LocalTime parseTime(String timeText) {
        timeText = timeText.trim();
        if (timeText.length() == 5) { // HH:mm
            return LocalTime.parse(timeText, TIME_FORMATTER);
        } else if (timeText.length() == 8) { // HH:mm:ss
            return LocalTime.parse(timeText);
        } else {
            return LocalTime.parse(timeText);
        }
    }

    private boolean isAdminSession() {
        User user = UserSession.getInstance().getCurrentUser();
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private void notifyDataChanged() {
        if (onShowDataChanged != null) {
            onShowDataChanged.run();
        }
    }
}
