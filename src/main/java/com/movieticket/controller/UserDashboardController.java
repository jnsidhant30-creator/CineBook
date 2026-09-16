package com.movieticket.controller;

import com.movieticket.dao.MovieDAO;
import com.movieticket.dao.NotifyMeDAO;
import com.movieticket.dao.ShowDAO;
import com.movieticket.dao.TheatreDAO;
import com.movieticket.dao.UserDAO;
import com.movieticket.model.Movie;
import com.movieticket.model.Show;
import com.movieticket.model.Theatre;
import com.movieticket.model.User;
import com.movieticket.util.UserSession;
import com.movieticket.view.MovieListView;
import com.movieticket.view.UserDashboardFrame;
import com.movieticket.view.MovieDetailsView;
import com.movieticket.view.ShowSelectionView;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.*;

/**
 * Controller for User Dashboard & Movie Browsing Module (Phase 12).
 *
 * Responsibilities:
 * - Role-based authorization verification (USER or logged-in accounts)
 * - Browsing and searching active movies from MySQL via MovieDAO
 * - Filtering movies by Genre
 * - Displaying selected movie metadata (Title, Genre, Duration, Language, Rating, Description)
 * - Loading available theatres and shows for the selected movie via ShowDAO and TheatreDAO
 * - Filtering available showtimes by Theatre and Date
 * - Handling empty states and database connection exceptions
 * - Safe session termination & logout workflow
 */
public class UserDashboardController {

    private final UserDashboardFrame view;
    private final MovieDAO movieDAO;
    private final TheatreDAO theatreDAO;
    private final ShowDAO showDAO;
    private final com.movieticket.dao.WatchlistDAO watchlistDAO;
    private final com.movieticket.dao.FavoriteDAO favoriteDAO;
    private final com.movieticket.dao.RecentlyViewedDAO recentlyViewedDAO;
    private final NotifyMeDAO notifyMeDAO;
    private final BookingController bookingController;
    private final NotificationController notificationController;
    private final ReviewController reviewController;
    private final com.movieticket.util.RecommendationService recommendationService;
    private final com.movieticket.util.CinePointsService cinePointsService;
    private final TmdbController tmdbController;

    private List<Movie> cachedMovies = new ArrayList<>();
    private final Map<Integer, Theatre> theatreMap = new HashMap<>();
    private List<Show> currentMovieShows = new ArrayList<>();
    private Movie selectedMovie = null;

