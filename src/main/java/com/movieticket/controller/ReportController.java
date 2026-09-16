package com.movieticket.controller;

import com.movieticket.dao.ReportDAO;
import com.movieticket.model.User;
import com.movieticket.util.UserSession;
import com.movieticket.view.ReportManagementView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for Admin Reports and Analytics Module (Phase 15).
 */
public class ReportController {

    public enum ReportType {
        BOOKING_REPORT,
        MOVIE_REPORT,
        SHOW_REPORT
    }

    private final ReportDAO reportDAO;
    private ReportManagementView view;
    private ReportType currentReportType = ReportType.BOOKING_REPORT;

    public ReportController() {
        this.reportDAO = new ReportDAO();
    }

    public ReportController(ReportDAO reportDAO) {
        this.reportDAO = reportDAO != null ? reportDAO : new ReportDAO();
    }

    public void bindView(ReportManagementView view) {
        this.view = view;
        initListeners();
    }

    private void initListeners() {
        if (view == null) return;

        view.getBtnBookingReport().addActionListener(e -> {
            currentReportType = ReportType.BOOKING_REPORT;
            updateActiveTabButtons();
            loadCurrentReport();
        });

        view.getBtnMovieReport().addActionListener(e -> {
            currentReportType = ReportType.MOVIE_REPORT;
            updateActiveTabButtons();
            loadCurrentReport();
        });

        view.getBtnShowReport().addActionListener(e -> {
            currentReportType = ReportType.SHOW_REPORT;
            updateActiveTabButtons();
            loadCurrentReport();
        });

        view.getBtnRefresh().addActionListener(e -> loadCurrentReport());
    }

    private void updateActiveTabButtons() {
        if (view == null) return;
        view.updateTabStyle(view.getBtnBookingReport(), currentReportType == ReportType.BOOKING_REPORT);
        view.updateTabStyle(view.getBtnMovieReport(), currentReportType == ReportType.MOVIE_REPORT);
        view.updateTabStyle(view.getBtnShowReport(), currentReportType == ReportType.SHOW_REPORT);
    }

    public void loadCurrentReport() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            JOptionPane.showMessageDialog(view, "Access denied. Administrator privileges required.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (view == null) return;

        switch (currentReportType) {
            case BOOKING_REPORT -> loadBookingReport();
            case MOVIE_REPORT -> loadMovieReport();
            case SHOW_REPORT -> loadShowReport();
        }
    }

    private void loadBookingReport() {
        view.getLblReportTitle().setText("Report: All System Bookings & Transactions");
        String[] columns = {
                "Booking ID", "Customer", "Movie Title", "Theatre", "Screen",
                "Show Date", "Show Time", "Seats", "Tickets", "Total Amount", "Booking Date", "Status"
        };

        DefaultTableModel model = view.getTableModel();
        model.setDataVector(new Object[][]{}, columns);

        try {
            List<Object[]> data = reportDAO.getDetailedBookingReport();
            if (data.isEmpty()) {
                view.setEmptyStateVisible(true);
                view.getLblSummaryText().setText("Total Records: 0 | Total Tickets: 0 | Total Revenue: ₹0.00");
                return;
            }

            view.setEmptyStateVisible(false);
            int totalTickets = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;

            for (Object[] row : data) {
                model.addRow(row);
                int tickets = (int) row[8];
                totalTickets += tickets;
                String amtStr = row[9].toString().replace("₹", "").trim();
                try {
                    totalRevenue = totalRevenue.add(new BigDecimal(amtStr));
                } catch (Exception ignored) {}
            }

            view.getLblSummaryText().setText(String.format(
                    "Total Bookings: %d | Total Tickets Sold: %d | Total Confirmed Revenue: ₹%s",
                    data.size(), totalTickets, totalRevenue.setScale(2)
            ));
        } catch (Exception ex) {
            System.err.println("[ReportController] Error loading booking report: " + ex.getMessage());
            JOptionPane.showMessageDialog(view, "Unable to load booking report data.\nPlease check database connection.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMovieReport() {
        view.getLblReportTitle().setText("Report: Movie-Wise Sales & Revenue Summary");
        String[] columns = {
                "Movie Title", "Genre", "Total Bookings", "Tickets Sold", "Total Revenue"
        };

        DefaultTableModel model = view.getTableModel();
        model.setDataVector(new Object[][]{}, columns);

        try {
            List<Object[]> data = reportDAO.getMovieBookingReport();
            if (data.isEmpty()) {
                view.setEmptyStateVisible(true);
                view.getLblSummaryText().setText("Total Movies: 0 | Total Tickets: 0 | Total Revenue: ₹0.00");
                return;
            }

            view.setEmptyStateVisible(false);
            int totalBookings = 0;
            int totalTickets = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;

            for (Object[] row : data) {
                model.addRow(row);
                totalBookings += (int) row[2];
                totalTickets += (int) row[3];
                String amtStr = row[4].toString().replace("₹", "").trim();
                try {
                    totalRevenue = totalRevenue.add(new BigDecimal(amtStr));
                } catch (Exception ignored) {}
            }

            view.getLblSummaryText().setText(String.format(
                    "Active Movies: %d | Total Bookings: %d | Total Tickets: %d | Total Revenue: ₹%s",
                    data.size(), totalBookings, totalTickets, totalRevenue.setScale(2)
            ));
        } catch (Exception ex) {
            System.err.println("[ReportController] Error loading movie report: " + ex.getMessage());
            JOptionPane.showMessageDialog(view, "Unable to load movie report data.\nPlease check database connection.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadShowReport() {
        view.getLblReportTitle().setText("Report: Show-Wise Performance & Attendance");
        String[] columns = {
                "Movie Title", "Theatre", "Screen", "Show Date", "Show Time", "Bookings", "Tickets Sold", "Total Revenue"
        };

        DefaultTableModel model = view.getTableModel();
        model.setDataVector(new Object[][]{}, columns);

        try {
            List<Object[]> data = reportDAO.getShowBookingReport();
            if (data.isEmpty()) {
                view.setEmptyStateVisible(true);
                view.getLblSummaryText().setText("Total Shows: 0 | Total Tickets: 0 | Total Revenue: ₹0.00");
                return;
            }

            view.setEmptyStateVisible(false);
            int totalBookings = 0;
            int totalTickets = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;

            for (Object[] row : data) {
                model.addRow(row);
                totalBookings += (int) row[5];
                totalTickets += (int) row[6];
                String amtStr = row[7].toString().replace("₹", "").trim();
                try {
                    totalRevenue = totalRevenue.add(new BigDecimal(amtStr));
                } catch (Exception ignored) {}
            }

            view.getLblSummaryText().setText(String.format(
                    "Total Shows: %d | Total Bookings: %d | Total Tickets Sold: %d | Total Revenue: ₹%s",
                    data.size(), totalBookings, totalTickets, totalRevenue.setScale(2)
            ));
        } catch (Exception ex) {
            System.err.println("[ReportController] Error loading show report: " + ex.getMessage());
            JOptionPane.showMessageDialog(view, "Unable to load show report data.\nPlease check database connection.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public ReportDAO getReportDAO() {
        return reportDAO;
    }
}
