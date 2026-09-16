package com.movieticket;

import com.movieticket.dao.MovieDAO;
import com.movieticket.dao.ShowDAO;
import com.movieticket.model.Movie;
import com.movieticket.model.OmdbMovie;
import com.movieticket.util.DatabaseSeeder;
import com.movieticket.util.ImageLoader;
import com.movieticket.util.OmdbService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RemoveMoviesWithoutPostersTest {

    @Test
    @DisplayName("Inspect, Validate Posters, and Remove Movies Without Posters")
    public void testRemoveMoviesWithoutPosters() {
        MovieDAO movieDAO = new MovieDAO();
        ShowDAO showDAO = new ShowDAO();
        OmdbService omdbService = new OmdbService();

        List<Movie> allMovies = movieDAO.getAllMovies();
        int totalBefore = allMovies.size();
        System.out.println("TOTAL MOVIES BEFORE: " + totalBefore);

        List<Movie> removedMovies = new ArrayList<>();
        List<Movie> validMovies = new ArrayList<>();

        for (Movie movie : allMovies) {
            boolean valid = true;
            String posterUrl = movie.getPosterUrl();
            OmdbMovie omdb = null;

            // 1. Check if movie has IMDb ID or Title/Year to query OMDb
            try {
                if (movie.getImdbId() != null && !movie.getImdbId().isBlank()) {
                    omdb = omdbService.getMovieDetails(movie.getImdbId());
                } else if (movie.getTitle() != null && !movie.getTitle().isBlank()) {
                    String yearStr = movie.getReleaseDate() != null ? String.valueOf(movie.getReleaseDate().toLocalDate().getYear()) : null;
                    omdb = omdbService.getMovieByTitleAndYear(movie.getTitle(), yearStr);
                }
            } catch (Exception e) {
                System.err.println("OMDb query failed for movie: " + movie.getTitle() + " - " + e.getMessage());
                valid = false;
            }

            // 2. Validate OMDb Response
            if (omdb == null && (posterUrl == null || posterUrl.isBlank() || "N/A".equalsIgnoreCase(posterUrl))) {
                System.out.println("No valid OMDb response for: " + movie.getTitle());
                valid = false;
            }

            // 3. Resolve poster URL from movie or OMDb
            if (valid && (posterUrl == null || posterUrl.isBlank() || "N/A".equalsIgnoreCase(posterUrl))) {
                if (omdb != null && omdb.getPosterUrl() != null && !omdb.getPosterUrl().isBlank() && !"N/A".equalsIgnoreCase(omdb.getPosterUrl())) {
                    posterUrl = omdb.getPosterUrl();
                    // Update DB with valid OMDb poster URL
                    movie.setPosterUrl(posterUrl);
                    if (omdb.getImdbId() != null) movie.setImdbId(omdb.getImdbId());
                    movieDAO.updateMovieMetadata(movie, movie.getImdbId(), posterUrl);
                } else {
                    System.out.println("Poster is null/empty/N/A for: " + movie.getTitle());
                    valid = false;
                }
            }

            // 4. Test downloading poster image
            if (valid && posterUrl != null && !posterUrl.isBlank() && !"N/A".equalsIgnoreCase(posterUrl)) {
                Image img = ImageLoader.loadImage(posterUrl);
                if (img == null || img.getWidth(null) <= 0 || img.getHeight(null) <= 0) {
                    System.out.println("Poster image failed to load for: " + movie.getTitle() + " (URL: " + posterUrl + ")");
                    valid = false;
                }
            }

            if (!valid) {
                removedMovies.add(movie);
                // Attempt removal from DB if no bookings exist
                int movieId = movie.getMovieId();
                try {
                    // Remove associated shows without bookings if necessary
                    List<com.movieticket.model.Show> shows = showDAO.getShowsByMovie(movieId);
                    for (com.movieticket.model.Show s : shows) {
                        if (!showDAO.hasAssociatedBookings(s.getShowId())) {
                            showDAO.deleteShow(s.getShowId());
                        }
                    }
                    boolean deleted = movieDAO.deleteMovie(movieId);
                    System.out.println("Deleted movie record ID " + movieId + " (\"" + movie.getTitle() + "\"): " + deleted);
                } catch (Exception ex) {
                    System.err.println("Could not delete movie ID " + movieId + ": " + ex.getMessage());
                }
            } else {
                validMovies.add(movie);
            }
        }

        // Clean up DatabaseSeeder curated movie list if any seed entries failed
        List<String[]> cleanCurated = new ArrayList<>();
        for (String[] entry : DatabaseSeeder.CURATED_MOVIES) {
            String title = entry[0];
            boolean isRemoved = removedMovies.stream().anyMatch(m -> m.getTitle() != null && m.getTitle().equalsIgnoreCase(title));
            if (!isRemoved) {
                cleanCurated.add(entry);
            }
        }

        int removedCount = removedMovies.size();
        int remainingCount = validMovies.size();

        System.out.println("=========================================");
        System.out.println("TOTAL MOVIES BEFORE: " + totalBefore);
        System.out.println("REMOVED: " + removedCount);
        System.out.println("REMAINING MOVIES: " + remainingCount);
        System.out.println("POSTER VALIDATION: " + (removedCount == 0 || remainingCount > 0 ? "PASS" : "FAIL"));
        System.out.println("=========================================");

        assertTrue(remainingCount > 0, "Remaining movies should be > 0");
    }
}