    public UserDashboardController(UserDashboardFrame view) {
        this.view = view;
        this.movieDAO = new MovieDAO();
        this.theatreDAO = new TheatreDAO();
        this.showDAO = new ShowDAO();
        this.watchlistDAO = new com.movieticket.dao.WatchlistDAO();
        this.favoriteDAO = new com.movieticket.dao.FavoriteDAO();
        this.recentlyViewedDAO = new com.movieticket.dao.RecentlyViewedDAO();
        this.notifyMeDAO = new NotifyMeDAO();
        this.bookingController = new BookingController();
        this.notificationController = new NotificationController();
        this.reviewController = new ReviewController(new com.movieticket.dao.ReviewDAO(), new com.movieticket.dao.BookingDAO(), showDAO);
        this.recommendationService = new com.movieticket.util.RecommendationService();
        this.cinePointsService = new com.movieticket.util.CinePointsService();
        this.tmdbController = new TmdbController();

        checkUserAccess();
        
        // Background heavy initializations
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                initTheatreCache();
                return null;
            }
            @Override
            protected void done() {
                checkAndShowPreferences(); // This uses its own worker
                initViewListeners();
                bookingController.bindUserWorkflow(view);
                loadMovies();
                updateNotificationBadge();
            }
        }.execute();
    }

    private void checkUserAccess() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(view,
                    "Please log in to access the Movie Browsing portal.",
                    "Authentication Required", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void checkAndShowPreferences() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && "CUSTOMER".equalsIgnoreCase(currentUser.getRole())) {
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    com.movieticket.dao.RecommendationDAO recDao = new com.movieticket.dao.RecommendationDAO();
                    return recDao.getUserPreference(currentUser.getUserId()) == null;
                }
                @Override
                protected void done() {
                    try {
                        if (get()) {
                            com.movieticket.view.PreferencesDialog dialog = new com.movieticket.view.PreferencesDialog(view, currentUser.getUserId());
                            dialog.setVisible(true);
                            if (dialog.getResultPreference() != null) {
                                new SwingWorker<Void, Void>() {
                                    @Override
                                    protected Void doInBackground() {
                                        new com.movieticket.dao.RecommendationDAO().saveUserPreference(dialog.getResultPreference());
                                        return null;
                                    }
                                }.execute();
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }.execute();
        }
    }

    private void initTheatreCache() {
        try {
            List<Theatre> theatres = theatreDAO.getAllTheatres();
            theatreMap.clear();
            for (Theatre t : theatres) {
                theatreMap.put(t.getTheatreId(), t);
            }
        } catch (Exception e) {
            System.err.println("[UserDashboardController] Error caching theatres: " + e.getMessage());
        }
    }

    private void initViewListeners() {
        // Welcome message
        if (UserSession.getInstance().getCurrentUser() != null) {
            view.setWelcomeMessage(UserSession.getInstance().getCurrentUser().getUsername());
        }

        // Notification Bell Listener
        view.getNotificationBellButton().addActionListener(e -> handleNotificationBellClick());

        // Sidebar Navigation Listeners
        view.setBrowseMoviesAction(e -> {
            loadMovies();
            updateNotificationBadge();
            view.showCard("Browse Movies");
        });
        view.setRecommendationsAction(e -> {
            updateNotificationBadge();
            view.showCard("Recommendations");
            loadRecommendations();
        });
        view.setMyBookingsAction(e -> {
            bookingController.loadUserBookingHistory();
            updateNotificationBadge();
            view.showCard("My Bookings");
        });
        view.setWatchlistAction(e -> {
            User current = UserSession.getInstance().getCurrentUser();
            if (current != null) {
                view.getWatchlistView().loadWatchlist(current.getUserId());
            }
            updateNotificationBadge();
            view.showCard("My Watchlist");
        });
        view.setCinePointsAction(e -> {
            User current = UserSession.getInstance().getCurrentUser();
            if (current != null) {
                int balance = cinePointsService.getBalance(current.getUserId());
                java.util.List<com.movieticket.model.CinePointsTransaction> history = cinePointsService.getTransactionHistory(current.getUserId());
                view.getCinePointsWalletView().updateWallet(balance, history);
            }
            updateNotificationBadge();
            view.showCard("CinePoints");
        });
        view.setLogoutAction(e -> handleLogout());
        view.setProfileAction(e -> handleProfileClick());
        view.setNotificationsAction(e -> handleNotificationBellClick());

        MovieListView movieListView = view.getMovieListView();
        MovieDetailsView detailsView = view.getMovieDetailsView();

                // Hero Section Buttons
        view.getMovieListView().getBtnWatchTrailer().addActionListener(e -> handleWatchTrailer());
        view.getMovieListView().getBtnBookTickets().addActionListener(e -> {
            if (selectedMovie != null) {
                view.getShowSelectionView().getMovieTitleLabel().setText(selectedMovie.getTitle());
                loadShowsForMovie(selectedMovie.getMovieId());
                view.showCard("Select Show");
            }
        });
        view.getMovieListView().getBtnFavorite().addActionListener(e -> handleFavoriteToggle());

        // Search & Refresh
        view.getHeaderSearchField().addActionListener(e -> handleMovieSearch());
        movieListView.getRefreshButton().addActionListener(e -> handleRefresh());

        // Genre & Language Filters
        movieListView.getGenreFilterCombo().addActionListener(e -> applyGenreFilter());
        movieListView.getLanguageFilterCombo().addActionListener(e -> applyGenreFilter());

        // City, Theatre & Date Filters (Now on ShowSelectionView)
        ShowSelectionView showView = view.getShowSelectionView();
        showView.getCityCombo().addActionListener(e -> filterShowsTable());
        showView.getTheatreCombo().addActionListener(e -> filterShowsTable());
        showView.getDateCombo().addActionListener(e -> filterShowsTable());

        // Action bindings for Show Selection are now dynamic inside populateShowsTable
        showView.getBackButton().addActionListener(e -> view.showCard("Movie Details"));

        // Bottom Action Buttons
        detailsView.getBtnBookTickets().addActionListener(e -> {
            if (selectedMovie != null) {
                view.getShowSelectionView().getMovieTitleLabel().setText(selectedMovie.getTitle());
                view.showCard("Select Show");
            }
        });

        showView.getBackButton().addActionListener(e -> view.showCard("Movie Details"));
        detailsView.getBtnWatchTrailer().addActionListener(e -> handleWatchTrailer());
        detailsView.getTrailerPanel().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleWatchTrailer();
            }
        });
        detailsView.getBackButton().addActionListener(e -> {
            selectedMovie = null;
            view.showCard("Browse Movies");
        });
        detailsView.getBtnWatchlist().addActionListener(e -> handleWatchlistToggle());
        detailsView.getBtnFavorite().addActionListener(e -> handleFavoriteToggle());
    }

    private void handleWatchTrailer() {
        if (selectedMovie == null) return;
        String yearStr = selectedMovie.getReleaseDate() != null ? String.valueOf(selectedMovie.getReleaseDate().toLocalDate().getYear()) : null;
        
        if (selectedMovie.getTmdbId() != null && selectedMovie.getTmdbId() > 0) {
            fetchAndShowTrailer(selectedMovie.getTmdbId(), yearStr);
            return;
        }

        // Just-in-time fallback for trailers
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                String query = selectedMovie.getTitle() + (yearStr != null ? " " + yearStr : "");
                java.util.List<com.movieticket.model.TmdbMovie> results = new com.movieticket.util.TmdbMovieService().searchMovies(query);
                if (results != null && !results.isEmpty()) {
                    return results.get(0).getTmdbId();
                }
                return 0;
            }
            @Override
            protected void done() {
                try {
                    Integer tId = get();
                    if (tId != null && tId > 0) {
                        fetchAndShowTrailer(tId, yearStr);
                    } else {
                        com.movieticket.view.TrailerDialog dialog = new com.movieticket.view.TrailerDialog(view, selectedMovie.getTitle(), null, yearStr);
                        dialog.setVisible(true);
                    }
                } catch (Exception e) {
                    com.movieticket.view.TrailerDialog dialog = new com.movieticket.view.TrailerDialog(view, selectedMovie.getTitle(), null, yearStr);
                    dialog.setVisible(true);
                }
            }
        }.execute();
    }

    private void fetchAndShowTrailer(int tmdbId, String yearStr) {
        tmdbController.loadMovieDetails(tmdbId, fullTmdb -> {
            SwingUtilities.invokeLater(() -> {
                com.movieticket.view.TrailerDialog dialog = new com.movieticket.view.TrailerDialog(view, selectedMovie.getTitle(), fullTmdb.getTrailerKey(), yearStr);
                dialog.setVisible(true);
            });
        }, err -> {
            SwingUtilities.invokeLater(() -> {
                com.movieticket.view.TrailerDialog dialog = new com.movieticket.view.TrailerDialog(view, selectedMovie.getTitle(), null, yearStr);
                dialog.setVisible(true);
            });
        }, null);
    }

    private void handleStartBookingFromShow(int showId) {
        Optional<Show> optShow = showDAO.getShowById(showId);
        if (optShow.isPresent()) {
            Show show = optShow.get();
            Theatre theatre = theatreMap.get(show.getTheatreId());
            bookingController.startBookingFlowForShow(show, selectedMovie, theatre);
        }
    }

    public void loadMovies() {
        User current = UserSession.getInstance().getCurrentUser();
        int userId = current != null ? current.getUserId() : 0;
        if (userId > 0 && view.getUserActivityCard() != null) {
            view.getUserActivityCard().loadUserData(userId);
        }

        // Show immediate lightweight UI
        JPanel gridPanel = view.getMovieListView().getMoviesGridPanel();
        gridPanel.removeAll();
        gridPanel.add(new JLabel(" Loading active movies..."));
        gridPanel.revalidate();
        gridPanel.repaint();

        view.getMovieListView().getRefreshButton().setEnabled(false);
        SwingWorker<java.util.List<Movie>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<Movie> doInBackground() throws Exception {
                return movieDAO.getActiveMovies(20);
            }

            @Override
            protected void done() {
                view.getMovieListView().getRefreshButton().setEnabled(true);
                try {
                    cachedMovies = get();
                    populateMovieTable(cachedMovies);
                    // Populate recommendations asynchronously
                    populateRecommendedTable(cachedMovies);
                } catch (Exception ex) {
                    System.err.println("[UserDashboardController] Error loading movies asynchronously: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadTmdbSections() {
        // Disabled completely. 
        // We now load everything progressively from the local database on dashboard open
        // and only hit TMDB for details when a specific movie is clicked.
    }
    
    private void populateRecommendedTable(java.util.List<Movie> movies) {
        new SwingWorker<java.util.List<Movie>, Void>() {
            @Override
            protected java.util.List<Movie> doInBackground() {
                if (movies == null) return new java.util.ArrayList<>();
                java.util.List<Movie> shuffled = new java.util.ArrayList<>(movies);
                java.util.Collections.shuffle(shuffled);
                return shuffled;
            }
            @Override
            protected void done() {
                try {
                    java.util.List<Movie> shuffled = get();
                    JPanel gridPanel = view.getMovieListView().getTopRatedGridPanel();
                    gridPanel.removeAll();
                    int count = 0;
                    for (Movie m : shuffled) {
                        if (count >= 10) break;
                        com.movieticket.view.components.MovieCardPanel card = new com.movieticket.view.components.MovieCardPanel(m);
                        card.setOnClickAction(() -> handleMovieCardClick(m));
                        card.setOnBookAction(() -> {
                            selectedMovie = m;
                            view.getShowSelectionView().getMovieTitleLabel().setText(m.getTitle());
                            loadShowsForMovie(m.getMovieId());
                            view.showCard("Select Show");
                        });
                        gridPanel.add(card);
                        count++;
                    }
                    gridPanel.revalidate();
                    gridPanel.repaint();
                } catch (Exception ignored) {}
            }
        }.execute();
    }
    
    private void handleTmdbCardClick(com.movieticket.model.TmdbMovie tmdb) {
        // If it's already in our DB, just open the regular movie view
        Optional<Movie> existing = movieDAO.findByTmdbId(tmdb.getTmdbId());
        if (existing.isPresent()) {
            handleMovieCardClick(existing.get());
            return;
        }
        
        // Map to temporary Movie
        Movie tempMovie = new Movie();
        tempMovie.setMovieId(-1); // denotes not in DB
        tempMovie.setTitle(tmdb.getTitle());
        tempMovie.setGenre(tmdb.getPrimaryGenre());
        tempMovie.setDuration(tmdb.getRuntime());
        tempMovie.setLanguage(tmdb.getOriginalLanguage());
        tempMovie.setTmdbId(tmdb.getTmdbId());
        tempMovie.setTmdbPosterPath(tmdb.getPosterPath());
        tempMovie.setTmdbBackdropPath(tmdb.getBackdropPath());
        
        double r = tmdb.getVoteAverage();
        tempMovie.setRating(java.math.BigDecimal.valueOf(r).setScale(1, java.math.RoundingMode.HALF_UP));
        
        if (tmdb.getReleaseDate() != null && tmdb.getReleaseDate().length() >= 10) {
            try {
                tempMovie.setReleaseDate(java.sql.Date.valueOf(tmdb.getReleaseDate().substring(0, 10)));
            } catch (Exception ignored) {}
        }
        
        selectedMovie = tempMovie;
        view.showCard("Movie Details");
        
        MovieDetailsView detailsView = view.getMovieDetailsView();
        String yearRuntimeGenre = (tmdb.getRuntime() > 0 ? tmdb.getRuntime() + " min \u2022 " : "") + tmdb.getPrimaryGenre();
        String ratingStr = "\u2605 " + tempMovie.getRating().toPlainString() + " / 10";
        
        detailsView.updateMovieDetails(
            tmdb.getTitle(),
            ratingStr,
            yearRuntimeGenre,
            tempMovie.getLanguage(),
            tmdb.getOverview(),
            "N/A", "N/A", "N/A", null
        );
        
        detailsView.getBtnBookTickets().setEnabled(false);
        detailsView.getBtnBookTickets().setText("NOT AVAILABLE IN CINEMAS");
        detailsView.getBtnWatchlist().setEnabled(false);
        detailsView.getBtnFavorite().setEnabled(false);
        
        // Trailer button remains enabled by default, TrailerDialog handles fallbacks
        // detailsView.setTrailerState(false);
        
        // Load details (poster, backdrop, trailer, cast) asynchronously
        tmdbController.loadMovieDetails(tmdb.getTmdbId(), fullTmdb -> {
            
            // Calculate text properties here so they can be captured by both invokeLater and SwingWorker
            final String plot = fullTmdb.getOverview() != null && !fullTmdb.getOverview().isBlank() ? fullTmdb.getOverview() : "No description available.";
            String d = "N/A";
            for (com.movieticket.model.TmdbCrewMember c : fullTmdb.getCrew()) {
                if ("Director".equalsIgnoreCase(c.getJob())) {
                    d = c.getName();
                    break;
                }
            }
            final String director = d;
            
            StringBuilder cs = new StringBuilder();
            for (int i = 0; i < Math.min(3, fullTmdb.getCast().size()); i++) {
                cs.append(fullTmdb.getCast().get(i).getName());
                if (i < 2 && i < fullTmdb.getCast().size() - 1) cs.append(", ");
            }
            final String castStr = cs.length() > 0 ? cs.toString() : "N/A";
            
            SwingUtilities.invokeLater(() -> {
                detailsView.updateMovieDetails(
                    fullTmdb.getTitle(),
                    ratingStr,
                    yearRuntimeGenre,
                    tempMovie.getLanguage(),
                    plot,
                    director,
                    "N/A",
                    castStr,
                    null // poster will be set by image loader
                );
                
                // Set trailer if available
                if (fullTmdb.getTrailerKey() != null && !fullTmdb.getTrailerKey().isBlank()) {
                    detailsView.setTrailerState(true);
                }
            });
            
            // Load Images
            new SwingWorker<Image[], Void>() {
                @Override
                protected Image[] doInBackground() {
                    Image poster = com.movieticket.util.ImageLoader.loadTmdbImage(fullTmdb.getPosterPath(), "w342");
                    Image backdrop = com.movieticket.util.ImageLoader.loadTmdbImage(fullTmdb.getBackdropPath(), "w1280");
                    return new Image[]{poster, backdrop};
                }
                @Override
                protected void done() {
                    try {
                        Image[] imgs = get();
                        if (imgs[0] != null) {
                            Image scaled = imgs[0].getScaledInstance(300, 450, Image.SCALE_SMOOTH);
                            detailsView.updateMovieDetails(fullTmdb.getTitle(), ratingStr, yearRuntimeGenre, tempMovie.getLanguage(), plot, director, "N/A", castStr, scaled);
                        }
                        if (imgs[1] != null) {
                            detailsView.setBackdropImage(imgs[1]);
                        }
                    } catch (Exception ignored) {}
                }
            }.execute();
        }, null, null);
    }

    private void loadRecommendations() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        view.getRecommendationView().showLoading();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private java.util.List<com.movieticket.model.RecommendedMovie> recForYou;
            private java.util.List<com.movieticket.model.RecommendedMovie> comingSoon;
            private java.util.List<com.movieticket.model.RecommendedMovie> popularFallback;

            @Override
            protected Void doInBackground() throws Exception {
                recForYou = recommendationService.getRecommendedForYou(currentUser.getUserId());
                comingSoon = recommendationService.getComingSoonForYou(currentUser.getUserId());
                
                popularFallback = new ArrayList<>();
                if (recForYou.isEmpty()) {
                    List<Movie> popular = recommendationService.getTopRated();
                    for (Movie m : popular) {
                        popularFallback.add(new com.movieticket.model.RecommendedMovie(m, 0, "Popular on CineBook"));
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                view.getRecommendationView().populateRecommendations(recForYou, comingSoon, popularFallback);
            }
        };
        worker.execute();
    }

        private void populateMovieTable(List<Movie> movies) {
        JPanel gridPanel = view.getMovieListView().getMoviesGridPanel();
        gridPanel.removeAll();

        if (movies != null && !movies.isEmpty()) {
            // Set Hero Movie
            Movie hero = movies.get(0);
            selectedMovie = hero;
            
            // Try to load hero poster
            new SwingWorker<Image, Void>() {
                @Override
                protected Image doInBackground() {
                    return com.movieticket.util.ImageLoader.loadMoviePoster(hero, "w780", 100, 130);
                }
                @Override
                protected void done() {
                    try {
                        view.getMovieListView().setHeroMovie(hero, get());
                    } catch (Exception e) {
                        view.getMovieListView().setHeroMovie(hero, null);
                    }
                }
            }.execute();
            
            for (Movie m : movies) {
                com.movieticket.view.components.MovieCardPanel card = new com.movieticket.view.components.MovieCardPanel(m);
                card.setOnClickAction(() -> handleMovieCardClick(m));
                card.setOnBookAction(() -> {
                    selectedMovie = m;
                    view.getShowSelectionView().getMovieTitleLabel().setText(m.getTitle());
                    loadShowsForMovie(m.getMovieId());
                    view.showCard("Select Show");
                });
                gridPanel.add(card);
            }
        }
        
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void handleMovieSearch() {
        String keyword = view.getHeaderSearchField().getText().trim();
        String selectedGenre = (String) view.getMovieListView().getGenreFilterCombo().getSelectedItem();
        String selectedLanguage = (String) view.getMovieListView().getLanguageFilterCombo().getSelectedItem();

        try {
            List<Movie> results = movieDAO.filterMovies(keyword, selectedGenre, selectedLanguage);

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(view,
                        "No movies found matching search/filter criteria.",
                        "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }

            populateMovieTable(results);
        } catch (Exception ex) {
            System.err.println("[UserDashboardController] Error searching movies: " + ex.getMessage());
        }
    }

    private void applyGenreFilter() {
        handleMovieSearch();
    }

    private void handleMovieCardClick(Movie movie) {
        if (movie == null) return;
        
        selectedMovie = movie;
        User currentUser = com.movieticket.util.UserSession.getInstance().getCurrentUser();
        final int userId = currentUser != null ? currentUser.getUserId() : 0;
        final int capturedMovieId = movie.getMovieId();

        // 1. Show Movie Details View immediately
        view.showCard("Movie Details");
        
        // 2. Setup base data while loading
        MovieDetailsView detailsView = view.getMovieDetailsView();
        
        String yearRuntimeGenre = (movie.getDuration() > 0 ? movie.getDuration() + " min \u2022 " : "") + movie.getGenre();
        String ratingStr = "\u2605 " + (movie.getRating() != null ? movie.getRating().toPlainString() : "0.0") + " / 10";
        
        detailsView.updateMovieDetails(
            movie.getTitle(),
            ratingStr,
            yearRuntimeGenre,
            movie.getLanguage(),
            "Loading details...",
            "N/A",
            "N/A",
            "N/A",
            null
        );

        // 3. Initial Button States
        detailsView.getBtnBookTickets().setEnabled(false);
        detailsView.getBtnWatchlist().setEnabled(false);
        detailsView.getBtnFavorite().setEnabled(false);
        
        if ("UPCOMING".equals(movie.getStatusCalculated())) {
            detailsView.getBtnBookTickets().setText("NOT YET AVAILABLE");
        } else {
            detailsView.getBtnBookTickets().setText("BOOK TICKETS");
        }

        // 4. Asynchronously record view and fetch watchlist/favorite statuses
        if (userId > 0) {
            new SwingWorker<boolean[], Void>() {
                @Override
                protected boolean[] doInBackground() {
                    recentlyViewedDAO.recordView(userId, capturedMovieId);
                    boolean isW = watchlistDAO.isWatchlisted(userId, capturedMovieId);
                    boolean isF = favoriteDAO.isFavorited(userId, capturedMovieId);
                    return new boolean[]{isW, isF};
                }
                @Override
                protected void done() {
                    if (selectedMovie == null || selectedMovie.getMovieId() != capturedMovieId) return;
                    try {
                        boolean[] flags = get();
                        detailsView.getBtnWatchlist().setEnabled(true);
                        detailsView.getBtnFavorite().setEnabled(true);
                        detailsView.setWatchlistState(flags[0]);
                        detailsView.setFavoriteState(flags[1]);
                    } catch (Exception ignored) {}
                }
            }.execute();
        }

        // 5. Asynchronously resolve trailer availability and fetch TMDB metadata as PRIMARY
        String releaseYearStr = movie.getReleaseDate() != null ? String.valueOf(movie.getReleaseDate().toLocalDate().getYear()) : null;
        
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                if (movie.getTmdbId() != null && movie.getTmdbId() > 0) {
                    return movie.getTmdbId();
                }
                // Just-in-time TMDB mapping
                String query = movie.getTitle() + (releaseYearStr != null ? " " + releaseYearStr : "");
                java.util.List<com.movieticket.model.TmdbMovie> results = new com.movieticket.util.TmdbMovieService().searchMovies(query);
                if (results != null && !results.isEmpty()) {
                    int foundId = results.get(0).getTmdbId();
                    movie.setTmdbId(foundId);
                    movieDAO.updateTmdbMetadata(movie.getMovieId(), results.get(0));
                    return foundId;
                }
                return 0;
            }

            @Override
            protected void done() {
                if (selectedMovie == null || selectedMovie.getMovieId() != capturedMovieId) return;
                try {
                    Integer finalTmdbId = get();
                    if (finalTmdbId != null && finalTmdbId > 0) {
                        tmdbController.loadMovieDetails(finalTmdbId, fullTmdb -> {
                            SwingUtilities.invokeLater(() -> {
                                if (selectedMovie == null || selectedMovie.getMovieId() != capturedMovieId) return;
                                String plot = fullTmdb.getOverview() != null && !fullTmdb.getOverview().isBlank() ? fullTmdb.getOverview() : "No description available.";
                                String cast = fullTmdb.getCast() != null && !fullTmdb.getCast().isEmpty() ? fullTmdb.getCast().stream().map(c -> c.getName()).collect(java.util.stream.Collectors.joining(", ")) : "N/A";
                                String dir = fullTmdb.getCrew() != null ? fullTmdb.getCrew().stream().filter(c -> "Director".equals(c.getJob())).map(c -> c.getName()).findFirst().orElse("N/A") : "N/A";
                                
                                // Fetch TMDB poster asynchronously
                                SwingWorker<Image, Void> posterWorker = new SwingWorker<>() {
                                    @Override
                                    protected Image doInBackground() {
                                        return com.movieticket.util.ImageLoader.loadMoviePoster(movie, "w342", 300, 450);
                                    }
                                    @Override
                                    protected void done() {
                                        if (selectedMovie == null || selectedMovie.getMovieId() != capturedMovieId) return;
                                        try {
                                            Image scaledPoster = get();
                                            detailsView.updateMovieDetails(
                                                movie.getTitle(),
                                                ratingStr,
                                                yearRuntimeGenre,
                                                movie.getLanguage(),
                                                plot,
                                                dir,
                                                "N/A", 
                                                cast,
                                                scaledPoster
                                            );
                                        } catch (Exception ignored) {}
                                    }
                                };
                                posterWorker.execute();
                            });
                        }, err -> {
                            loadOmdbFallback(movie, detailsView, yearRuntimeGenre, ratingStr, releaseYearStr, capturedMovieId);
                        }, null);
                    } else {
                        loadOmdbFallback(movie, detailsView, yearRuntimeGenre, ratingStr, releaseYearStr, capturedMovieId);
                    }
                } catch (Exception e) {
                    loadOmdbFallback(movie, detailsView, yearRuntimeGenre, ratingStr, releaseYearStr, capturedMovieId);
                }
            }
        }.execute();
        
        // 6. Try TMDB Backdrop if available
        if (movie.getTmdbBackdropPath() != null) {
            new SwingWorker<Image, Void>() {
                @Override
                protected Image doInBackground() {
                    return com.movieticket.util.ImageLoader.loadTmdbImage(movie.getTmdbBackdropPath(), "w1280");
                }
                @Override
                protected void done() {
                    if (selectedMovie == null || selectedMovie.getMovieId() != capturedMovieId) return;
                    try {
                        Image img = get();
                        if (img != null) detailsView.setBackdropImage(img);
                    } catch (Exception ignored) {}
                }
            }.execute();
        } else {
            detailsView.setBackdropImage(null);
        }

        // 7. Load shows asynchronously
        loadShowsForMovie(capturedMovieId);
    }
        
    private void loadOmdbFallback(Movie movie, com.movieticket.view.MovieDetailsView detailsView, String yearRuntimeGenre, String ratingStr, String releaseYearStr, int capturedMovieId) {
        SwingWorker<String, Void> trailerCheckWorker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return com.movieticket.util.MovieTrailerService.getInstance().getTrailerUrl(movie.getTitle(), releaseYearStr);
            }
            @Override
            protected void done() {
                try {
                    String trailerUrl = get();
                    // detailsView.setTrailerState(trailerUrl != null && !trailerUrl.isBlank());
                } catch (Exception ex) {
                    // detailsView.setTrailerState(false);
                }
            }
        };
        trailerCheckWorker.execute();

        SwingWorker<Object[], Void> omdbWorker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() {
                com.movieticket.util.OmdbService omdbService = new com.movieticket.util.OmdbService();
                com.movieticket.model.OmdbMovie omdbInfo = null;
                Image scaledPoster = null;

                if (movie.getImdbId() != null && !movie.getImdbId().isBlank()) {
                    try {
                        omdbInfo = omdbService.getMovieDetails(movie.getImdbId());
                    } catch (Exception e) {}
                }

                String posterUrl = (movie.getPosterUrl() != null && !movie.getPosterUrl().isBlank()
                                    && !"N/A".equalsIgnoreCase(movie.getPosterUrl()))
                                   ? movie.getPosterUrl() : null;

                if (posterUrl == null && omdbInfo != null && omdbInfo.getPosterUrl() != null) {
                    posterUrl = omdbInfo.getPosterUrl();
                }

                if (posterUrl == null && movie.getImdbId() == null && movie.getTitle() != null) {
                    try {
                        java.util.List<com.movieticket.model.OmdbMovie> searchResults =
                            omdbService.searchMovies(movie.getTitle());
                        if (!searchResults.isEmpty()) {
                            com.movieticket.model.OmdbMovie first = searchResults.get(0);
                            if (first.getPosterUrl() != null) posterUrl = first.getPosterUrl();
                            if (omdbInfo == null && first.getImdbId() != null) {
                                try {
                                    omdbInfo = omdbService.getMovieDetails(first.getImdbId());
                                    if (omdbInfo != null && posterUrl == null && omdbInfo.getPosterUrl() != null) {
                                        posterUrl = omdbInfo.getPosterUrl();
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    } catch (Exception e) {}
                }

                if (posterUrl != null) {
                    scaledPoster = com.movieticket.util.ImageLoader.loadImage(posterUrl, 300, 450);
                }
                return new Object[]{ omdbInfo, scaledPoster };
            }

            @Override
            protected void done() {
                if (selectedMovie == null || selectedMovie.getMovieId() != capturedMovieId) return;
                String finalYearRuntimeGenre = yearRuntimeGenre;
                String plot   = "No description available.";
                String dir    = "N/A", writer = "N/A", stars = "N/A";
                Image scaledPoster = null;

                try {
                    Object[] result    = get();
                    com.movieticket.model.OmdbMovie omdbInfo = (com.movieticket.model.OmdbMovie) result[0];
                    scaledPoster = (Image) result[1];

                    if (omdbInfo != null) {
                        finalYearRuntimeGenre = (omdbInfo.getYear()    != null ? omdbInfo.getYear()    + " \u2022 " : "")
                                              + (omdbInfo.getRuntime() != null ? omdbInfo.getRuntime() + " \u2022 " : "")
                                              + (omdbInfo.getGenre()   != null ? omdbInfo.getGenre()   : "");
                        plot   = omdbInfo.getPlot()     != null && !omdbInfo.getPlot().isBlank() ? omdbInfo.getPlot()     : plot;
                        dir    = omdbInfo.getDirector() != null && !omdbInfo.getDirector().isBlank() ? omdbInfo.getDirector() : dir;
                        writer = omdbInfo.getActors()   != null && !omdbInfo.getActors().isBlank() ? omdbInfo.getActors()   : writer;
                        stars  = omdbInfo.getActors()   != null && !omdbInfo.getActors().isBlank() ? omdbInfo.getActors()   : stars;
                    }
                } catch (Exception ex) {
                    System.err.println("[OmdbFallback] " + ex.getMessage());
                } finally {
                    detailsView.updateMovieDetails(
                        movie.getTitle(),
                        ratingStr,
                        finalYearRuntimeGenre,
                        movie.getLanguage(),
                        plot,
                        dir,
                        writer,
                        stars,
                        scaledPoster
                    );
                }
            }
        };
        omdbWorker.execute();
    }
    
    private void handleWatchlistToggle() {
        if (selectedMovie == null) return;

        User currentUser = com.movieticket.util.UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(view, "Please log in to manage your watchlist.", "Authentication Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = currentUser.getUserId();
        int movieId = selectedMovie.getMovieId();
        boolean isSaved = watchlistDAO.isWatchlisted(userId, movieId);
        
        if (isSaved) {
            watchlistDAO.removeFromWatchlist(userId, movieId);
            view.getMovieDetailsView().setWatchlistState(false);
            JOptionPane.showMessageDialog(view, "\"" + selectedMovie.getTitle() + "\" removed from your Watchlist.", "Watchlist Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            watchlistDAO.addToWatchlist(userId, movieId);
            view.getMovieDetailsView().setWatchlistState(true);
            JOptionPane.showMessageDialog(view, "\"" + selectedMovie.getTitle() + "\" added to your Watchlist!", "Watchlist Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void handleFavoriteToggle() {
        if (selectedMovie == null) return;

        User currentUser = com.movieticket.util.UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(view, "Please log in to manage favorites.", "Authentication Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = currentUser.getUserId();
        int movieId = selectedMovie.getMovieId();
        boolean isFavorited = favoriteDAO.isFavorited(userId, movieId);
        
        if (isFavorited) {
            favoriteDAO.removeFromFavorites(userId, movieId);
            view.getMovieDetailsView().setFavoriteState(false);
            JOptionPane.showMessageDialog(view, "\"" + selectedMovie.getTitle() + "\" removed from your Favorites.", "Favorites Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            favoriteDAO.addToFavorites(userId, movieId);
            view.getMovieDetailsView().setFavoriteState(true);
            JOptionPane.showMessageDialog(view, "\"" + selectedMovie.getTitle() + "\" added to your Favorites!", "Favorites Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadShowsForMovie(int movieId) {
        new SwingWorker<java.util.List<Show>, Void>() {
            @Override
            protected java.util.List<Show> doInBackground() {
                try {
                    return showDAO.getShowsByMovie(movieId);
                } catch (Exception ex) {
                    return new java.util.ArrayList<>();
                }
            }
            @Override
            protected void done() {
                if (selectedMovie == null || selectedMovie.getMovieId() != movieId) return;
                try {
                    currentMovieShows = get();
                    updateShowFilterDropdowns(currentMovieShows);
                    populateShowsTable(currentMovieShows);
                } catch (Exception ex) {
                    System.err.println("[UserDashboardController] Error loading shows for movie: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateShowFilterDropdowns(List<Show> shows) {
        JComboBox<String> cityCombo = view.getShowSelectionView().getCityCombo();
        JComboBox<String> theatreCombo = view.getShowSelectionView().getTheatreCombo();
        JComboBox<String> dateCombo = view.getShowSelectionView().getDateCombo();

        cityCombo.removeAllItems();
        cityCombo.addItem("ALL CITIES");

        theatreCombo.removeAllItems();
        theatreCombo.addItem("ALL THEATRES");

        dateCombo.removeAllItems();
        dateCombo.addItem("ALL DATES");

        Set<String> uniqueCities = new LinkedHashSet<>();
        Set<Integer> uniqueTheatres = new LinkedHashSet<>();
        Set<String> uniqueDates = new LinkedHashSet<>();

        for (Show s : shows) {
            uniqueTheatres.add(s.getTheatreId());
            if (s.getShowDate() != null) {
                uniqueDates.add(s.getShowDate().toString());
            }
            if (theatreMap.containsKey(s.getTheatreId())) {
                String loc = theatreMap.get(s.getTheatreId()).getLocation();
                if (loc != null && !loc.trim().isEmpty()) {
                    uniqueCities.add(loc.trim());
                }
            }
        }

        for (String c : uniqueCities) {
            cityCombo.addItem(c);
        }

        for (int tid : uniqueTheatres) {
            String name = theatreMap.containsKey(tid) ? theatreMap.get(tid).getTheatreName() : "Theatre #" + tid;
            theatreCombo.addItem(tid + " - " + name);
        }

        for (String dateStr : uniqueDates) {
            dateCombo.addItem(dateStr);
        }
    }

    private void filterShowsTable() {
        if (currentMovieShows == null) return;

        String selectedCity = (String) view.getShowSelectionView().getCityCombo().getSelectedItem();
        String selectedTheatre = (String) view.getShowSelectionView().getTheatreCombo().getSelectedItem();
        String selectedDate = (String) view.getShowSelectionView().getDateCombo().getSelectedItem();

        List<Show> filtered = new ArrayList<>();

        for (Show s : currentMovieShows) {
            boolean matchCity = true;
            if (selectedCity != null && !selectedCity.startsWith("ALL")) {
                if (theatreMap.containsKey(s.getTheatreId())) {
                    String loc = theatreMap.get(s.getTheatreId()).getLocation();
                    matchCity = loc != null && loc.trim().equalsIgnoreCase(selectedCity);
                } else {
                    matchCity = false;
                }
            }

            boolean matchTheatre = true;
            if (selectedTheatre != null && !selectedTheatre.startsWith("ALL")) {
                try {
                    int tid = Integer.parseInt(selectedTheatre.split(" - ")[0].trim());
                    matchTheatre = (s.getTheatreId() == tid);
                } catch (Exception ignored) {}
            }

            boolean matchDate = true;
            if (selectedDate != null && !selectedDate.startsWith("ALL")) {
                matchDate = s.getShowDate() != null && s.getShowDate().toString().equals(selectedDate);
            }

            if (matchCity && matchTheatre && matchDate) {
                filtered.add(s);
            }
        }

        populateShowsTable(filtered);
    }

    private void populateShowsTable(List<Show> shows) {
        JPanel container = view.getShowSelectionView().getShowsContainer();
        container.removeAll();
        
        if (shows.isEmpty()) {
            JLabel emptyLbl = new JLabel("No shows currently available.");
            emptyLbl.setForeground(com.movieticket.util.CineBookTheme.DANGER_COLOR);
            emptyLbl.setFont(com.movieticket.util.ThemeManager.getFont(java.awt.Font.ITALIC, 16));
            container.add(emptyLbl);
            container.revalidate();
            container.repaint();
            return;
        }

        // Group shows by Theatre
        Map<Integer, List<Show>> showsByTheatre = new LinkedHashMap<>();
        for (Show s : shows) {
            showsByTheatre.computeIfAbsent(s.getTheatreId(), k -> new ArrayList<>()).add(s);
        }

        for (Map.Entry<Integer, List<Show>> entry : showsByTheatre.entrySet()) {
            int tid = entry.getKey();
            List<Show> theatreShows = entry.getValue();
            
            String theatreName = theatreMap.containsKey(tid) ? theatreMap.get(tid).getTheatreName() : "Theatre #" + tid;
            
            com.movieticket.view.components.GlassCardPanel tCard = new com.movieticket.view.components.GlassCardPanel();
            tCard.setLayout(new BoxLayout(tCard, BoxLayout.Y_AXIS));
            tCard.setTopAccent(com.movieticket.util.CineBookTheme.ACCENT_CYAN, 3);
            tCard.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 20, 24));
            tCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
            
            JLabel tNameLbl = new JLabel(theatreName);
            tNameLbl.setFont(com.movieticket.util.ThemeManager.getFont(java.awt.Font.BOLD, 20));
            tNameLbl.setForeground(com.movieticket.util.CineBookTheme.TEXT_PRIMARY);
            tCard.add(tNameLbl);
            tCard.add(Box.createVerticalStrut(10));
            
            // Group by Screen inside theatre
            Map<String, List<Show>> showsByScreen = new LinkedHashMap<>();
            for (Show s : theatreShows) {
                String screen = s.getScreenNumber() != null ? s.getScreenNumber() : "Screen 1";
                showsByScreen.computeIfAbsent(screen, k -> new ArrayList<>()).add(s);
            }
            
            for (Map.Entry<String, List<Show>> sEntry : showsByScreen.entrySet()) {
                String screenName = sEntry.getKey();
                
                JPanel screenRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));
                screenRow.setOpaque(false);
                
                JLabel screenLbl = new JLabel(screenName + "  ");
                screenLbl.setFont(com.movieticket.util.ThemeManager.getFont(java.awt.Font.BOLD, 14));
                screenLbl.setForeground(com.movieticket.util.CineBookTheme.TEXT_MUTED);
                screenRow.add(screenLbl);
                
                for (Show show : sEntry.getValue()) {
                    String timeStr = show.getShowTime() != null ? show.getShowTime().toString() : "00:00";
                    JButton timeBtn = new JButton(timeStr);
                    com.movieticket.util.ThemeManager.styleSecondaryButton(timeBtn);
                    timeBtn.setPreferredSize(new Dimension(100, 36));
                    timeBtn.addActionListener(e -> handleStartBookingFromShow(show.getShowId()));
                    screenRow.add(timeBtn);
                }
                
                tCard.add(screenRow);
            }
            
            container.add(tCard);
            container.add(Box.createVerticalStrut(20));
        }
        
        container.revalidate();
        container.repaint();
    }

    private void handleRefresh() {
        if (!view.getMovieListView().getRefreshButton().isEnabled()) return;
        view.getHeaderSearchField().setText("");
        view.getMovieListView().getGenreFilterCombo().setSelectedIndex(0);
        view.getMovieListView().getLanguageFilterCombo().setSelectedIndex(0);
        selectedMovie = null;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                initTheatreCache();
                return null;
            }
            @Override
            protected void done() {
                loadMovies();
            }
        }.execute();
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            UserSession.getInstance().clearSession();
            view.dispose();

            AuthenticationController authController = new AuthenticationController(new UserDAO());
            authController.startApplication();
        }
    }

    private void handleProfileClick() {
        User current = UserSession.getInstance().getCurrentUser();
        if (current != null) {
            String profileInfo = "Username: " + current.getUsername() + "\n" +
                                 "Role: " + current.getRole();
            JOptionPane.showMessageDialog(view, profileInfo, "User Profile", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleNotificationBellClick() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            notificationController.openNotificationDialog(view, currentUser.getUserId(), this::updateNotificationBadge);
        }
    }

    public void updateNotificationBadge() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            notificationController.updateUnreadBadgeCount(currentUser.getUserId(), view::setUnreadNotificationCount);
        }
    }
}

