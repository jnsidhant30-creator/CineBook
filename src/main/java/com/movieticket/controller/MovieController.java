package com.movieticket.controller;

import com.movieticket.dao.MovieDAO;
import com.movieticket.model.Movie;
import com.movieticket.controller.OmdbController;
import com.movieticket.view.MovieListView;
import com.movieticket.view.MovieManagementView;
import com.movieticket.view.OmdbSearchDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for Movie Management Module (Phase 8).
 *
 * Responsibilities:
 * - Validate all movie input fields (Title, Genre, Language, Duration, Rating)
 * - Coordinate CRUD operations with MovieDAO
 * - Manage search and live refresh across database
 * - Check referential integrity before delete (prevent deleting movies with associated shows)
 * - Populate and synchronize JTable and form fields
 * - Ensure separation of concerns (no SQL in UI/Controller, no Swing in DAO/Model)
 */
public class MovieController {

    private final MovieDAO movieDAO;
    private MovieManagementView adminView;
    private MovieListView userView;
    private BookingController bookingController;
    private Runnable onMovieDataChanged;
    private final OmdbController omdbController;
    private final TmdbController tmdbController;

    public MovieController() {
        this.movieDAO       = new MovieDAO();
        this.omdbController = new OmdbController();
        this.tmdbController = new TmdbController();
    }

    public MovieController(MovieDAO movieDAO) {
        this.movieDAO       = movieDAO;
        this.omdbController = new OmdbController();
        this.tmdbController = new TmdbController();
    }

    public void setBookingController(BookingController bookingController) {
        this.bookingController = bookingController;
    }

    public void setOnMovieDataChanged(Runnable onMovieDataChanged) {
        this.onMovieDataChanged = onMovieDataChanged;
    }

    public List<Movie> getMovies() {
        return movieDAO.getAllMovies();
    }

