package com.movieticket.controller;

import com.movieticket.dao.ScreenDAO;
import com.movieticket.dao.TheatreDAO;
import com.movieticket.model.Screen;
import com.movieticket.model.Theatre;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.TheatreScreenManagementView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Controller for TheatreScreenManagementView.
 * Handles CRUD for both Theatres (with city/contact/status) and Screens.
 * Phase 5 — Multi-Theatre & Screen Management.
 */
public class TheatreScreenController {

    private final TheatreDAO theatreDAO;
    private final ScreenDAO screenDAO;
    private TheatreScreenManagementView view;
    private Runnable onDataChanged;

    public TheatreScreenController(TheatreDAO theatreDAO, ScreenDAO screenDAO) {
        this.theatreDAO = theatreDAO;
        this.screenDAO = screenDAO;
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void bindView(TheatreScreenManagementView view) {
        this.view = view;

        // Init Theatre tab
        refreshTheatresTable();
        clearTheatreForm();
        bindTheatreEvents();

        // Init Screens tab
        loadScreenTheatreCombo();
        refreshScreensTable();
        clearScreenForm();
        bindScreenEvents();
    }

    // ─── Theatre Tab ─────────────────────────────────────────────────────────

    private void bindTheatreEvents() {
        view.getAddTheatreBtn().addActionListener(e -> addTheatre());
        view.getUpdateTheatreBtn().addActionListener(e -> updateTheatre());
        view.getDeleteTheatreBtn().addActionListener(e -> deleteTheatre());
        view.getClearTheatreBtn().addActionListener(e -> clearTheatreForm());
        view.getTheatreSearchBtn().addActionListener(e -> searchTheatres());
        view.getTheatreRefreshBtn().addActionListener(e -> { clearTheatreForm(); refreshTheatresTable(); });

        view.getTheatresTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.getTheatresTable().getSelectedRow();
                if (row >= 0) populateTheatreForm(row);
            }
        });
    }

    private void addTheatre() {
        Theatre t = buildTheatreFromForm();
        if (t == null) return;
        int id = theatreDAO.addTheatre(t);
        if (id > 0) {
            ThemeManager.showInfo(null, "Theatre added successfully! (ID: " + id + ")", "Success");
            clearTheatreForm();
            refreshTheatresTable();
            loadScreenTheatreCombo();
            if (onDataChanged != null) onDataChanged.run();
        } else {
            ThemeManager.showError(null, "Failed to add theatre. Please check the fields.", "Error");
        }
    }

    private void updateTheatre() {
        String idStr = view.getTheatreIdField().getText().trim();
        if (idStr.isEmpty()) {
            ThemeManager.showWarning(null, "Please select a theatre from the table to update.", "No Selection");
            return;
        }
        Theatre t = buildTheatreFromForm();
        if (t == null) return;
        t.setTheatreId(Integer.parseInt(idStr));
        if (theatreDAO.updateTheatre(t)) {
            ThemeManager.showInfo(null, "Theatre updated successfully.", "Success");
            clearTheatreForm();
            refreshTheatresTable();
            if (onDataChanged != null) onDataChanged.run();
        } else {
            ThemeManager.showError(null, "Failed to update theatre.", "Error");
        }
    }

    private void deleteTheatre() {
        String idStr = view.getTheatreIdField().getText().trim();
        if (idStr.isEmpty()) {
            ThemeManager.showWarning(null, "Please select a theatre to delete.", "No Selection");
            return;
        }
        int id = Integer.parseInt(idStr);
        if (theatreDAO.hasAssociatedRecords(id)) {
            ThemeManager.showError(null, "Cannot delete — this theatre has associated shows, seats, or bookings.", "Cannot Delete");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(null,
                "Delete Theatre #" + id + "? This cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (theatreDAO.deleteTheatre(id)) {
                ThemeManager.showInfo(null, "Theatre deleted.", "Deleted");
                clearTheatreForm();
                refreshTheatresTable();
                loadScreenTheatreCombo();
                if (onDataChanged != null) onDataChanged.run();
            } else {
                ThemeManager.showError(null, "Failed to delete theatre.", "Error");
            }
        }
    }

    private Theatre buildTheatreFromForm() {
        String name = view.getTheatreNameField().getText().trim();
        String location = view.getTheatreLocationField().getText().trim();
        String city = view.getTheatreCityField().getText().trim();
        String contact = view.getTheatreContactField().getText().trim();
        String capacityStr = view.getTheatreCapacityField().getText().trim();
        String status = (String) view.getTheatreStatusCombo().getSelectedItem();

        if (name.isEmpty()) { ThemeManager.showWarning(null, "Theatre Name is required.", "Validation"); return null; }
        if (location.isEmpty()) { ThemeManager.showWarning(null, "Location is required.", "Validation"); return null; }
        if (city.isEmpty()) { ThemeManager.showWarning(null, "City is required.", "Validation"); return null; }

        int capacity = 100;
        try {
            if (!capacityStr.isEmpty()) capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            ThemeManager.showWarning(null, "Total Seats must be a positive number.", "Validation");
            return null;
        }

        Theatre t = new Theatre();
        t.setTheatreName(name);
        t.setLocation(location);
        t.setCity(city);
        t.setContactNumber(contact.isEmpty() ? null : contact);
        t.setTotalSeats(capacity);
        t.setStatus(status);
        return t;
    }

    private void populateTheatreForm(int row) {
        DefaultTableModel m = view.getTheatresTableModel();
        view.getTheatreIdField().setText(m.getValueAt(row, 0).toString());
        view.getTheatreNameField().setText(m.getValueAt(row, 1).toString());
        view.getTheatreLocationField().setText(m.getValueAt(row, 2).toString());
        view.getTheatreCityField().setText(m.getValueAt(row, 3).toString());
        Object contact = m.getValueAt(row, 4);
        view.getTheatreContactField().setText(contact != null ? contact.toString() : "");
        view.getTheatreCapacityField().setText(m.getValueAt(row, 5).toString());
        Object status = m.getValueAt(row, 7);
        if (status != null) view.getTheatreStatusCombo().setSelectedItem(status.toString());
    }

    private void clearTheatreForm() {
        view.getTheatreIdField().setText("");
        view.getTheatreNameField().setText("");
        view.getTheatreLocationField().setText("");
        view.getTheatreCityField().setText("");
        view.getTheatreContactField().setText("");
        view.getTheatreCapacityField().setText("");
        view.getTheatreStatusCombo().setSelectedIndex(0);
        view.getTheatreSearchField().setText("");
    }

    public void refreshTheatresTable() {
        DefaultTableModel m = view.getTheatresTableModel();
        m.setRowCount(0);
        for (Theatre t : theatreDAO.getAllTheatres()) {
            m.addRow(new Object[]{
                t.getTheatreId(),
                t.getTheatreName(),
                t.getLocation(),
                t.getCity() != null ? t.getCity() : "",
                t.getContactNumber() != null ? t.getContactNumber() : "",
                t.getTotalSeats(),
                t.getNumScreens(),
                t.getStatus() != null ? t.getStatus() : "ACTIVE"
            });
        }
    }

    private void searchTheatres() {
        String keyword = view.getTheatreSearchField().getText().trim();
        DefaultTableModel m = view.getTheatresTableModel();
        m.setRowCount(0);
        for (Theatre t : theatreDAO.searchTheatres(keyword)) {
            m.addRow(new Object[]{
                t.getTheatreId(), t.getTheatreName(), t.getLocation(),
                t.getCity() != null ? t.getCity() : "",
                t.getContactNumber() != null ? t.getContactNumber() : "",
                t.getTotalSeats(), t.getNumScreens(),
                t.getStatus() != null ? t.getStatus() : "ACTIVE"
            });
        }
    }

    // ─── Screen Tab ──────────────────────────────────────────────────────────

    private void bindScreenEvents() {
        view.getAddScreenBtn().addActionListener(e -> addScreen());
        view.getUpdateScreenBtn().addActionListener(e -> updateScreen());
        view.getDeleteScreenBtn().addActionListener(e -> deleteScreen());
        view.getClearScreenBtn().addActionListener(e -> clearScreenForm());
        view.getScreenRefreshBtn().addActionListener(e -> { clearScreenForm(); refreshScreensTable(); });

        view.getScreensTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.getScreensTable().getSelectedRow();
                if (row >= 0) populateScreenForm(row);
            }
        });
    }

    private void loadScreenTheatreCombo() {
        JComboBox<String> combo = view.getScreenTheatreCombo();
        combo.removeAllItems();
        combo.addItem("— Select Theatre —");
        for (Theatre t : theatreDAO.getAllTheatres()) {
            combo.addItem(t.getTheatreId() + " - " + t.getTheatreName());
        }
    }

    private void addScreen() {
        Screen s = buildScreenFromForm();
        if (s == null) return;
        int id = screenDAO.addScreen(s);
        if (id > 0) {
            ThemeManager.showInfo(null, "Screen added successfully! (ID: " + id + ")", "Success");
            clearScreenForm();
            refreshScreensTable();
            if (onDataChanged != null) onDataChanged.run();
        } else {
            ThemeManager.showError(null, "Failed to add screen.", "Error");
        }
    }

    private void updateScreen() {
        String idStr = view.getScreenIdField().getText().trim();
        if (idStr.isEmpty()) {
            ThemeManager.showWarning(null, "Please select a screen from the table to update.", "No Selection");
            return;
        }
        Screen s = buildScreenFromForm();
        if (s == null) return;
        s.setScreenId(Integer.parseInt(idStr));
        if (screenDAO.updateScreen(s)) {
            ThemeManager.showInfo(null, "Screen updated successfully.", "Success");
            clearScreenForm();
            refreshScreensTable();
            if (onDataChanged != null) onDataChanged.run();
        } else {
            ThemeManager.showError(null, "Failed to update screen.", "Error");
        }
    }

    private void deleteScreen() {
        String idStr = view.getScreenIdField().getText().trim();
        if (idStr.isEmpty()) {
            ThemeManager.showWarning(null, "Please select a screen to delete.", "No Selection");
            return;
        }
        int id = Integer.parseInt(idStr);
        if (screenDAO.hasAssociatedShows(id)) {
            ThemeManager.showError(null, "Cannot delete — this screen has associated shows.", "Cannot Delete");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(null,
                "Delete Screen #" + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (screenDAO.deleteScreen(id)) {
                ThemeManager.showInfo(null, "Screen deleted.", "Deleted");
                clearScreenForm();
                refreshScreensTable();
                if (onDataChanged != null) onDataChanged.run();
            } else {
                ThemeManager.showError(null, "Failed to delete screen.", "Error");
            }
        }
    }

    private Screen buildScreenFromForm() {
        String theatreStr = (String) view.getScreenTheatreCombo().getSelectedItem();
        if (theatreStr == null || theatreStr.startsWith("—")) {
            ThemeManager.showWarning(null, "Please select a theatre for this screen.", "Validation");
            return null;
        }
        int theatreId = Integer.parseInt(theatreStr.split(" - ")[0].trim());

        String name = view.getScreenNameField().getText().trim();
        if (name.isEmpty()) { ThemeManager.showWarning(null, "Screen Name is required.", "Validation"); return null; }

        String type = (String) view.getScreenTypeCombo().getSelectedItem();
        String capStr = view.getScreenCapacityField().getText().trim();
        String status = (String) view.getScreenStatusCombo().getSelectedItem();

        int capacity = 100;
        try {
            if (!capStr.isEmpty()) capacity = Integer.parseInt(capStr);
            if (capacity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            ThemeManager.showWarning(null, "Capacity must be a positive number.", "Validation");
            return null;
        }

        return new Screen(0, theatreId, name, type, capacity, status);
    }

    private void populateScreenForm(int row) {
        DefaultTableModel m = view.getScreensTableModel();
        view.getScreenIdField().setText(m.getValueAt(row, 0).toString());
        view.getScreenNameField().setText(m.getValueAt(row, 2).toString());
        view.getScreenTypeCombo().setSelectedItem(m.getValueAt(row, 3).toString());
        view.getScreenCapacityField().setText(m.getValueAt(row, 4).toString());
        Object status = m.getValueAt(row, 5);
        if (status != null) view.getScreenStatusCombo().setSelectedItem(status.toString());
        // Theatre combo
        String theatreName = m.getValueAt(row, 1).toString();
        for (int i = 0; i < view.getScreenTheatreCombo().getItemCount(); i++) {
            String item = view.getScreenTheatreCombo().getItemAt(i);
            if (item != null && item.contains(theatreName)) {
                view.getScreenTheatreCombo().setSelectedIndex(i);
                break;
            }
        }
    }

    private void clearScreenForm() {
        view.getScreenIdField().setText("");
        view.getScreenNameField().setText("");
        view.getScreenCapacityField().setText("");
        if (view.getScreenTheatreCombo().getItemCount() > 0) view.getScreenTheatreCombo().setSelectedIndex(0);
        view.getScreenTypeCombo().setSelectedIndex(0);
        view.getScreenStatusCombo().setSelectedIndex(0);
    }

    public void refreshScreensTable() {
        DefaultTableModel m = view.getScreensTableModel();
        m.setRowCount(0);
        for (Screen sc : screenDAO.getAllScreens()) {
            // Resolve theatre name
            String theatreName = "Theatre #" + sc.getTheatreId();
            try {
                var opt = theatreDAO.getTheatreById(sc.getTheatreId());
                if (opt.isPresent()) theatreName = opt.get().getTheatreName();
            } catch (Exception ignored) {}
            m.addRow(new Object[]{
                sc.getScreenId(),
                theatreName,
                sc.getScreenName(),
                sc.getScreenType(),
                sc.getCapacity(),
                sc.getStatus()
            });
        }
    }
}
