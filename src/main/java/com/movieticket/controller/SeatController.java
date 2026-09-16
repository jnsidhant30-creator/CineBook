package com.movieticket.controller;

import com.movieticket.dao.SeatDAO;
import com.movieticket.dao.TheatreDAO;
import com.movieticket.model.Seat;
import com.movieticket.model.Theatre;
import com.movieticket.model.User;
import com.movieticket.util.UserSession;
import com.movieticket.view.SeatManagementView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for Seat Management Module (Phase 11).
 *
 * Responsibilities:
 * - Admin authorization check (ADMIN role only)
 * - Coordinating SeatDAO and TheatreDAO operations
 * - Loading theatres and dynamic screens
 * - Form validation (Row, Seat Number, Type, Status)
 * - Duplicate seat prevention per theatre/screen
 * - Safe seat deletion protecting booking records
 * - Multi-field search and type/status filtering
 * - Synchronizing Admin Dashboard total seats statistic
 */
public class SeatController {

    private final SeatDAO seatDAO;
    private final TheatreDAO theatreDAO;
    private SeatManagementView view;
    private Runnable onSeatDataChanged;

    private List<Theatre> theatresCache = new ArrayList<>();
    private final Map<Integer, Theatre> theatreMap = new HashMap<>();

    public SeatController() {
        this(new SeatDAO(), new TheatreDAO());
    }

    public SeatController(SeatDAO seatDAO, TheatreDAO theatreDAO) {
        this.seatDAO = seatDAO != null ? seatDAO : new SeatDAO();
        this.theatreDAO = theatreDAO != null ? theatreDAO : new TheatreDAO();
    }

    public void setOnSeatDataChanged(Runnable callback) {
        this.onSeatDataChanged = callback;
    }

    /**
     * Binds the SeatManagementView to this controller.
     */
    public void bindView(SeatManagementView view) {
        this.view = view;
        if (this.view == null) return;

        checkAdminAccess();
        initViewListeners();
        reloadTheatres();
        refreshTable();
    }

