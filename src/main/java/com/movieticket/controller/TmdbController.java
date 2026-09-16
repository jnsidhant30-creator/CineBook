package com.movieticket.controller;

import com.movieticket.dao.MovieDAO;
import com.movieticket.model.Movie;
import com.movieticket.model.TmdbMovie;
import com.movieticket.util.TmdbApiClient.TmdbException;
import com.movieticket.util.TmdbMovieService;

import javax.swing.SwingWorker;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * TmdbController.java — Bridges TMDB service layer with GUI and DAO.
 * Runs all network/DB tasks asynchronously via SwingWorker.
 */
public class TmdbController {

    private final TmdbMovieService tmdbService;
    private final MovieDAO movieDAO;

    public TmdbController() {
        this.tmdbService = new TmdbMovieService();
        this.movieDAO = new MovieDAO();
    }

    public void searchMovies(String query, Consumer<List<TmdbMovie>> onSuccess, Consumer<String> onError, Runnable onDone) {
        new SwingWorker<List<TmdbMovie>, Void>() {
            @Override
            protected List<TmdbMovie> doInBackground() {
                return tmdbService.searchMovies(query);
            }
            @Override
            protected void done() {
                handleResult(this, onSuccess, onError, onDone);
            }
        }.execute();
    }

    public void loadMovieDetails(int tmdbId, Consumer<TmdbMovie> onSuccess, Consumer<String> onError, Runnable onDone) {
        new SwingWorker<TmdbMovie, Void>() {
            @Override
            protected TmdbMovie doInBackground() {
                return tmdbService.getMovieDetails(tmdbId);
            }
            @Override
            protected void done() {
                handleResult(this, onSuccess, onError, onDone);
            }
        }.execute();
    }

    public void loadPopular(Consumer<List<TmdbMovie>> onSuccess, Consumer<String> onError, Runnable onDone) {
        new SwingWorker<List<TmdbMovie>, Void>() {
            @Override
            protected List<TmdbMovie> doInBackground() { return tmdbService.getPopularMovies(); }
            @Override
            protected void done() { handleResult(this, onSuccess, onError, onDone); }
        }.execute();
    }

    public void loadTopRated(Consumer<List<TmdbMovie>> onSuccess, Consumer<String> onError, Runnable onDone) {
        new SwingWorker<List<TmdbMovie>, Void>() {
            @Override
            protected List<TmdbMovie> doInBackground() { return tmdbService.getTopRatedMovies(); }
            @Override
            protected void done() { handleResult(this, onSuccess, onError, onDone); }
        }.execute();
    }

    public void loadUpcoming(Consumer<List<TmdbMovie>> onSuccess, Consumer<String> onError, Runnable onDone) {
        new SwingWorker<List<TmdbMovie>, Void>() {
            @Override
            protected List<TmdbMovie> doInBackground() { return tmdbService.getUpcomingMovies(); }
            @Override
            protected void done() { handleResult(this, onSuccess, onError, onDone); }
        }.execute();
    }

    public void loadTrending(Consumer<List<TmdbMovie>> onSuccess, Consumer<String> onError, Runnable onDone) {
        new SwingWorker<List<TmdbMovie>, Void>() {
            @Override
            protected List<TmdbMovie> doInBackground() { return tmdbService.getTrendingMovies(); }
            @Override
            protected void done() { handleResult(this, onSuccess, onError, onDone); }
        }.execute();
    }

    public void loadNowPlaying(Consumer<List<TmdbMovie>> onSuccess, Consumer<String> onError, Runnable onDone) {
        new SwingWorker<List<TmdbMovie>, Void>() {
            @Override
            protected List<TmdbMovie> doInBackground() { return tmdbService.getNowPlayingMovies(); }
            @Override
            protected void done() { handleResult(this, onSuccess, onError, onDone); }
        }.execute();
    }

    public void discoverMovies(String params, Consumer<List<TmdbMovie>> onSuccess, Consumer<String> onError, Runnable onDone) {
        new SwingWorker<List<TmdbMovie>, Void>() {
            @Override
            protected List<TmdbMovie> doInBackground() { return tmdbService.discoverMovies(params); }
            @Override
            protected void done() { handleResult(this, onSuccess, onError, onDone); }
        }.execute();
    }

