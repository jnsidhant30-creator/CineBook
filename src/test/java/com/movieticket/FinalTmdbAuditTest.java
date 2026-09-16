package com.movieticket;

import com.movieticket.dao.MovieDAO;
import com.movieticket.model.Movie;

import java.util.List;

public class FinalTmdbAuditTest {
    public static void main(String[] args) {
        System.out.println("=== FINAL TMDB AUDIT ===");
        try {
            MovieDAO movieDAO = new MovieDAO();
            List<Movie> movies = movieDAO.getAllMovies();
            
            int total = movies.size();
            int tmdbMapped = 0;
            int tmdbUnmapped = 0;
            int omdbFallback = 0;
            int missingPoster = 0;
            int missingTrailer = 0; // we can't easily check trailers from DB without API calls, but we check tmdb_id

            for (Movie m : movies) {
                boolean hasTmdb = m.getTmdbId() != null && m.getTmdbId() > 0;
                if (hasTmdb) {
                    tmdbMapped++;
                } else {
                    tmdbUnmapped++;
                    if (m.getImdbId() != null && !m.getImdbId().isEmpty()) {
                        omdbFallback++;
                    }
                }
                
                boolean hasPoster = false;
                if (m.getTmdbPosterPath() != null && !m.getTmdbPosterPath().trim().isEmpty()) {
                    hasPoster = true;
                } else if (m.getPosterUrl() != null && !m.getPosterUrl().trim().isEmpty() && !m.getPosterUrl().equalsIgnoreCase("N/A")) {
                    hasPoster = true;
                }
                
                if (!hasPoster) {
                    missingPoster++;
                    System.out.println("Missing Poster for: " + m.getTitle() + " (ID: " + m.getMovieId() + ")");
                }
            }

            System.out.println("Total movies: " + total);
            System.out.println("TMDB mapped: " + tmdbMapped);
            System.out.println("TMDB unmapped: " + tmdbUnmapped);
            System.out.println("OMDb fallback: " + omdbFallback);
            System.out.println("Missing poster: " + missingPoster);

            System.out.println("=== END AUDIT ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
