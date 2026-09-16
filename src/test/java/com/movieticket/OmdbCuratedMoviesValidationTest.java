package com.movieticket;

import com.movieticket.dao.MovieDAO;
import com.movieticket.model.Movie;
import com.movieticket.model.OmdbMovie;
import com.movieticket.util.DatabaseSeeder;
import com.movieticket.util.ImageLoader;
import com.movieticket.util.OmdbService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Image;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class OmdbCuratedMoviesValidationTest {

    @Test
    @DisplayName("Validate all 35 curated movies with OMDb and Seed Database")
    public void testValidateAndSeedAllCuratedMovies() throws Exception {
        OmdbService omdbService = new OmdbService();
        MovieDAO movieDAO = new MovieDAO();

        String[][] movies = DatabaseSeeder.CURATED_MOVIES;
        assertEquals(35, movies.length, "Total curated movies must be 35");

        System.out.println("====================================================================================================================");
        System.out.println(String.format("%-36s | %-4s | %-13s | %-16s | %-25s | %-10s | %-6s",
                "Movie", "Year", "OMDb Response", "Poster", "Genre", "IMDb ID", "Status"));
        System.out.println("====================================================================================================================");

        int validCount = 0;
        int posterAvailableCount = 0;
        int posterFailedCount = 0;

        for (String[] entry : movies) {
            String title = entry[0];
            String year = entry[1];

            OmdbMovie omdb = null;
            boolean omdbResponse = false;
            String posterStatus = "N/A";
            String genre = "N/A";
            String imdbId = "N/A";
            String status = "FAILED";

            try {
                omdb = omdbService.getMovieByTitleAndYear(title, year);
                if (omdb != null) {
                    omdbResponse = true;
                    genre = omdb.getGenre() != null ? omdb.getGenre() : "N/A";
                    imdbId = omdb.getImdbId() != null ? omdb.getImdbId() : "N/A";
                    if (omdb.getPosterUrl() != null && !omdb.getPosterUrl().isBlank() && !"N/A".equalsIgnoreCase(omdb.getPosterUrl())) {
                        posterStatus = "Available";
                        posterAvailableCount++;
                    } else {
                        posterStatus = "Unavailable";
                        posterFailedCount++;
                    }

                    boolean isValid = DatabaseSeeder.validateOmdbMovie(omdb, title, year);
                    if (isValid) {
                        status = "PASS";
                        validCount++;
                    }
                } else {
                    posterFailedCount++;
                }
            } catch (Exception e) {
                System.err.println("Error querying " + title + ": " + e.getMessage());
                posterFailedCount++;
            }

            System.out.println(String.format("%-36s | %-4s | %-13s | %-16s | %-25s | %-10s | %-6s",
                    title, year, omdbResponse ? "True" : "False", posterStatus,
                    genre.length() > 25 ? genre.substring(0, 22) + "..." : genre,
                    imdbId, status));
        }

        System.out.println("====================================================================================================================");
        System.out.println("TOTAL MOVIES: " + movies.length);
        System.out.println("OMDb VALID: " + validCount);
        System.out.println("POSTER AVAILABLE: " + posterAvailableCount);
        System.out.println("POSTER FAILED: " + posterFailedCount);
        System.out.println("====================================================================================================================");

        // Execute Database Seeder
        DatabaseSeeder.seedCuratedOmdbMovies();

        // Verify Database has the movies
        List<Movie> allMovies = movieDAO.getAllMovies();
        assertTrue(allMovies.size() >= 30, "Database should have at least 30 movies after seeding");

        // Verify required poster test movies
        String[] testPosterMovies = {
            "Avatar",
            "Inception",
            "The Dark Knight",
            "Interstellar",
            "Titanic",
            "3 Idiots",
            "Toy Story",
            "The Conjuring",
            "Coco",
            "Avengers: Endgame"
        };

        System.out.println("\n--- Testing Posters for 10 Required Key Movies ---");
        for (String testTitle : testPosterMovies) {
            Optional<Movie> found = allMovies.stream()
                    .filter(m -> m.getTitle() != null && m.getTitle().equalsIgnoreCase(testTitle))
                    .findFirst();

            assertTrue(found.isPresent(), "Movie must be found in DB: " + testTitle);
            Movie m = found.get();
            assertNotNull(m.getPosterUrl(), "Poster URL must exist for: " + testTitle);
            assertFalse(m.getPosterUrl().isBlank() || "N/A".equalsIgnoreCase(m.getPosterUrl()),
                    "Poster URL must not be N/A for: " + testTitle);

            Image img = ImageLoader.loadImage(m.getPosterUrl());
            assertNotNull(img, "Poster image must download successfully for: " + testTitle);
            System.out.println(" [PASS] " + testTitle + " -> Poster URL: " + m.getPosterUrl() + " (Image: " + img.getWidth(null) + "x" + img.getHeight(null) + ")");
        }
        System.out.println("--- All 10 Required Key Movie Posters Verified Successfully ---\n");
    }
}
