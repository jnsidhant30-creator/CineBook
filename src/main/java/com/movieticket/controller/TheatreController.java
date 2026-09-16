package com.movieticket.controller;

import com.movieticket.dao.TheatreDAO;
import com.movieticket.model.Theatre;
import com.movieticket.view.TheatreManagementView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Controller for Theatre Management Module (Phase 9).
 *
 * Responsibilities:
 * - Validate all theatre input fields (Name, Location, Capacity/Screens)
 * - Coordinate CRUD operations with TheatreDAO
 * - Manage search and live refresh across database
 * - Check referential integrity before delete (protect against deleting theatres with active shows/seats)
 * - Populate and synchronize JTable and form fields
 * - Ensure separation of concerns (no SQL in UI/Controller, no Swing in DAO/Model)
 */
public class TheatreController {

    private final TheatreDAO theatreDAO;
    private TheatreManagementView view;
    private Runnable onTheatreDataChanged;

    public TheatreController() {
        this.theatreDAO = new TheatreDAO();
    }

    public TheatreController(TheatreDAO theatreDAO) {
        this.theatreDAO = theatreDAO;
    }

    public void setOnTheatreDataChanged(Runnable onTheatreDataChanged) {
        this.onTheatreDataChanged = onTheatreDataChanged;
    }

    public List<Theatre> getTheatres() {
        return theatreDAO.getAllTheatres();
    }

    /**
     * Connects and initializes the Theatre Management View.
     */
    public void bindView(TheatreManagementView view) {
        this.view = view;
        refreshTable();
        clearForm();

        // Clear existing action listeners to avoid duplicates
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
            view.getSearchButton().addActionListener(e -> searchTheatres());
        }
        if (view.getRefreshButton() != null) {
            for (java.awt.event.ActionListener al : view.getRefreshButton().getActionListeners()) {
                view.getRefreshButton().removeActionListener(al);
            }
            view.getRefreshButton().addActionListener(e -> refreshTable());
        }

        // Action Listeners
        view.getAddButton().addActionListener(e -> addTheatre());
        view.getUpdateButton().addActionListener(e -> updateTheatre());
        view.getDeleteButton().addActionListener(e -> deleteTheatre());
        view.getClearButton().addActionListener(e -> clearForm());

        // Mouse click on table row to populate form
        view.getTheatresTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.getTheatresTable().getSelectedRow();
                if (row != -1) {
                    populateFormFromRow(row);
                }
            }
        });
    }

    /**
     * Validates input and adds a new theatre record to MySQL.
     */
    public void addTheatre() {
        if (view == null) return;

        String name = view.getNameField().getText().trim();
        String location = view.getLocationField().getText().trim();
        String capacityText = view.getCapacityField().getText().trim();

        // 1. Validation Checks
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Theatre name is required.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (location.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Theatre location is required.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
            if (capacity <= 0) {
                JOptionPane.showMessageDialog(view, "Number of screens must be a positive number.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Number of screens must be a positive number.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Insert into MySQL via DAO
        Theatre theatre = new Theatre(0, name, location, capacity);
        int generatedId = theatreDAO.addTheatre(theatre);

        if (generatedId > 0) {
            JOptionPane.showMessageDialog(view, "Theatre added successfully with ID: " + generatedId,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
            notifyDataChanged();
        } else {
            JOptionPane.showMessageDialog(view, "Unable to add theatre. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Validates input and updates an existing theatre in MySQL.
     */
    public void updateTheatre() {
        if (view == null) return;

        String idText = view.getTheatreIdField().getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please select a theatre from the table to update.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int theatreId;
        try {
            theatreId = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Invalid Theatre ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = view.getNameField().getText().trim();
        String location = view.getLocationField().getText().trim();
        String capacityText = view.getCapacityField().getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Theatre name is required.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (location.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Theatre location is required.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
            if (capacity <= 0) {
                JOptionPane.showMessageDialog(view, "Number of screens must be a positive number.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Number of screens must be a positive number.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Theatre theatre = new Theatre(theatreId, name, location, capacity);
        boolean updated = theatreDAO.updateTheatre(theatre);

        if (updated) {
            JOptionPane.showMessageDialog(view, "Theatre updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
            notifyDataChanged();
        } else {
            JOptionPane.showMessageDialog(view, "Unable to update theatre. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Confirms deletion, verifies foreign key dependencies, and deletes the theatre from MySQL.
     */
    public void deleteTheatre() {
        if (view == null) return;

        String idText = view.getTheatreIdField().getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please select a theatre from the table to delete.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int theatreId;
        try {
            theatreId = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Invalid Theatre ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirmation Dialog
        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to delete this theatre?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Check foreign key dependencies
            if (theatreDAO.hasAssociatedRecords(theatreId)) {
                JOptionPane.showMessageDialog(view,
                        "Cannot delete this theatre because shows or bookings are associated with it.",
                        "Cannot Delete Theatre", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean deleted = theatreDAO.deleteTheatre(theatreId);
            if (deleted) {
                JOptionPane.showMessageDialog(view, "Theatre deleted successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshTable();
                notifyDataChanged();
            } else {
                JOptionPane.showMessageDialog(view, "Unable to delete theatre. Please check database connection.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void clearForm() {
        if (view == null) return;
        view.getTheatreIdField().setText("");
        view.getNameField().setText("");
        view.getLocationField().setText("");
        view.getCapacityField().setText("");
        view.getTheatresTable().clearSelection();
    }

    private void populateFormFromRow(int row) {
        if (view == null) return;
        DefaultTableModel model = view.getTableModel();
        view.getTheatreIdField().setText(String.valueOf(model.getValueAt(row, 0)));
        view.getNameField().setText(String.valueOf(model.getValueAt(row, 1)));
        view.getLocationField().setText(String.valueOf(model.getValueAt(row, 2)));
        view.getCapacityField().setText(String.valueOf(model.getValueAt(row, 3)));
    }

    public void searchTheatres() {
        if (view == null) return;
        String query = view.getSearchField().getText().trim();
        if (query.isEmpty()) {
            refreshTable();
            return;
        }

        List<Theatre> results = theatreDAO.searchTheatres(query);
        populateTable(results);

        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(view, "No theatres found.",
                    "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void refreshTable() {
        if (view == null) return;
        view.getSearchField().setText("");
        List<Theatre> theatres = theatreDAO.getAllTheatres();
        populateTable(theatres);
    }

    private void populateTable(List<Theatre> theatres) {
        if (view == null) return;
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Theatre t : theatres) {
            model.addRow(new Object[]{
                    t.getTheatreId(),
                    t.getTheatreName(),
                    t.getLocation(),
                    t.getTotalSeats()
            });
        }
    }

    private void notifyDataChanged() {
        if (onTheatreDataChanged != null) {
            onTheatreDataChanged.run();
        }
    }
}