    private void checkAdminAccess() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            JOptionPane.showMessageDialog(view,
                    "Access denied. Administrator privileges required for Seat Management.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initViewListeners() {
        // Theatre selection change
        view.getTheatreCombo().addActionListener(e -> {
            updateScreenOptions();
            refreshTable();
        });

        // Screen selection change
        view.getScreenCombo().addActionListener(e -> refreshTable());

        // Type & Status filters
        view.getFilterTypeCombo().addActionListener(e -> applyFilters());
        view.getFilterStatusCombo().addActionListener(e -> applyFilters());

        // Action Buttons
        view.getAddButton().addActionListener(e -> handleAddSeat());
        view.getUpdateButton().addActionListener(e -> handleUpdateSeat());
        view.getDeleteButton().addActionListener(e -> handleDeleteSeat());
        view.getClearButton().addActionListener(e -> clearForm());

        // Search & Refresh
        view.getSearchButton().addActionListener(e -> handleSearch());
        view.getSearchField().addActionListener(e -> handleSearch());
        view.getRefreshButton().addActionListener(e -> handleRefresh());

        // Table Row Selection
        view.getSeatsTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableRowSelection();
            }
        });
    }

    /**
     * Reloads theatres from MySQL into cache and combobox.
     */
    public void reloadTheatres() {
        if (view == null) return;

        try {
            theatresCache = theatreDAO.getAllTheatres();
            theatreMap.clear();

            view.getTheatreCombo().removeAllItems();
            if (theatresCache.isEmpty()) {
                view.getTheatreCombo().addItem("No theatres available");
            } else {
                for (Theatre t : theatresCache) {
                    theatreMap.put(t.getTheatreId(), t);
                    view.getTheatreCombo().addItem(t.getTheatreId() + " - " + t.getTheatreName());
                }
            }
            updateScreenOptions();
        } catch (Exception ex) {
            System.err.println("[SeatController] Error loading theatres: " + ex.getMessage());
        }
    }

    private void updateScreenOptions() {
        if (view == null) return;

        int theatreId = getSelectedTheatreId();
        view.getScreenCombo().removeAllItems();

        if (theatreId > 0 && theatreMap.containsKey(theatreId)) {
            Theatre t = theatreMap.get(theatreId);
            int screens = Math.max(1, t.getTotalSeats() > 200 ? 4 : (t.getTotalSeats() > 100 ? 3 : 2));
            for (int i = 1; i <= screens; i++) {
                view.getScreenCombo().addItem("Screen " + i);
            }
        } else {
            view.getScreenCombo().addItem("Screen 1");
            view.getScreenCombo().addItem("Screen 2");
            view.getScreenCombo().addItem("Screen 3");
            view.getScreenCombo().addItem("Screen 4");
        }
    }

    private int getSelectedTheatreId() {
        if (view == null) return -1;
        String selected = (String) view.getTheatreCombo().getSelectedItem();
        if (selected == null || selected.startsWith("No theatres")) return -1;
        try {
            String[] parts = selected.split(" - ");
            return Integer.parseInt(parts[0].trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private String getSelectedScreen() {
        if (view == null) return "Screen 1";
        String screen = (String) view.getScreenCombo().getSelectedItem();
        return screen != null ? screen : "Screen 1";
    }

    /**
     * Refreshes the JTable with seats from the database.
     */
    public void refreshTable() {
        if (view == null) return;

        int theatreId = getSelectedTheatreId();
        List<Seat> seats;
        if (theatreId > 0) {
            seats = seatDAO.getSeatsByTheatre(theatreId);
        } else {
            seats = seatDAO.getAllSeats();
        }

        populateTable(seats);
    }

    private void populateTable(List<Seat> seats) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);

        String screen = getSelectedScreen();
        String filterType = (String) view.getFilterTypeCombo().getSelectedItem();
        String filterStatus = (String) view.getFilterStatusCombo().getSelectedItem();

        for (Seat s : seats) {
            // Check filters
            if (filterType != null && !"ALL".equalsIgnoreCase(filterType) && !filterType.equalsIgnoreCase(s.getSeatType())) {
                continue;
            }
            if (filterStatus != null && !"ALL".equalsIgnoreCase(filterStatus) && !filterStatus.equalsIgnoreCase(s.getStatus())) {
                continue;
            }

            String theatreName = "Theatre #" + s.getTheatreId();
            if (theatreMap.containsKey(s.getTheatreId())) {
                theatreName = theatreMap.get(s.getTheatreId()).getTheatreName();
            }

            model.addRow(new Object[]{
                    s.getSeatId(),
                    theatreName,
                    s.getScreenNumber() != null ? s.getScreenNumber() : screen,
                    s.getRowName(),
                    s.getSeatNumber(),
                    s.getSeatType() != null ? s.getSeatType() : "REGULAR",
                    s.getStatus()
            });
        }
    }

    private void applyFilters() {
        refreshTable();
    }

    private void handleTableRowSelection() {
        int selectedRow = view.getSeatsTable().getSelectedRow();
        if (selectedRow < 0) return;

        try {
            int seatId = (int) view.getSeatsTable().getValueAt(selectedRow, 0);
            Optional<Seat> opt = seatDAO.getSeatById(seatId);
            if (opt.isPresent()) {
                Seat seat = opt.get();
                view.getSeatIdField().setText(String.valueOf(seat.getSeatId()));
                view.getRowField().setText(seat.getRowName());
                view.getSeatNumberField().setText(seat.getSeatNumber());

                if (seat.getSeatType() != null) {
                    view.getSeatTypeCombo().setSelectedItem(seat.getSeatType());
                }
                if (seat.getStatus() != null) {
                    view.getStatusCombo().setSelectedItem(seat.getStatus());
                }

                // Match theatre combo
                for (int i = 0; i < view.getTheatreCombo().getItemCount(); i++) {
                    String item = view.getTheatreCombo().getItemAt(i);
                    if (item.startsWith(seat.getTheatreId() + " -")) {
                        view.getTheatreCombo().setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("[SeatController] Error selecting table row: " + ex.getMessage());
        }
    }

    private void handleAddSeat() {
        int theatreId = getSelectedTheatreId();
        if (theatreId <= 0) {
            JOptionPane.showMessageDialog(view, "Please select a valid theatre.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String row = view.getRowField().getText().trim();
        String seatNumber = view.getSeatNumberField().getText().trim();
        String seatType = (String) view.getSeatTypeCombo().getSelectedItem();
        String status = (String) view.getStatusCombo().getSelectedItem();
        String screen = getSelectedScreen();

        if (row.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Row cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            view.getRowField().requestFocus();
            return;
        }

        if (seatNumber.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Seat number cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            view.getSeatNumberField().requestFocus();
            return;
        }

        // Duplicate prevention check
        if (seatDAO.isSeatExists(theatreId, seatNumber)) {
            JOptionPane.showMessageDialog(view, "This seat already exists for the selected screen.", "Duplicate Seat Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Seat newSeat = new Seat(0, theatreId, screen, row, seatNumber, seatType, status);
        int generatedId = seatDAO.addSeat(newSeat);

        if (generatedId > 0) {
            JOptionPane.showMessageDialog(view, "Seat added successfully! (ID: " + generatedId + ")", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
            if (onSeatDataChanged != null) {
                onSeatDataChanged.run();
            }
        } else {
            JOptionPane.showMessageDialog(view, "Failed to add seat. Please check database connection.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateSeat() {
        String idText = view.getSeatIdField().getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please select a seat from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int seatId;
        try {
            seatId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Invalid Seat ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int theatreId = getSelectedTheatreId();
        if (theatreId <= 0) {
            JOptionPane.showMessageDialog(view, "Please select a valid theatre.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String row = view.getRowField().getText().trim();
        String seatNumber = view.getSeatNumberField().getText().trim();
        String seatType = (String) view.getSeatTypeCombo().getSelectedItem();
        String status = (String) view.getStatusCombo().getSelectedItem();
        String screen = getSelectedScreen();

        if (row.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Row cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (seatNumber.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Seat number cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Duplicate check excluding self
        if (seatDAO.isSeatExistsExcludingId(theatreId, seatNumber, seatId)) {
            JOptionPane.showMessageDialog(view, "This seat already exists for the selected screen.", "Duplicate Seat Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Seat seat = new Seat(seatId, theatreId, screen, row, seatNumber, seatType, status);
        boolean updated = seatDAO.updateSeat(seat);

        if (updated) {
            JOptionPane.showMessageDialog(view, "Seat updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
            if (onSeatDataChanged != null) {
                onSeatDataChanged.run();
            }
        } else {
            JOptionPane.showMessageDialog(view, "Failed to update seat. Please check database connection.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteSeat() {
        String idText = view.getSeatIdField().getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please select a seat from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int seatId;
        try {
            seatId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Invalid Seat ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Deletion safety check
        if (seatDAO.hasAssociatedBookings(seatId)) {
            JOptionPane.showMessageDialog(view,
                    "Cannot delete this seat because it is associated with existing booking data.",
                    "Referential Integrity Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to delete this seat?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = seatDAO.deleteSeat(seatId);
            if (deleted) {
                JOptionPane.showMessageDialog(view, "Seat deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshTable();
                if (onSeatDataChanged != null) {
                    onSeatDataChanged.run();
                }
            } else {
                JOptionPane.showMessageDialog(view, "Failed to delete seat. It may be referenced by existing records.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String query = view.getSearchField().getText().trim();
        if (query.isEmpty()) {
            refreshTable();
            return;
        }

        int theatreId = getSelectedTheatreId();
        List<Seat> results = seatDAO.searchSeats(theatreId, query);
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(view, "No seats found matching: \"" + query + "\"", "Search Results", JOptionPane.INFORMATION_MESSAGE);
        }
        populateTable(results);
    }

    private void handleRefresh() {
        view.getSearchField().setText("");
        view.getFilterTypeCombo().setSelectedIndex(0);
        view.getFilterStatusCombo().setSelectedIndex(0);
        clearForm();
        reloadTheatres();
        refreshTable();
    }

    public void clearForm() {
        if (view == null) return;
        view.getSeatIdField().setText("");
        view.getRowField().setText("");
        view.getSeatNumberField().setText("");
        view.getSeatTypeCombo().setSelectedIndex(0);
        view.getStatusCombo().setSelectedIndex(0);
        view.getSeatsTable().clearSelection();
    }
}
