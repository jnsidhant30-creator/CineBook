package com.movieticket.controller;

import com.movieticket.dao.MovieDAO;
import com.movieticket.model.Movie;
import com.movieticket.model.OmdbMovie;
import com.movieticket.util.OmdbService;
import com.movieticket.util.OmdbService.OmdbException;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * OmdbController.java — Bridges the OMDb API service layer with the DAO and GUI.
 *
 * All network and database calls are dispatched via SwingWorker so the Swing EDT
 * is never blocked.  Callbacks are always invoked on the EDT.
 *
 * Responsibilities:
 *  - Search OMDb (background thread) → deliver results to GUI callback
 *  - Import / upsert a movie (background thread) → duplicate detection via imdb_id
 *  - Map OmdbMovie fields to existing Movie model fields
 *  - Never expose the API key; never log/display it
 */
public class OmdbController {

    private final OmdbService omdbService;
    private final MovieDAO movieDAO;

    public OmdbController() {
        this.omdbService = new OmdbService();
        this.movieDAO    = new MovieDAO();
    }

    // =========================================================================
    // Search
    // =========================================================================

    /**
     * Launches a background search against OMDb.
     *
     * @param query         the movie title to search
     * @param onSuccess     called on EDT with the list of results (may be empty)
     * @param onError       called on EDT with a user-friendly error message
     * @param onDone        called on EDT when the worker finishes (success or fail) — use to hide spinner
     */
    public void searchOnline(String query,
                             Consumer<List<OmdbMovie>> onSuccess,
                             Consumer<String>          onError,
                             Runnable                  onDone) {

        new SwingWorker<List<OmdbMovie>, Void>() {

            @Override
            protected List<OmdbMovie> doInBackground() throws Exception {
                return omdbService.searchMovies(query);
            }

            @Override
            protected void done() {
                try {
                    List<OmdbMovie> results = get();
                    onSuccess.accept(results);
                } catch (java.util.concurrent.ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof OmdbException) {
                        onError.accept(cause.getMessage());
                    } else {
                        onError.accept("An unexpected error occurred while searching OMDb.");
                        System.err.println("[OmdbController] Unexpected search error: " + cause);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    onError.accept("Search was interrupted. Please try again.");
                } finally {
                    if (onDone != null) onDone.run();
                }
            }
        }.execute();
    }

    // =========================================================================
    // Import / Upsert
    // =========================================================================

    /**
     * Imports an OMDb movie into the local MySQL database in a background thread.
     *
     * Logic:
     *  1. Fetch full details from OMDb using imdbId.
     *  2. Check if a movie with this imdbId already exists in the DB.
     *  3. If YES  → update its metadata (title, genre, language, rating, poster_url)
     *              WITHOUT touching theatre/show/seat/booking/pricing data.
     *  4. If NO   → insert a new row.
     *
     * @param omdbResult    the OmdbMovie returned from search (must have imdbId set)
     * @param onSuccess     called on EDT with a result message (e.g. "Imported" / "Updated")
     * @param onError       called on EDT with a user-friendly error message
     * @param onDone        called on EDT when finished — use to hide spinner / refresh table
     */
    public void importMovie(OmdbMovie omdbResult,
                            Consumer<String> onSuccess,
                            Consumer<String> onError,
                            Runnable         onDone) {

        new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                // 1. Fetch full details
                OmdbMovie details = omdbService.getMovieDetails(omdbResult.getImdbId());
                if (details == null) {
                    throw new OmdbException("Movie details not found on OMDb for ID: " + omdbResult.getImdbId());
                }

                // 2. Map to existing Movie model
                Movie movie = mapToMovie(details);
                String imdbId    = details.getImdbId();
                String posterUrl = details.getPosterUrl();

                // 3. Duplicate check
                Optional<Movie> existing = movieDAO.findByImdbId(imdbId);
                if (existing.isPresent()) {
                    // UPDATE — preserve booking/pricing/theatre/show data
                    Movie existingMovie = existing.get();
                    movie.setMovieId(existingMovie.getMovieId());
                    boolean updated = movieDAO.updateMovieMetadata(movie, imdbId, posterUrl);
                    if (updated) {
                        return "Movie \"" + movie.getTitle() + "\" updated successfully (IMDb ID already existed).";
                    } else {
                        throw new OmdbException("Failed to update movie in database. Please try again.");
                    }
                } else {
                    // INSERT
                    int newId = movieDAO.addMovieWithOmdb(movie, imdbId, posterUrl);
                    if (newId > 0) {
                        return "Movie \"" + movie.getTitle() + "\" imported successfully (ID: " + newId + ").";
                    } else {
                        throw new OmdbException("Failed to save movie to database. Please try again.");
                    }
                }
            }