    public void importMovie(TmdbMovie tmdbResult, Consumer<String> onSuccess, Consumer<String> onError, Runnable onDone) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                // Fetch full details
                TmdbMovie details = tmdbService.getMovieDetails(tmdbResult.getTmdbId());
                if (details == null) {
                    throw new TmdbException("Movie details not found on TMDB for ID: " + tmdbResult.getTmdbId());
                }

                // Check duplicate
                Optional<Movie> existing = movieDAO.findByTmdbId(details.getTmdbId());
                if (existing.isPresent()) {
                    // Update
                    boolean updated = movieDAO.updateTmdbMetadata(existing.get().getMovieId(), details);
                    if (updated) {
                        return "Movie \"" + details.getTitle() + "\" updated successfully.";
                    } else {
                        throw new TmdbException("Failed to update movie in database.");
                    }
                } else {
                    // Insert
                    Movie movie = mapToMovie(details);
                    int newId = movieDAO.addMovieWithTmdb(movie, details.getTmdbId(), details.getPosterPath(), details.getBackdropPath());
                    if (newId > 0) {
                        return "Movie \"" + movie.getTitle() + "\" imported successfully (ID: " + newId + ").";
                    } else {
                        throw new TmdbException("Failed to save movie to database.");
                    }
                }
            }
            @Override
            protected void done() {
                handleResult(this, onSuccess, onError, onDone);
            }
        }.execute();
    }
    
    public void syncMovie(int localMovieId, int tmdbId, Consumer<String> onSuccess, Consumer<String> onError, Runnable onDone) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                TmdbMovie details = tmdbService.getMovieDetails(tmdbId);
                if (details == null) {
                    throw new TmdbException("Movie details not found on TMDB for ID: " + tmdbId);
                }
                boolean updated = movieDAO.updateTmdbMetadata(localMovieId, details);
                if (updated) {
                    return "Movie metadata synced with TMDB successfully.";
                } else {
                    throw new TmdbException("Failed to sync movie in database.");
                }
            }
            @Override
            protected void done() {
                handleResult(this, onSuccess, onError, onDone);
            }
        }.execute();
    }

    private <T> void handleResult(SwingWorker<T, Void> worker, Consumer<T> onSuccess, Consumer<String> onError, Runnable onDone) {
        try {
            T result = worker.get();
            if (onSuccess != null) onSuccess.accept(result);
        } catch (java.util.concurrent.ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof TmdbException) {
                if (onError != null) onError.accept(cause.getMessage());
            } else {
                if (onError != null) onError.accept("An unexpected error occurred: " + cause.getMessage());
                cause.printStackTrace();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            if (onError != null) onError.accept("Task was interrupted. Please try again.");
        } finally {
            if (onDone != null) onDone.run();
        }
    }

    private Movie mapToMovie(TmdbMovie tmdb) {
        Movie movie = new Movie();
        movie.setTitle(tmdb.getTitle() != null ? tmdb.getTitle() : "Unknown Title");
        movie.setGenre(tmdb.getPrimaryGenre());
        movie.setDuration(tmdb.getRuntime());
        movie.setLanguage(tmdb.getOriginalLanguage() != null ? tmdb.getOriginalLanguage() : "English");
        
        double r = tmdb.getVoteAverage();
        if (r < 0.0) r = 0.0;
        if (r > 10.0) r = 10.0;
        movie.setRating(BigDecimal.valueOf(r).setScale(1, RoundingMode.HALF_UP));
        
        movie.setReleaseDate(parseReleaseDate(tmdb.getReleaseDate()));
        return movie;
    }

    private java.sql.Date parseReleaseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank() || dateStr.length() < 10) return null;
        try {
            // TMDB format is "YYYY-MM-DD"
            java.time.LocalDate ld = java.time.LocalDate.parse(dateStr.substring(0, 10));
            return java.sql.Date.valueOf(ld);
        } catch (Exception e) {
            return null;
        }
    }
}
