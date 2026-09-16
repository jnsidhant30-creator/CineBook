package com.movieticket.util;

import com.movieticket.dao.MovieDAO;
import com.movieticket.dao.ShowDAO;
import com.movieticket.dao.TheatreDAO;
import com.movieticket.model.Movie;
import com.movieticket.model.OmdbMovie;
import com.movieticket.model.Show;
import com.movieticket.model.Theatre;
import com.movieticket.model.TmdbMovie;
import com.movieticket.util.TmdbMovieService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * DatabaseSeeder.java — Idempotent Automatic Curated OMDb Movie Data Seeder.
 *
 * Seeds 35 curated popular movies across diverse genres and eras:
 * Validates OMDb response (Response=True, Poster != 'N/A', IMDb ID, Genre, Plot, etc.),
 * populates the Movie model and MySQL table with live OMDb metadata and posters.
 *
 * Preserves user-generated data, bookings, shows, and favorites.
 */
public class DatabaseSeeder {

    public static final String[][] CURATED_MOVIES = {
        // ACTION / SCI-FI
        {"Avatar", "2009"},
        {"Avengers: Endgame", "2019"},
        {"The Dark Knight", "2008"},
        {"Inception", "2010"},
        {"Interstellar", "2014"},
        {"The Matrix", "1999"},
        {"Jurassic Park", "1993"},
        {"Top Gun: Maverick", "2022"},

        // SUPERHERO / FANTASY
        {"Iron Man", "2008"},
        {"Spider-Man: No Way Home", "2021"},
        {"Black Panther", "2018"},
        {"Guardians of the Galaxy", "2014"},
        {"Doctor Strange", "2016"},
        {"Harry Potter and the Sorcerer's Stone", "2001"},

        // DRAMA
        {"The Shawshank Redemption", "1994"},
        {"Forrest Gump", "1994"},
        {"The Godfather", "1972"},
        {"The Green Mile", "1999"},
        {"The Pursuit of Happyness", "2006"},

        // COMEDY
        {"The Hangover", "2009"},
        {"Home Alone", "1990"},
        {"3 Idiots", "2009"},
        {"Zindagi Na Milegi Dobara", "2011"},

        // ROMANCE
        {"Titanic", "1997"},
        {"The Notebook", "2004"},
        {"La La Land", "2016"},

        // HORROR / THRILLER
        {"The Conjuring", "2013"},
        {"A Quiet Place", "2018"},
        {"Get Out", "2017"},
        {"A Nightmare on Elm Street", "1984"},

        // ANIMATION / FAMILY
        {"Toy Story", "1995"},
        {"Finding Nemo", "2003"},
        {"The Lion King", "1994"},
        {"Frozen", "2013"},
        {"Coco", "2017"}
    };

    /**
     * Backward-compatible entry point called by Main.java.
     */
    public static void seed2026Movies() {
        seedCuratedOmdbMovies();
    }