            @Override
            protected void done() {
                try {
                    String msg = get();
                    onSuccess.accept(msg);
                } catch (java.util.concurrent.ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof OmdbException) {
                        onError.accept(cause.getMessage());
                    } else {
                        onError.accept("An unexpected error occurred while importing the movie.");
                        System.err.println("[OmdbController] Unexpected import error: " + cause);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    onError.accept("Import was interrupted. Please try again.");
                } finally {
                    if (onDone != null) onDone.run();
                }
            }
        }.execute();
    }

    // =========================================================================
    // Mapping — OmdbMovie → Movie (existing model)
    // =========================================================================

    /**
     * Maps the OMDb-sourced data into the existing {@link Movie} domain model.
     *
     * Field mapping:
     *  OMDb Title         → Movie.title
     *  OMDb Genre (first) → Movie.genre
     *  OMDb Runtime       → Movie.duration (minutes integer, 0 if unavailable)
     *  OMDb Language(1st) → Movie.language
     *  OMDb IMDb Rating   → Movie.rating (capped to 10.0, 1 decimal place)
     *  OMDb Year          → Movie.releaseDate (Jan 1 of year, or null if unparseable)
     *
     * CineBook fields NOT touched:
     *  - Theatre, Show, Show time, Seat capacity, Seat availability,
     *    Ticket price, Premium price, VIP price, Booking data
     */
    private Movie mapToMovie(OmdbMovie omdb) {
        Movie movie = new Movie();

        // Title — required
        movie.setTitle(omdb.getTitle() != null ? omdb.getTitle() : "Unknown Title");

        // Genre — first listed genre or "General"
        movie.setGenre(omdb.getPrimaryGenre());

        // Duration — parsed from "162 min" → 162, or 0 if unavailable
        movie.setDuration(omdb.getRuntimeMinutes());

        // Language — first listed language or "English"
        movie.setLanguage(omdb.getPrimaryLanguage());

        // Rating — clamped to [0.0, 10.0] with 1 decimal place
        double r = omdb.getImdbRatingDouble();
        if (r < 0.0) r = 0.0;
        if (r > 10.0) r = 10.0;
        movie.setRating(BigDecimal.valueOf(r).setScale(1, RoundingMode.HALF_UP));

        // Release date — parse first 4 digits of year as "YYYY-01-01"
        movie.setReleaseDate(parseReleaseDate(omdb.getYear()));

        return movie;
    }

    /**
     * Parses the year string returned by OMDb (e.g. "2009", "2009–2012") into a SQL Date
     * for Jan 1 of that year.  Returns null if the year cannot be parsed.
     */
    private java.sql.Date parseReleaseDate(String year) {
        if (year == null || year.isBlank()) return null;
        try {
            // Take only the first 4 digits
            String fourDigit = year.trim().replaceAll("[^0-9].*", "").trim();
            if (fourDigit.length() < 4) return null;
            int y = Integer.parseInt(fourDigit.substring(0, 4));
            java.time.LocalDate ld = java.time.LocalDate.of(y, 1, 1);
            return java.sql.Date.valueOf(ld);
        } catch (Exception e) {
            return null;
        }
    }
}
