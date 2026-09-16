package com.movieticket;

import com.movieticket.dao.MovieDAO;
import com.movieticket.model.Movie;
import com.movieticket.model.TmdbMovie;
import com.movieticket.util.TmdbMovieService;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

public class PosterTrailerAudit {

    public static void main(String[] args) {
        System.out.println("Starting Poster and Trailer Audit...");

        MovieDAO movieDAO = new MovieDAO();
        TmdbMovieService tmdbService = new TmdbMovieService();

        List<Movie> allMovies = movieDAO.getAllMovies();
        System.out.println("Found " + allMovies.size() + " movies in the local database.");

        List<String> posterAndTrailer = new ArrayList<>();
        List<String> posterOnly = new ArrayList<>();
        List<String> neither = new ArrayList<>();

        try (PrintWriter writer = new PrintWriter(new FileWriter("CINEBOOK_MOVIE_POSTER_TRAILER_AUDIT.md"))) {
            writer.println("# CineBook Movie Poster & Trailer Audit\n");
            writer.println("| Movie | Year | Local Movie ID | TMDB ID | Poster Available | Trailer Available | Trailer Type | Video Site | Video Key | Status |");
            writer.println("|---|---|---|---|---|---|---|---|---|---|");

            for (Movie movie : allMovies) {
                String title = movie.getTitle();
                String year = movie.getReleaseDate() != null ? String.valueOf(movie.getReleaseDate().toLocalDate().getYear()) : "N/A";
                int localId = movie.getMovieId();
                Integer tmdbId = movie.getTmdbId();

                if (tmdbId == null || tmdbId == 0) {
                    List<TmdbMovie> searchResults = tmdbService.searchMovies(title);
                    if (searchResults != null && !searchResults.isEmpty()) {
                        tmdbId = searchResults.get(0).getTmdbId();
                    } else {
                        writer.printf("| %s | %s | %d | N/A | false | false | N/A | N/A | N/A | ❌ Neither |%n",
                                title, year, localId);
                        neither.add(title);
                        continue;
                    }
                }

                // Fetch details from TMDB
                TmdbMovie tmdbDetails = tmdbService.getMovieDetails(tmdbId);
                if (tmdbDetails == null) {
                    writer.printf("| %s | %s | %d | %d | false | false | N/A | N/A | N/A | ❌ Neither |%n",
                            title, year, localId, tmdbId);
                    neither.add(title);
                    continue;
                }

                boolean hasPoster = tmdbDetails.getPosterPath() != null && !tmdbDetails.getPosterPath().isBlank();
                boolean hasTrailer = tmdbDetails.getTrailerKey() != null && !tmdbDetails.getTrailerKey().isBlank();

                String trailerType = hasTrailer ? "Trailer" : "N/A";
                String videoSite = hasTrailer ? "YouTube" : "N/A";
                String videoKey = hasTrailer ? tmdbDetails.getTrailerKey() : "N/A";

                String status;
                if (hasPoster && hasTrailer) {
                    status = "✅ VERIFIED";
                    posterAndTrailer.add(title);
                } else if (hasPoster) {
                    status = "⚠️ PARTIAL";
                    posterOnly.add(title);
                } else {
                    status = "❌ FAILED";
                    neither.add(title);
                }

                writer.printf("| %s | %s | %d | %d | %b | %b | %s | %s | %s | %s |%n",
                        title, year, localId, tmdbId, hasPoster, hasTrailer, trailerType, videoSite, videoKey, status);
            }

            System.out.println("\nAudit Report Generated: CINEBOOK_MOVIE_POSTER_TRAILER_AUDIT.md");
            
            System.out.println("\n--- Summary ---");
            System.out.println("✅ Poster + Trailer: " + posterAndTrailer.size());
            System.out.println("⚠️ Poster only: " + posterOnly.size());
            System.out.println("❌ Neither: " + neither.size());
            
            System.out.println("\nDetails:");
            System.out.println("✅ Poster + Trailer: " + posterAndTrailer);
            System.out.println("⚠️ Poster only: " + posterOnly);
            System.out.println("❌ Neither: " + neither);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
