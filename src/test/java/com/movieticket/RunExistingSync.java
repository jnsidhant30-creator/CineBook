package com.movieticket;

import com.movieticket.dao.MovieDAO;
import com.movieticket.model.Movie;
import com.movieticket.util.TmdbMovieService;

import java.util.List;

public class RunExistingSync {
    public static void main(String[] args) {
        System.out.println("Starting existing TMDB-first synchronization...");
        try {
            MovieDAO dao = new MovieDAO();
            List<Movie> movies = dao.getAllMovies();
            System.out.println("Loaded " + movies.size() + " movies.");
            
            TmdbMovieService service = new TmdbMovieService();
            service.syncLegacyMovies(movies, 
                msg -> System.out.println(msg), 
                () -> {
                    System.out.println("Sync completed.");
                    System.exit(0);
                }
            );
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
