package com.movieticket.controller;

import com.movieticket.dao.MovieDAO;
import com.movieticket.dao.UpcomingMovieDAO;
import com.movieticket.model.Movie;
import com.movieticket.model.OmdbMovie;
import com.movieticket.model.UpcomingMovie;
import com.movieticket.util.MovieMonitoringService;
import com.movieticket.util.OmdbService;
import com.movieticket.view.AdminDashboardFrame;
import com.movieticket.view.MovieManagementView;
import com.movieticket.view.UpcomingMoviesView;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UpcomingMoviesController {

    private final UpcomingMoviesView view;
    private final AdminDashboardFrame dashboard;
    private final MovieDAO movieDAO;
    private final UpcomingMovieDAO upcomingMovieDAO;
    private final MovieMonitoringService monitoringService;
    private final OmdbService omdbService;
    private final MovieManagementView movieManagementView;

    private List<UpcomingMovie> currentUpcoming;
    private List<Movie> currentRecent;

    public UpcomingMoviesController(UpcomingMoviesView view, 
                                    AdminDashboardFrame dashboard,
                                    MovieDAO movieDAO, 
                                    UpcomingMovieDAO upcomingMovieDAO,
                                    MovieMonitoringService monitoringService,
                                    OmdbService omdbService,
                                    MovieManagementView movieManagementView) {
        this.view = view;
        this.dashboard = dashboard;
        this.movieDAO = movieDAO;
        this.upcomingMovieDAO = upcomingMovieDAO;
        this.monitoringService = monitoringService;
        this.omdbService = omdbService;
        this.movieManagementView = movieManagementView;

        initListeners();
        refreshData();
    }

    private void initListeners() {
        view.getCheckNewMoviesBtn().addActionListener(e -> performManualCheck());
        
        view.getAddToCineBookBtn().addActionListener(e -> {
            UpcomingMovie selected = view.getSelectedUpcomingMovie(currentUpcoming);
            if (selected != null) {
                addToCineBook(selected);
            } else {
                JOptionPane.showMessageDialog(view, "Please select an upcoming movie first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        view.getIgnoreBtn().addActionListener(e -> {
            UpcomingMovie selected = view.getSelectedUpcomingMovie(currentUpcoming);
            if (selected != null) {
                ignoreMovie(selected);
            } else {
                JOptionPane.showMessageDialog(view, "Please select an upcoming movie first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        view.getViewDetailsBtn().addActionListener(e -> {
            UpcomingMovie selected = view.getSelectedUpcomingMovie(currentUpcoming);
            if (selected != null) {
                viewMovieDetails(selected);
            } else {
                JOptionPane.showMessageDialog(view, "Please select an upcoming movie first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void performManualCheck() {
        view.getCheckNewMoviesBtn().setEnabled(false);
        view.getCheckNewMoviesBtn().setText("Checking...");

        SwingWorker<MovieMonitoringService.CheckResult, Void> worker = new SwingWorker<>() {
            @Override
            protected MovieMonitoringService.CheckResult doInBackground() throws Exception {
                return monitoringService.checkForNewMovies();
            }

            @Override
            protected void done() {
                try {
                    MovieMonitoringService.CheckResult result = get();
                    JOptionPane.showMessageDialog(view, 
                        "Movie Check Completed\n\nNew Found: " + result.newFound + 
                        "\nAlready Known: " + result.alreadyKnown + 
                        "\nFailed: " + result.failed, 
                        "Discovery Status", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(view, "Error during movie check: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    view.getCheckNewMoviesBtn().setEnabled(true);
                    view.getCheckNewMoviesBtn().setText("Check for New Movies");
                }
            }
        };
        worker.execute();
    }

    private void refreshData() {
        // Fetch ALL upcoming movies regardless of time (for the table)
        currentUpcoming = upcomingMovieDAO.getAllUpcomingMovies();
        
        // Remove IGNORED and APPROVED from the standard view list or keep them?
        // Let's filter to only NEW or VIEWED for display in Upcoming Table
        List<UpcomingMovie> activeUpcoming = currentUpcoming.stream()
                .filter(m -> "NEW".equals(m.getStatus()) || "VIEWED".equals(m.getStatus()))
                .toList();

        view.populateUpcomingTable(activeUpcoming);

        // Fetch recent movies (added in the last 2 months) from main Movie table
        LocalDate today = LocalDate.now();
        LocalDate twoMonthsAgo = today.minusMonths(2);
        
        List<Movie> allMovies = movieDAO.getAllMovies();
        currentRecent = allMovies.stream()
                .filter(m -> m.getReleaseDate() != null)
                .filter(m -> {
                    LocalDate rd = m.getReleaseDate().toLocalDate();
                    return (rd.isEqual(twoMonthsAgo) || rd.isAfter(twoMonthsAgo)) && (rd.isEqual(today) || rd.isBefore(today));
                })
                .toList();
                
        view.populateRecentTable(currentRecent);

        // Update Dashboard Summary
        long newCount = activeUpcoming.stream().filter(m -> "NEW".equals(m.getStatus())).count();
        dashboard.getLblNewUpcoming().setText("New: " + newCount);
        dashboard.getLblTotalUpcoming().setText("Upcoming: " + activeUpcoming.size());
        dashboard.getLblTotalRecent().setText("Recent: " + currentRecent.size());
        
        if (monitoringService.getLastCheckTime() != null) {
            view.getLastCheckLabel().setText("Last Check: " + monitoringService.getLastCheckTime().toString());
            // Since we schedule every 24 hours
            view.getNextCheckLabel().setText("Next Auto Check: ~24hrs from last");
        }
    }

    private void ignoreMovie(UpcomingMovie movie) {
        int confirm = JOptionPane.showConfirmDialog(view, "Ignore '" + movie.getTitle() + "'?\nIt won't be shown here anymore.", "Ignore Movie", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            upcomingMovieDAO.updateStatus(movie.getId(), "IGNORED");
            refreshData();
        }
    }

    private void viewMovieDetails(UpcomingMovie movie) {
        if ("NEW".equals(movie.getStatus())) {
            upcomingMovieDAO.updateStatus(movie.getId(), "VIEWED");
            movie.setStatus("VIEWED");
            refreshData();
        }
        
        if (movie.getImdbId() == null || movie.getImdbId().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Title: " + movie.getTitle() + "\nRelease: " + movie.getReleaseDate() + "\nGenre: " + movie.getGenre() + "\n(No IMDb ID available for full details)");
            return;
        }

        view.getViewDetailsBtn().setEnabled(false);
        view.getViewDetailsBtn().setText("Loading...");

        SwingWorker<OmdbMovie, Void> worker = new SwingWorker<>() {
            @Override
            protected OmdbMovie doInBackground() throws Exception {
                return omdbService.getMovieDetails(movie.getImdbId());
            }

            @Override
            protected void done() {
                try {
                    OmdbMovie detail = get();
                    if (detail != null) {
                        JOptionPane.showMessageDialog(view,
                                "Title: " + detail.getTitle() + "\n" +
                                "Genre: " + detail.getGenre() + "\n" +
                                "Director: " + detail.getDirector() + "\n" +
                                "Actors: " + detail.getActors() + "\n" +
                                "Plot: " + detail.getPlot() + "\n" +
                                "IMDb Rating: " + detail.getImdbRating()
                                , "Movie Details", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(view, "Could not fetch details from OMDb.", "Error", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    view.getViewDetailsBtn().setEnabled(true);
                    view.getViewDetailsBtn().setText("View Details");
                }
            }
        };
        worker.execute();
    }

    private void addToCineBook(UpcomingMovie movie) {
        int confirm = JOptionPane.showConfirmDialog(view, "Add '" + movie.getTitle() + "' to CineBook?\nThis will fetch full details from OMDb.", "Add to CineBook", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        view.getAddToCineBookBtn().setEnabled(false);
        view.getAddToCineBookBtn().setText("Adding...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // 1. Fetch full details if we have IMDb ID
                String genre = movie.getGenre() != null ? movie.getGenre() : "Unknown";
                String lang = "English";
                int duration = 120;
                BigDecimal rating = new BigDecimal("0.0");
                String poster = movie.getPosterUrl();
                
                if (movie.getImdbId() != null && !movie.getImdbId().isEmpty()) {
                    OmdbMovie detail = omdbService.getMovieDetails(movie.getImdbId());
                    if (detail != null) {
                        genre = detail.getGenre();
                        lang = detail.getLanguage();
                        if (detail.getRuntime() != null && detail.getRuntime().contains("min")) {
                            try {
                                duration = Integer.parseInt(detail.getRuntime().replace(" min", "").trim());
                            } catch (Exception ignored) {}
                        }
                        if (detail.getImdbRating() != null && !detail.getImdbRating().equalsIgnoreCase("N/A")) {
                            try {
                                rating = new BigDecimal(detail.getImdbRating());
                            } catch (Exception ignored) {}
                        }
                        poster = detail.getPosterUrl();
                    }
                }

                // 2. Map to main Movie object
                Movie newMainMovie = new Movie(0, movie.getTitle(), genre, duration, lang, rating, movie.getReleaseDate());
                newMainMovie.setImdbId(movie.getImdbId());
                newMainMovie.setPosterUrl(poster);

                // 3. Insert into DB
                int resultId = movieDAO.addMovie(newMainMovie);
                boolean success = (resultId > 0);
                if (success) {
                    upcomingMovieDAO.updateStatus(movie.getId(), "APPROVED");
                }
                return success;
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(view, "Movie successfully added to CineBook!\nYou can now schedule shows for it.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Notify Admin Controller / Movie View to refresh its list
                        // A quick hack is to re-click the search/refresh in MovieManagementView if accessible, or just reload data
                        // (Assuming MovieManagementController will reload when the tab is switched, or we trigger it)
                    } else {
                        JOptionPane.showMessageDialog(view, "Failed to add movie to CineBook (might be a duplicate).", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    refreshData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    view.getAddToCineBookBtn().setEnabled(true);
                    view.getAddToCineBookBtn().setText("Add to CineBook");
                }
            }
        };
        worker.execute();
    }
}
