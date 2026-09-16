package com.movieticket.controller;

import com.movieticket.dao.ReviewDAO;
import com.movieticket.model.Review;
import com.movieticket.view.AdminDashboardFrame;
import com.movieticket.view.AdminReviewsView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminReviewController {

    private final AdminDashboardFrame dashboard;
    private final AdminReviewsView reviewsView;
    private final ReviewDAO reviewDAO;

    public AdminReviewController(AdminDashboardFrame dashboard) {
        this.dashboard = dashboard;
        this.reviewsView = dashboard.getAdminReviewsView();
        this.reviewDAO = new ReviewDAO();

        initController();
    }

    private void initController() {
        dashboard.setReviewsSidebarAction(e -> {
            dashboard.showCard("Reviews");
            dashboard.setActiveSidebarButton("Reviews");
            loadAllReviews();
        });

        reviewsView.getBtnRefresh().addActionListener(e -> loadAllReviews());
        reviewsView.getBtnHideReview().addActionListener(e -> updateReviewStatus("HIDDEN"));
        reviewsView.getBtnRestoreReview().addActionListener(e -> updateReviewStatus("VISIBLE"));
    }

    public void loadAllReviews() {
        SwingWorker<List<Review>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Review> doInBackground() {
                return reviewDAO.getAllReviewsForAdmin();
            }

            @Override
            protected void done() {
                try {
                    List<Review> reviews = get();
                    DefaultTableModel model = reviewsView.getTableModel();
                    model.setRowCount(0);

                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                    for (Review r : reviews) {
                        String targetMovie = r.getMovieId() != null ? "Movie ID: " + r.getMovieId() : "N/A";
                        String targetTheatre = r.getTheatreId() != null ? "Theatre ID: " + r.getTheatreId() : "N/A";
                        
                        model.addRow(new Object[]{
                                r.getReviewId(),
                                r.getCreatedAt().format(dtf),
                                r.getUsername() != null ? r.getUsername() : "User ID: " + r.getUserId(),
                                targetMovie,
                                targetTheatre,
                                "⭐ " + r.getRating(),
                                r.getReviewText(),
                                r.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    System.err.println("[AdminReviewController] Error loading reviews: " + ex.getMessage());
                    JOptionPane.showMessageDialog(reviewsView, "Failed to load reviews.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateReviewStatus(String newStatus) {
        int selectedRow = reviewsView.getReviewsTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(reviewsView, "Please select a review from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reviewId = (int) reviewsView.getTableModel().getValueAt(selectedRow, 0);
        String currentStatus = (String) reviewsView.getTableModel().getValueAt(selectedRow, 7);

        if (currentStatus.equals(newStatus)) {
            JOptionPane.showMessageDialog(reviewsView, "Review is already marked as " + newStatus + ".", "No Change", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean updated = reviewDAO.updateReviewStatus(reviewId, newStatus);
        if (updated) {
            JOptionPane.showMessageDialog(reviewsView, "Review status updated to " + newStatus + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAllReviews();
        } else {
            JOptionPane.showMessageDialog(reviewsView, "Failed to update review status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