    /**
     * Seeds or updates the 35 curated movies with verified TMDB metadata and posters (fallback to OMDb).
     */
    public static void seedCuratedOmdbMovies() {
        ensureColumnsExist();

        OmdbService omdbService = new OmdbService();
        TmdbMovieService tmdbService = new TmdbMovieService();
        MovieDAO movieDAO = new MovieDAO();
        TheatreDAO theatreDAO = new TheatreDAO();
        ShowDAO showDAO = new ShowDAO();

        List<Theatre> theatres = theatreDAO.getAllTheatres();
        int defaultTheatreId = (theatres != null && !theatres.isEmpty()) ? theatres.get(0).getTheatreId() : 1;

        com.movieticket.dao.ScreenDAO screenDAO = new com.movieticket.dao.ScreenDAO();
        List<com.movieticket.model.Screen> screens = screenDAO.getScreensByTheatre(defaultTheatreId);
        int defaultScreenId = (screens != null && !screens.isEmpty()) ? screens.get(0).getScreenId() : 1;

        int insertedCount = 0;
        int updatedCount = 0;
        int preservedCount = 0;
        int failedCount = 0;

        for (String[] entry : CURATED_MOVIES) {
            String title = entry[0];
            String year = entry[1];

            try {
                boolean tmdbSuccess = false;
                
                // 1. TMDB PRIMARY FETCH
                List<TmdbMovie> searchResults = tmdbService.searchMovies(title + " " + year);
                if (searchResults != null && !searchResults.isEmpty()) {
                    TmdbMovie bestMatch = null;
                    for (TmdbMovie r : searchResults) {
                        boolean titleMatch = false;
                        if (r.getTitle() != null && (r.getTitle().equalsIgnoreCase(title) || r.getTitle().toLowerCase().contains(title.toLowerCase()))) titleMatch = true;
                        if (r.getOriginalTitle() != null && r.getOriginalTitle().equalsIgnoreCase(title)) titleMatch = true;
                        
                        boolean yearMatch = true;
                        if (r.getReleaseDate() != null && r.getReleaseDate().length() >= 4) {
                            try {
                                int tmdbYear = Integer.parseInt(r.getReleaseDate().substring(0, 4));
                                yearMatch = Math.abs(Integer.parseInt(year) - tmdbYear) <= 1;
                            } catch (Exception ignored) {}
                        }
                        
                        if (titleMatch && yearMatch) {
                            bestMatch = r;
                            break;
                        }
                    }
                    
                    if (bestMatch != null) {
                        TmdbMovie fullTmdb = tmdbService.getMovieDetails(bestMatch.getTmdbId());
                        if (fullTmdb != null && fullTmdb.getPosterPath() != null && !fullTmdb.getPosterPath().isBlank()) {
                            // Map TMDB to Movie entity
                            Movie movie = mapTmdbToMovie(fullTmdb);
                            
                            // Duplicate Check
                            Optional<Movie> existingByTmdb = movieDAO.findByTmdbId(fullTmdb.getTmdbId());
                            Optional<Movie> existingByTitle = Optional.empty();
                            if (existingByTmdb.isEmpty()) {
                                List<Movie> all = movieDAO.getAllMovies();
                                existingByTitle = all.stream()
                                        .filter(m -> m.getTitle() != null && m.getTitle().equalsIgnoreCase(title))
                                        .findFirst();
                            }
                            
                            Optional<Movie> existing = existingByTmdb.isPresent() ? existingByTmdb : existingByTitle;
                            
                            if (existing.isPresent()) {
                                Movie existingMovie = existing.get();
                                boolean needsUpdate = existingMovie.getTmdbId() == null || existingMovie.getTmdbId() == 0 || 
                                                      existingMovie.getTmdbPosterPath() == null || existingMovie.getTmdbPosterPath().isBlank();
                                
                                if (needsUpdate) {
                                    boolean updated = movieDAO.updateTmdbMetadata(existingMovie.getMovieId(), fullTmdb);
                                    if (updated) updatedCount++;
                                } else {
                                    preservedCount++;
                                }
                            } else {
                                int generatedId = movieDAO.addMovieWithTmdb(movie, fullTmdb.getTmdbId(), fullTmdb.getPosterPath(), fullTmdb.getBackdropPath());
                                if (generatedId > 0) {
                                    insertedCount++;
                                    movie.setMovieId(generatedId);
                                    
                                    List<Show> shows = showDAO.getShowsByMovie(generatedId);
                                    if (shows.isEmpty() && defaultTheatreId > 0) {
                                        Show s = new Show(0, generatedId, defaultTheatreId, LocalDate.now(), LocalTime.of(19, 30), new BigDecimal("250.00"));
                                        s.setScreenId(defaultScreenId);
                                        showDAO.addShow(s);
                                    }
                                }
                            }
                            tmdbSuccess = true;
                        }
                    }
                }
                
                // 2. OMDb FALLBACK
                if (!tmdbSuccess) {
                    OmdbMovie omdb = omdbService.getMovieByTitleAndYear(title, year);
                    if (!validateOmdbMovie(omdb, title, year)) {
                        failedCount++;
                        continue;
                    }
                    
                    Movie movie = mapOmdbToMovie(omdb);
                    String imdbId = omdb.getImdbId();
                    String posterUrl = omdb.getPosterUrl();
                    
                    Optional<Movie> existingByImdb = movieDAO.findByImdbId(imdbId);
                    Optional<Movie> existingByTitle = Optional.empty();
                    if (existingByImdb.isEmpty()) {
                        List<Movie> all = movieDAO.getAllMovies();
                        existingByTitle = all.stream()
                                .filter(m -> m.getTitle() != null && m.getTitle().equalsIgnoreCase(title))
                                .findFirst();
                    }
                    
                    Optional<Movie> existing = existingByImdb.isPresent() ? existingByImdb : existingByTitle;
                    
                    if (existing.isPresent()) {
                        Movie existingMovie = existing.get();
                        boolean needsUpdate = existingMovie.getPosterUrl() == null 
                                || existingMovie.getPosterUrl().isBlank()
                                || "N/A".equalsIgnoreCase(existingMovie.getPosterUrl())
                                || existingMovie.getImdbId() == null;
                        
                        if (needsUpdate) {
                            movie.setMovieId(existingMovie.getMovieId());
                            boolean updated = movieDAO.updateMovieMetadata(movie, imdbId, posterUrl);
                            if (updated) updatedCount++;
                        } else {
                            preservedCount++;
                        }
                    } else {
                        int generatedId = movieDAO.addMovieWithOmdb(movie, imdbId, posterUrl);
                        if (generatedId > 0) {
                            insertedCount++;
                            movie.setMovieId(generatedId);
                            
                            List<Show> shows = showDAO.getShowsByMovie(generatedId);
                            if (shows.isEmpty() && defaultTheatreId > 0) {
                                Show s = new Show(0, generatedId, defaultTheatreId, LocalDate.now(), LocalTime.of(19, 30), new BigDecimal("250.00"));
                                s.setScreenId(defaultScreenId);
                                showDAO.addShow(s);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("[DatabaseSeeder] Error seeding movie \"" + title + "\" (" + year + "): " + ex.getMessage());
                failedCount++;
            }
        }

        System.out.println(String.format(
            "[DatabaseSeeder] Seeding Complete -> Inserted: %d, Updated: %d, Preserved: %d, Failed: %d (Total: %d)",
            insertedCount, updatedCount, preservedCount, failedCount, CURATED_MOVIES.length
        ));
    }

    /**
     * Validates OMDb response according to strict criteria:
     * - Response = True (omdb != null)
     * - Poster is not null, not empty, and not "N/A"
     * - IMDb ID exists
     * - Title and Year exist
     * - Genre and Plot exist
     */
    public static boolean validateOmdbMovie(OmdbMovie omdb, String title, String year) {
        if (omdb == null) {
            System.err.println("[DatabaseSeeder] OMDb validation failed (Response=False or null) for: " + title + " (" + year + ")");
            return false;
        }

        if (omdb.getPosterUrl() == null || omdb.getPosterUrl().isBlank() || "N/A".equalsIgnoreCase(omdb.getPosterUrl())) {
            System.err.println("[DatabaseSeeder] OMDb validation failed (Poster unavailable) for: " + title + " (" + year + ")");
            return false;
        }

        if (omdb.getImdbId() == null || omdb.getImdbId().isBlank()) {
            System.err.println("[DatabaseSeeder] OMDb validation failed (Missing IMDb ID) for: " + title + " (" + year + ")");
            return false;
        }

        if (omdb.getTitle() == null || omdb.getTitle().isBlank()) {
            System.err.println("[DatabaseSeeder] OMDb validation failed (Missing Title) for: " + title + " (" + year + ")");
            return false;
        }

        if (omdb.getYear() == null || omdb.getYear().isBlank()) {
            System.err.println("[DatabaseSeeder] OMDb validation failed (Missing Year) for: " + title + " (" + year + ")");
            return false;
        }

        if (omdb.getGenre() == null || omdb.getGenre().isBlank()) {
            System.err.println("[DatabaseSeeder] OMDb validation failed (Missing Genre) for: " + title + " (" + year + ")");
            return false;
        }

        if (omdb.getPlot() == null || omdb.getPlot().isBlank()) {
            System.err.println("[DatabaseSeeder] OMDb validation failed (Missing Plot) for: " + title + " (" + year + ")");
            return false;
        }

        return true;
    }

    /**
     * Maps TmdbMovie to CineBook Movie domain model.
     */
    public static Movie mapTmdbToMovie(TmdbMovie tmdb) {
        Movie movie = new Movie();
        movie.setTitle(tmdb.getTitle() != null ? tmdb.getTitle() : "Unknown");
        movie.setGenre(tmdb.getPrimaryGenre());
        if (tmdb.getRuntime() > 0) {
            movie.setDuration(tmdb.getRuntime());
        } else {
            movie.setDuration(120); // Default if not found
        }
        movie.setLanguage(tmdb.getOriginalLanguage() != null && !tmdb.getOriginalLanguage().isBlank() ? tmdb.getOriginalLanguage() : "English");

        double r = tmdb.getVoteAverage();
        if (r < 0.0) r = 0.0;
        if (r > 10.0) r = 10.0;
        movie.setRating(BigDecimal.valueOf(r).setScale(1, RoundingMode.HALF_UP));

        movie.setReleaseDate(parseReleaseDate(tmdb.getReleaseDate()));
        return movie;
    }

    /**
     * Maps OmdbMovie to CineBook Movie domain model.
     */
    public static Movie mapOmdbToMovie(OmdbMovie omdb) {
        Movie movie = new Movie();
        movie.setTitle(omdb.getTitle() != null ? omdb.getTitle() : "Unknown");
        movie.setGenre(omdb.getPrimaryGenre());
        movie.setDuration(omdb.getRuntimeMinutes());
        movie.setLanguage(omdb.getPrimaryLanguage());

        double r = omdb.getImdbRatingDouble();
        if (r < 0.0) r = 0.0;
        if (r > 10.0) r = 10.0;
        movie.setRating(BigDecimal.valueOf(r).setScale(1, RoundingMode.HALF_UP));

        movie.setReleaseDate(parseReleaseDate(omdb.getYear()));
        movie.setImdbId(omdb.getImdbId());
        movie.setPosterUrl(omdb.getPosterUrl());

        return movie;
    }

    public static Date parseReleaseDate(String year) {
        if (year == null || year.isBlank()) return null;
        try {
            String fourDigit = year.trim().replaceAll("[^0-9].*", "").trim();
            if (fourDigit.length() < 4) return null;
            int y = Integer.parseInt(fourDigit.substring(0, 4));
            LocalDate ld = LocalDate.of(y, 1, 1);
            return Date.valueOf(ld);
        } catch (Exception e) {
            return null;
        }
    }

    private static void ensureColumnsExist() {
        String[] sqls = {
            "ALTER TABLE movies ADD COLUMN release_date DATE NULL",
            "ALTER TABLE movies ADD COLUMN imdb_id VARCHAR(20) NULL",
            "ALTER TABLE movies ADD COLUMN poster_url TEXT NULL",
            "ALTER TABLE movies ADD COLUMN tmdb_id INT NULL",
            "ALTER TABLE movies ADD COLUMN tmdb_poster_path VARCHAR(200) NULL",
            "ALTER TABLE movies ADD COLUMN tmdb_backdrop_path VARCHAR(200) NULL",
            "ALTER TABLE movies ADD COLUMN tmdb_vote_avg DECIMAL(4,2) NULL",
            "ALTER TABLE movies ADD COLUMN tmdb_vote_count INT NULL",
            "ALTER TABLE movies ADD COLUMN tmdb_popularity DECIMAL(10,4) NULL",
            "ALTER TABLE movies ADD COLUMN tmdb_last_updated DATETIME NULL",
            "CREATE UNIQUE INDEX idx_movies_tmdb_id ON movies (tmdb_id)",
            "ALTER TABLE shows ADD COLUMN screen_id INT NULL DEFAULT 1",
            "ALTER TABLE shows ADD COLUMN premium_price DECIMAL(10,2) NULL",
            "ALTER TABLE shows ADD COLUMN vip_price DECIMAL(10,2) NULL"
        };
        for (String sql : sqls) {
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (Exception ignored) {
                // Column already exists or table supports it
            }
        }
    }
}
