package com.movieticket.util;

import com.movieticket.dao.MovieDAO;
import com.movieticket.dao.RecommendationDAO;
import com.movieticket.dao.BookingDAO;
import com.movieticket.dao.ShowDAO;
import com.movieticket.model.Movie;
import com.movieticket.model.RecommendedMovie;
import com.movieticket.model.UserPreference;
import com.movieticket.model.Booking;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService {
    private final RecommendationDAO recommendationDAO;
    private final MovieDAO movieDAO;
    private final BookingDAO bookingDAO;

    // In-memory cache for user recommendations
    private static final Map<Integer, List<RecommendedMovie>> recommendationCache = new HashMap<>();

    public RecommendationService() {
        this.recommendationDAO = new RecommendationDAO();
        this.movieDAO = new MovieDAO();
        this.bookingDAO = new BookingDAO();
    }
    
    public void clearCache(int userId) {
        recommendationCache.remove(userId);
    }

    public List<RecommendedMovie> getRecommendations(int userId) {
        if (recommendationCache.containsKey(userId)) {
            return recommendationCache.get(userId);
        }

        List<Movie> allMovies = movieDAO.getAllMovies();
        if (allMovies == null || allMovies.isEmpty()) {
            return Collections.emptyList();
        }

        UserPreference pref = recommendationDAO.getUserPreference(userId);
        List<String> watchedGenres = recommendationDAO.getWatchedGenres(userId);
        List<String> watchedLanguages = recommendationDAO.getWatchedLanguages(userId);
        List<Booking> userBookings = bookingDAO.getBookingsByUser(userId);
        
        ShowDAO showDAO = new ShowDAO();
        Set<Integer> bookedMovieIds = new HashSet<>();
        if (userBookings != null) {
            for (Booking b : userBookings) {
                if (b.getShowId() > 0) {
                    showDAO.getShowById(b.getShowId()).ifPresent(show -> {
                        bookedMovieIds.add(show.getMovieId());
                    });
                }
            }
        }

        Set<String> preferredGenres = new HashSet<>();
        if (pref != null && pref.getPreferredGenres() != null) {
            preferredGenres.addAll(Arrays.asList(pref.getPreferredGenres().split(",")));
        }
        preferredGenres.addAll(watchedGenres);

        Set<String> preferredLanguages = new HashSet<>();
        if (pref != null && pref.getPreferredLanguages() != null) {
            preferredLanguages.addAll(Arrays.asList(pref.getPreferredLanguages().split(",")));
        }
        preferredLanguages.addAll(watchedLanguages);

        // Use all movies as a popularity proxy (sorted by rating)
        List<Movie> allMoviesSorted = new ArrayList<>(allMovies);
        allMoviesSorted.sort((a, b) -> {
            java.math.BigDecimal ra = a.getRating() != null ? a.getRating() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal rb = b.getRating() != null ? b.getRating() : java.math.BigDecimal.ZERO;
            return rb.compareTo(ra);
        });
        Set<Integer> popularMovieIds = allMoviesSorted.stream()
                .limit(10).map(Movie::getMovieId).collect(Collectors.toSet());

        List<RecommendedMovie> scoredMovies = new ArrayList<>();

        for (Movie movie : allMovies) {
            if (bookedMovieIds.contains(movie.getMovieId())) {
                continue; // Skip already booked
            }

            int score = 0;
            String reason = "Popular on CineBook";

            if (movie.getGenre() != null && preferredGenres.contains(movie.getGenre())) {
                score += 40;
                reason = "Because you like " + movie.getGenre();
            }

            if (movie.getLanguage() != null && preferredLanguages.contains(movie.getLanguage())) {
                score += 20;
                if (score == 20) reason = "Because you watch in " + movie.getLanguage();
            }

            if (popularMovieIds.contains(movie.getMovieId())) {
                score += 10;
            }

            if (movie.getReleaseDate() != null) {
                LocalDate release = movie.getReleaseDate().toLocalDate();
                LocalDate now = LocalDate.now();
                if (release.isAfter(now.minusDays(30))) {
                    score += 10;
                }
            }

            if (score > 0) {
                scoredMovies.add(new RecommendedMovie(movie, score, reason));
            }
        }

        // Sort by score descending
        scoredMovies.sort((m1, m2) -> Integer.compare(m2.getScore(), m1.getScore()));
        
        // Cache and return top 20
        List<RecommendedMovie> result = scoredMovies.stream().limit(20).collect(Collectors.toList());
        recommendationCache.put(userId, result);
        return result;
    }
    
    public List<RecommendedMovie> getRecommendedForYou(int userId) {
        return getRecommendations(userId).stream()
                .filter(m -> m.getScore() >= 40)
                .collect(Collectors.toList());
    }

    public List<RecommendedMovie> getComingSoonForYou(int userId) {
        LocalDate now = LocalDate.now();
        return getRecommendations(userId).stream()
                .filter(m -> m.getMovie().getReleaseDate() != null && m.getMovie().getReleaseDate().toLocalDate().isAfter(now))
                .collect(Collectors.toList());
    }
    
    public List<Movie> getTopRated() {
        List<Movie> movies = movieDAO.getAllMovies();
        if (movies == null) return Collections.emptyList();
        movies.sort((a, b) -> {
            java.math.BigDecimal ra = a.getRating() != null ? a.getRating() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal rb = b.getRating() != null ? b.getRating() : java.math.BigDecimal.ZERO;
            return rb.compareTo(ra);
        });
        return movies.stream().limit(10).collect(Collectors.toList());
    }
}