    /**
     * Connects and initializes the Admin Movie Management View.
     */
    public void bindAdminPanel(MovieManagementView view) {
        this.adminView = view;
        refreshAdminTable();
        clearAdminForm();

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
        for (java.awt.event.ActionListener al : view.getSearchButton().getActionListeners()) {
            view.getSearchButton().removeActionListener(al);
        }
        if (view.getRefreshButton() != null) {
            for (java.awt.event.ActionListener al : view.getRefreshButton().getActionListeners()) {
                view.getRefreshButton().removeActionListener(al);
            }
            view.getRefreshButton().addActionListener(e -> refreshAdminTable());
        }

        // Add action listeners
        view.getAddButton().addActionListener(e -> addMovie());
        view.getUpdateButton().addActionListener(e -> updateMovie());
        view.getDeleteButton().addActionListener(e -> deleteMovie());
        view.getClearButton().addActionListener(e -> clearAdminForm());
        view.getSearchButton().addActionListener(e -> searchMoviesAdmin());

        // OMDb: Wire the "Search Online Movies" button
        if (view.getSearchOnlineButton() != null) {
            String apiKey = System.getenv("OMDB_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                // Disable gracefully — offline mode, existing catalog still works
                view.getSearchOnlineButton().setEnabled(false);
                view.getSearchOnlineButton().setToolTipText(
                        "OMDb API key not configured. Set the OMDB_API_KEY environment variable to enable online search.");
            } else {
                // Remove old listeners first
                for (java.awt.event.ActionListener al : view.getSearchOnlineButton().getActionListeners()) {
                    view.getSearchOnlineButton().removeActionListener(al);
                }
                view.getSearchOnlineButton().addActionListener(e -> openOmdbSearchDialog());
            }
        }
        
        // TMDB: Wire the "Search TMDB" button
        if (view.getSearchTmdbButton() != null) {
            for (java.awt.event.ActionListener al : view.getSearchTmdbButton().getActionListeners()) {
                view.getSearchTmdbButton().removeActionListener(al);
            }
            view.getSearchTmdbButton().addActionListener(e -> openTmdbSearchDialog());
        }

        // Table selection listener
        view.getMovieTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = view.getMovieTable().getSelectedRow();
                if (selectedRow != -1) {
                    populateAdminFormFromRow(selectedRow);
                }
            }
        });
    }

    /**
     * Opens the OMDb search dialog as a modal window.
     * After a successful import, refreshes the admin table and notifies data changed.
     */
    private void openOmdbSearchDialog() {
        if (adminView == null) return;
        Window window = SwingUtilities.getWindowAncestor(adminView);
        Frame owner = (window instanceof Frame f) ? f : null;
        OmdbSearchDialog dialog = new OmdbSearchDialog(owner, omdbController, () -> {
            refreshAdminTable();
            notifyDataChanged();
        });
        dialog.setVisible(true);
    }

    /**
     * Opens the TMDB search dialog as a modal window.
     */
    private void openTmdbSearchDialog() {
        if (adminView == null) return;
        Window window = SwingUtilities.getWindowAncestor(adminView);
        Frame owner = (window instanceof Frame f) ? f : null;
        // Requires TmdbSearchAdminDialog to exist in view
        com.movieticket.view.TmdbSearchAdminDialog dialog = new com.movieticket.view.TmdbSearchAdminDialog(owner, tmdbController, () -> {
            refreshAdminTable();
            notifyDataChanged();
        });
        dialog.setVisible(true);
    }

    /**
     * Connects user browsing panel.
     */


    // --- Admin Operations ---

    /**
     * Validates input and adds a new movie record to MySQL.
     */
    public void addMovie() {
        if (adminView == null) return;

        String title = adminView.getTitleField().getText().trim();
        String genre = adminView.getGenreField().getText().trim();
        String language = adminView.getLanguageField().getText().trim();
        String durationText = adminView.getDurationField().getText().trim();
        String ratingText = adminView.getRatingField().getText().trim();

        // 1. Validation Checks
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(adminView, "Movie title is required.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationText);
            if (duration <= 0) {
                JOptionPane.showMessageDialog(adminView, "Duration must be a valid positive number.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(adminView, "Duration must be a valid positive number.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal rating = BigDecimal.ZERO;
        if (!ratingText.isEmpty()) {
            try {
                double r = Double.parseDouble(ratingText);
                if (r < 0.0 || r > 10.0) {
                    JOptionPane.showMessageDialog(adminView, "Rating must be between 0 and 10.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                rating = BigDecimal.valueOf(r);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(adminView, "Rating must be a valid number between 0 and 10.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (genre.isEmpty()) genre = "General";
        if (language.isEmpty()) language = "English";

        // 2. Insert into MySQL via DAO
        Movie movie = new Movie(0, title, genre, duration, language, rating);
        int generatedId = movieDAO.addMovie(movie);

        if (generatedId > 0) {
            JOptionPane.showMessageDialog(adminView, "Movie added successfully with ID: " + generatedId,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearAdminForm();
            refreshAdminTable();
            notifyDataChanged();

            // Broadcast New Movie Notification to all customer accounts
            new NotificationController().broadcastNewMovie(title);
        } else {
            JOptionPane.showMessageDialog(adminView, "Unable to save movie. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Validates input and updates an existing movie record in MySQL.
     */
    public void updateMovie() {
        if (adminView == null) return;

        String idText = adminView.getMovieIdField().getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(adminView, "Please select a movie from the table to update.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int movieId;
        try {
            movieId = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(adminView, "Invalid Movie ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String title = adminView.getTitleField().getText().trim();
        String genre = adminView.getGenreField().getText().trim();
        String language = adminView.getLanguageField().getText().trim();
        String durationText = adminView.getDurationField().getText().trim();
        String ratingText = adminView.getRatingField().getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(adminView, "Movie title is required.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationText);
            if (duration <= 0) {
                JOptionPane.showMessageDialog(adminView, "Duration must be a valid positive number.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(adminView, "Duration must be a valid positive number.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal rating = BigDecimal.ZERO;
        if (!ratingText.isEmpty()) {
            try {
                double r = Double.parseDouble(ratingText);
                if (r < 0.0 || r > 10.0) {
                    JOptionPane.showMessageDialog(adminView, "Rating must be between 0 and 10.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                rating = BigDecimal.valueOf(r);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(adminView, "Rating must be a valid number between 0 and 10.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (genre.isEmpty()) genre = "General";
        if (language.isEmpty()) language = "English";

        Movie movie = new Movie(movieId, title, genre, duration, language, rating);
        boolean updated = movieDAO.updateMovie(movie);

        if (updated) {
            JOptionPane.showMessageDialog(adminView, "Movie updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearAdminForm();
            refreshAdminTable();
            notifyDataChanged();
        } else {
            JOptionPane.showMessageDialog(adminView, "Unable to update movie. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Confirms deletion, verifies foreign key dependencies, and deletes the movie from MySQL.
     */
    public void deleteMovie() {
        if (adminView == null) return;

        String idText = adminView.getMovieIdField().getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(adminView, "Please select a movie from the table to delete.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int movieId;
        try {
            movieId = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(adminView, "Invalid Movie ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirmation Dialog
        int confirm = JOptionPane.showConfirmDialog(adminView,
                "Are you sure you want to delete this movie?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Check foreign key dependency with shows
            if (movieDAO.hasAssociatedShows(movieId)) {
                JOptionPane.showMessageDialog(adminView,
                        "Cannot delete this movie because shows are associated with it.",
                        "Cannot Delete Movie", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean deleted = movieDAO.deleteMovie(movieId);
            if (deleted) {
                JOptionPane.showMessageDialog(adminView, "Movie deleted successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                clearAdminForm();
                refreshAdminTable();
                notifyDataChanged();
            } else {
                JOptionPane.showMessageDialog(adminView, "Unable to delete movie. Please check database connection.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void clearAdminForm() {
        if (adminView == null) return;
        adminView.getMovieIdField().setText("");
        adminView.getTitleField().setText("");
        adminView.getGenreField().setText("");
        adminView.getLanguageField().setText("");
        adminView.getDurationField().setText("");
        adminView.getRatingField().setText("");
        adminView.getMovieTable().clearSelection();
    }

    private void populateAdminFormFromRow(int row) {
        if (adminView == null) return;
        DefaultTableModel model = adminView.getTableModel();
        adminView.getMovieIdField().setText(String.valueOf(model.getValueAt(row, 0)));
        adminView.getTitleField().setText(String.valueOf(model.getValueAt(row, 1)));
        adminView.getGenreField().setText(String.valueOf(model.getValueAt(row, 2)));
        adminView.getDurationField().setText(String.valueOf(model.getValueAt(row, 3)));
        adminView.getLanguageField().setText(String.valueOf(model.getValueAt(row, 4)));
        adminView.getRatingField().setText(String.valueOf(model.getValueAt(row, 5)));
    }

    public void searchMoviesAdmin() {
        if (adminView == null) return;
        String query = adminView.getSearchField().getText().trim();
        if (query.isEmpty()) {
            refreshAdminTable();
            return;
        }

        List<Movie> results = movieDAO.searchMovies(query);
        populateAdminTable(results);

        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(adminView, "No movies found.",
                    "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void refreshAdminTable() {
        if (adminView == null) return;
        adminView.getSearchField().setText("");
        List<Movie> movies = movieDAO.getAllMovies();
        populateAdminTable(movies);
    }

    private void populateAdminTable(List<Movie> movies) {
        if (adminView == null) return;
        DefaultTableModel model = adminView.getTableModel();
        model.setRowCount(0);
        for (Movie m : movies) {
            model.addRow(new Object[]{
                    m.getMovieId(),
                    m.getTitle(),
                    m.getGenre(),
                    m.getDuration(),
                    m.getLanguage(),
                    m.getRating()
            });
        }
    }



    private void notifyDataChanged() {
        if (onMovieDataChanged != null) {
            onMovieDataChanged.run();
        }
    }
}
