package com.movieticket.controller;

import com.movieticket.dao.BookingDAO;
import com.movieticket.dao.ReviewDAO;
import com.movieticket.dao.ShowDAO;
import com.movieticket.model.Booking;
import com.movieticket.model.Review;
import com.movieticket.model.Show;
import com.movieticket.model.User;
import com.movieticket.util.UserSession;
import com.movieticket.view.SubmitReviewDialog;
import com.movieticket.view.ViewReviewsDialog;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReviewController {

    private final ReviewDAO reviewDAO;
    private final BookingDAO bookingDAO;
    private final ShowDAO showDAO;

    public ReviewController(ReviewDAO reviewDAO, BookingDAO bookingDAO, ShowDAO showDAO) {
        this.reviewDAO = reviewDAO;
        this.bookingDAO = bookingDAO;
        this.showDAO = showDAO;
    }

    public boolean canUserReview(int userId, Booking booking) {
        if (!booking.getStatus().equals("CONFIRMED")) return false;
        
        Optional<Show> optShow = showDAO.getShowById(booking.getShowId());
        if (optShow.isPresent()) {
            Show show = optShow.get();
            LocalDateTime showEndDateTime = show.getShowDate().atTime(show.getEndTime() != null ? show.getEndTime() : show.getShowTime().plusHours(3));
            if (LocalDateTime.now().isAfter(showEndDateTime)) {
                return true;
            }
        }
        return false;
    }

    public void openSubmitReviewDialog(Window owner, Booking booking, Integer movieId, Integer theatreId, String targetName) {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        boolean isMovie = movieId != null;
        if (reviewDAO.hasUserReviewedBooking(currentUser.getUserId(), booking.getBookingId(), isMovie)) {
            JOptionPane.showMessageDialog(owner, "You have already reviewed this " + (isMovie ? "movie" : "theatre") + " for this booking.", "Already Reviewed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SubmitReviewDialog dialog = new SubmitReviewDialog(owner, targetName);
        dialog.getSubmitButton().addActionListener(e -> {
            int rating = dialog.getSelectedRating();
            if (rating < 1 || rating > 5) {
                JOptionPane.showMessageDialog(dialog, "Please select a star rating (1-5).", "Rating Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String text = dialog.getReviewText();
            if (text != null && text.length() > 500) {
                JOptionPane.showMessageDialog(dialog, "Review text cannot exceed 500 characters.", "Text Too Long", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Review review = new Review();
            review.setUserId(currentUser.getUserId());
            review.setBookingId(booking.getBookingId());
            review.setMovieId(movieId);
            review.setTheatreId(theatreId);
            review.setRating(rating);
            review.setReviewText(text);

            if (reviewDAO.addReview(review)) {
                JOptionPane.showMessageDialog(dialog, "Thank you for your review!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to submit review. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.setVisible(true);
    }

    public void openViewReviewsDialog(Window owner, int targetId, boolean isMovie, String targetName) {
        ViewReviewsDialog dialog = new ViewReviewsDialog(owner, targetName);
        loadMoreReviews(dialog, targetId, isMovie);
        
        dialog.getBtnLoadMore().addActionListener(e -> loadMoreReviews(dialog, targetId, isMovie));
        dialog.setVisible(true);
    }

    private void loadMoreReviews(ViewReviewsDialog dialog, int targetId, boolean isMovie) {
        SwingWorker<List<Review>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Review> doInBackground() {
                if (isMovie) {
                    return reviewDAO.getVisibleReviewsForMovie(targetId, dialog.getCurrentOffset(), dialog.getLimit());
                } else {
                    return reviewDAO.getVisibleReviewsForTheatre(targetId, dialog.getCurrentOffset(), dialog.getLimit());
                }
            }

            @Override
            protected void done() {
                try {
                    dialog.addReviews(get());
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Failed to load reviews.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public double getAverageRating(boolean isMovie, int targetId) {
        return reviewDAO.getAverageRating(isMovie ? "movie_id" : "theatre_id", targetId);
    }
    
    public int getRatingCount(boolean isMovie, int targetId) {
        return reviewDAO.getRatingCount(isMovie ? "movie_id" : "theatre_id", targetId);
    }
}
