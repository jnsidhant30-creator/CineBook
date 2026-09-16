package com.movieticket.dao;

import com.movieticket.model.Movie;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Movie entity (Phase 8).
 *
 * Implements CRUD operations, search across multiple columns (title, genre, language),
 * live counting, and foreign-key dependency verification with MySQL.
 */
public class MovieDAO {

    public int addMovie(Movie movie) {
        String sql = "INSERT INTO movies (title, genre, duration, language, rating, release_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getGenre());
            stmt.setInt(3, movie.getDuration());
            stmt.setString(4, movie.getLanguage());
            stmt.setBigDecimal(5, movie.getRating());
            stmt.setDate(6, movie.getReleaseDate());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error adding movie: " + e.getMessage());
        }
        return -1;
    }

    public Optional<Movie> getMovieById(int movieId) {
        String sql = "SELECT * FROM movies WHERE movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error finding movie by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY release_date DESC, movie_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                movies.add(mapResultSetToMovie(rs));
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error loading all movies: " + e.getMessage());
        }
        return movies;
    }

    public List<Movie> getActiveMovies(int limit) {
        List<Movie> movies = new ArrayList<>();
        // Select only required columns for displaying movie cards on the dashboard.
        String sql = "SELECT movie_id, title, genre, duration, language, rating, release_date, imdb_id, poster_url, tmdb_id, tmdb_poster_path FROM movies ORDER BY release_date DESC, movie_id DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movies.add(mapResultSetToMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error loading active movies: " + e.getMessage());
        }
        return movies;
    }


    public List<Movie> searchMovies(String keyword) {
        return filterMovies(keyword, null, null);
    }

    public List<Movie> filterMovies(String keyword, String genre, String language) {
        StringBuilder sql = new StringBuilder("SELECT * FROM movies WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + keyword.trim() + "%");
        }

        if (genre != null && !genre.isEmpty() && !"ALL".equalsIgnoreCase(genre)) {
            sql.append(" AND UPPER(genre) = UPPER(?)");
            params.add(genre.trim());
        }

        if (language != null && !language.isEmpty() && !"ALL".equalsIgnoreCase(language)) {
            sql.append(" AND UPPER(language) = UPPER(?)");
            params.add(language.trim());
        }

        sql.append(" ORDER BY movie_id ASC");

        List<Movie> movies = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movies.add(mapResultSetToMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error filtering movies: " + e.getMessage());
        }
        return movies;
    }

    public List<String> getDistinctGenres() {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT DISTINCT genre FROM movies WHERE genre IS NOT NULL AND genre != '' ORDER BY genre ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                genres.add(rs.getString("genre"));
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error fetching distinct genres: " + e.getMessage());
        }
        return genres;
    }

    public List<String> getDistinctLanguages() {
        List<String> languages = new ArrayList<>();
        String sql = "SELECT DISTINCT language FROM movies WHERE language IS NOT NULL AND language != '' ORDER BY language ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                languages.add(rs.getString("language"));
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error fetching distinct languages: " + e.getMessage());
        }
        return languages;
    }

    public boolean updateMovie(Movie movie) {
        String sql = "UPDATE movies SET title = ?, genre = ?, duration = ?, language = ?, rating = ? WHERE movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getGenre());
            stmt.setInt(3, movie.getDuration());
            stmt.setString(4, movie.getLanguage());
            stmt.setBigDecimal(5, movie.getRating());
            stmt.setInt(6, movie.getMovieId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error updating movie: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteMovie(int movieId) {
        String sql = "DELETE FROM movies WHERE movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error deleting movie: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if any shows are associated with this movie before deletion to protect referential integrity.
     */
    public boolean hasAssociatedShows(int movieId) {
        String sql = "SELECT COUNT(*) FROM shows WHERE movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error checking associated shows: " + e.getMessage());
        }
        return false;
    }

    private Movie mapResultSetToMovie(ResultSet rs) throws SQLException {
        Date relDate = null;
        try {
            relDate = rs.getDate("release_date");
        } catch (SQLException ignored) {}

        Movie movie = new Movie(
                rs.getInt("movie_id"),
                rs.getString("title"),
                rs.getString("genre"),
                rs.getInt("duration"),
                rs.getString("language"),
                rs.getBigDecimal("rating"),
                relDate
        );

        // Read OMDb columns if they exist (graceful fallback if migration not yet applied)
        try { movie.setImdbId(rs.getString("imdb_id")); } catch (SQLException ignored) {}
        try { movie.setPosterUrl(rs.getString("poster_url")); } catch (SQLException ignored) {}

        // Read TMDB columns if they exist
        try {
            int tmdbId = rs.getInt("tmdb_id");
            if (!rs.wasNull()) movie.setTmdbId(tmdbId);
        } catch (SQLException ignored) {}
        try { movie.setTmdbPosterPath(rs.getString("tmdb_poster_path")); } catch (SQLException ignored) {}
        try { movie.setTmdbBackdropPath(rs.getString("tmdb_backdrop_path")); } catch (SQLException ignored) {}
        try { movie.setTmdbVoteAvg(rs.getBigDecimal("tmdb_vote_avg")); } catch (SQLException ignored) {}
        try {
            int voteCount = rs.getInt("tmdb_vote_count");
            if (!rs.wasNull()) movie.setTmdbVoteCount(voteCount);
        } catch (SQLException ignored) {}
        try { movie.setTmdbPopularity(rs.getBigDecimal("tmdb_popularity")); } catch (SQLException ignored) {}
        try { movie.setTmdbLastUpdated(rs.getTimestamp("tmdb_last_updated")); } catch (SQLException ignored) {}

        return movie;
    }

    public boolean existsMovieTitle(String title) {
        String sql = "SELECT 1 FROM movies WHERE UPPER(TRIM(title)) = UPPER(TRIM(?))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error checking movie existence: " + e.getMessage());
            return false;
        }
    }

    public List<Movie> getLatestMovies(int limit) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT movie_id, title, genre, duration, language, rating, release_date, imdb_id, poster_url, tmdb_id, tmdb_poster_path FROM movies ORDER BY release_date DESC, movie_id DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit <= 0 ? 10 : limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movies.add(mapResultSetToMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error fetching latest movies: " + e.getMessage());
        }
        return movies;
    }

    public List<Movie> getNowShowingMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE release_date IS NULL OR release_date <= CURRENT_DATE ORDER BY release_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                movies.add(mapResultSetToMovie(rs));
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error fetching now showing movies: " + e.getMessage());
        }
        return movies;
    }

    public List<Movie> getUpcomingMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE release_date > CURRENT_DATE ORDER BY release_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                movies.add(mapResultSetToMovie(rs));
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error fetching upcoming movies: " + e.getMessage());
        }
        return movies;
    }

    public int countMovies() throws SQLException {
        String sql = "SELECT COUNT(*) FROM movies";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // =========================================================================
    // OMDb Integration Methods
    // =========================================================================

    /**
     * Finds an existing movie by its IMDb ID.
     * Used to detect duplicates before importing from OMDb.
     *
     * @param imdbId the IMDb identifier (e.g. "tt0499549")
     * @return the existing Movie wrapped in Optional, or empty if not found
     */
    public Optional<Movie> findByImdbId(String imdbId) {
        if (imdbId == null || imdbId.isBlank()) return Optional.empty();
        String sql = "SELECT * FROM movies WHERE imdb_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, imdbId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error finding movie by IMDb ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Inserts a new movie imported from OMDb, including the IMDb ID and poster URL.
     *
     * @param movie     the Movie object with title/genre/duration/language/rating populated
     * @param imdbId    the IMDb identifier to store (may be null)
     * @param posterUrl the OMDb poster URL to store (may be null)
     * @return the generated movie_id, or -1 if the insert failed
     */
    public int addMovieWithOmdb(Movie movie, String imdbId, String posterUrl) {
        String sql = "INSERT INTO movies (title, genre, duration, language, rating, release_date, imdb_id, poster_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getGenre());
            stmt.setInt(3, movie.getDuration());
            stmt.setString(4, movie.getLanguage());
            stmt.setBigDecimal(5, movie.getRating());
            stmt.setDate(6, movie.getReleaseDate());
            stmt.setString(7, imdbId);
            stmt.setString(8, posterUrl);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error adding OMDb movie: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates the metadata (title, genre, language, rating, imdb_id, poster_url) of an
     * existing movie identified by movie_id.
     *
     * IMPORTANT: This method deliberately does NOT touch duration, release_date, or any
     * columns that belong to the booking/show/pricing domain.  Theatre, show, seat,
     * and booking data are always preserved.
     *
     * @param movie     Movie with movieId, title, genre, language, rating set
     * @param imdbId    IMDb identifier (may be null)
     * @param posterUrl OMDb poster URL (may be null)
     * @return true if the update affected at least one row
     */
    public boolean updateMovieMetadata(Movie movie, String imdbId, String posterUrl) {
        String sql = "UPDATE movies SET title = ?, genre = ?, language = ?, rating = ?, " +
                     "imdb_id = ?, poster_url = ? WHERE movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getGenre());
            stmt.setString(3, movie.getLanguage());
            stmt.setBigDecimal(4, movie.getRating());
            stmt.setString(5, imdbId);
            stmt.setString(6, posterUrl);
            stmt.setInt(7, movie.getMovieId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error updating OMDb metadata: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // TMDB Integration Methods
    // =========================================================================

    /**
     * Finds an existing movie by its TMDB ID.
     */
    public Optional<Movie> findByTmdbId(int tmdbId) {
        String sql = "SELECT * FROM movies WHERE tmdb_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tmdbId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error finding movie by TMDB ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Inserts a new movie imported from TMDB.
     */
    public int addMovieWithTmdb(Movie movie, int tmdbId, String posterPath, String backdropPath) {
        String sql = "INSERT INTO movies (title, genre, duration, language, rating, release_date, tmdb_id, tmdb_poster_path, tmdb_backdrop_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getGenre());
            stmt.setInt(3, movie.getDuration());
            stmt.setString(4, movie.getLanguage());
            stmt.setBigDecimal(5, movie.getRating());
            stmt.setDate(6, movie.getReleaseDate());
            stmt.setInt(7, tmdbId);
            stmt.setString(8, posterPath);
            stmt.setString(9, backdropPath);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error adding TMDB movie: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates the metadata of an existing movie from TMDB.
     */
    public boolean updateTmdbMetadata(int movieId, com.movieticket.model.TmdbMovie tmdb) {
        String sql = "UPDATE movies SET title = ?, genre = ?, language = ?, rating = ?, " +
                     "tmdb_id = ?, tmdb_poster_path = ?, tmdb_backdrop_path = ?, tmdb_vote_avg = ?, tmdb_vote_count = ?, tmdb_popularity = ?, tmdb_last_updated = CURRENT_TIMESTAMP " +
                     "WHERE movie_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tmdb.getTitle());
            stmt.setString(2, tmdb.getPrimaryGenre());
            stmt.setString(3, tmdb.getOriginalLanguage());
            
            // Convert voteAvg to BigDecimal
            java.math.BigDecimal voteAvg = java.math.BigDecimal.valueOf(tmdb.getVoteAverage());
            voteAvg = voteAvg.setScale(1, java.math.RoundingMode.HALF_UP);
            stmt.setBigDecimal(4, voteAvg);
            
            stmt.setInt(5, tmdb.getTmdbId());
            stmt.setString(6, tmdb.getPosterPath());
            stmt.setString(7, tmdb.getBackdropPath());
            stmt.setBigDecimal(8, voteAvg);
            stmt.setInt(9, tmdb.getVoteCount());
            java.math.BigDecimal popularity = java.math.BigDecimal.valueOf(tmdb.getPopularity());
            popularity = popularity.setScale(4, java.math.RoundingMode.HALF_UP);
            stmt.setBigDecimal(10, popularity);
            stmt.setInt(11, movieId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error updating TMDB metadata: " + e.getMessage());
            return false;
        }
    }
}
