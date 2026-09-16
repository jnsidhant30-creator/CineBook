package com.movieticket.util;

import com.movieticket.model.TmdbCastMember;
import com.movieticket.model.TmdbCrewMember;
import com.movieticket.model.TmdbMovie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * TmdbMovieService.java — Business logic layer for TMDB API interactions.
 */
public class TmdbMovieService {

    private final TmdbApiClient apiClient;
    
    // Cache for genre mappings
    private final Map<Integer, String> genreMap = new HashMap<>();

    private static class CacheEntry<T> {
        final T data;
        final long timestamp;
        CacheEntry(T data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > 10 * 60 * 1000; // 10 minutes
        }
    }

    private final Map<String, CacheEntry<List<TmdbMovie>>> listCache = new ConcurrentHashMap<>();
    private final Map<Integer, CacheEntry<TmdbMovie>> detailCache = new ConcurrentHashMap<>();

    private List<TmdbMovie> getCachedList(String key, Supplier<List<TmdbMovie>> fetcher) {
        CacheEntry<List<TmdbMovie>> entry = listCache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.data;
        }
        List<TmdbMovie> data = fetcher.get();
        if (data != null && !data.isEmpty()) {
            listCache.put(key, new CacheEntry<>(data));
        }
        return data;
    }

    public TmdbMovieService() {
        this.apiClient = new TmdbApiClient();
    }
    
    private void ensureGenresLoaded() {
        if (!genreMap.isEmpty()) return;
        try {
            String json = apiClient.getJson("/genre/movie/list", "language=en-US");
            String genresJson = TmdbJsonParser.field(json, "genres");
            List<String> objects = TmdbJsonParser.splitObjects(genresJson);
            for (String obj : objects) {
                String idStr = TmdbJsonParser.field(obj, "id");
                String name = TmdbJsonParser.field(obj, "name");
                if (idStr != null && name != null) {
                    genreMap.put(Integer.parseInt(idStr), name);
                }
            }
        } catch (Exception e) {
            System.err.println("[TmdbMovieService] Failed to load genres: " + e.getMessage());
        }
    }

    public List<TmdbMovie> searchMovies(String query) {
        if (query == null || query.isBlank()) return new ArrayList<>();
        ensureGenresLoaded();
        
        String encodedQuery = query.trim().replace(" ", "%20");
        String json = apiClient.getJson("/search/movie", "query=" + encodedQuery + "&language=en-US&page=1&include_adult=false");
        return parseMovieList(json);
    }

    public TmdbMovie getMovieDetails(int tmdbId) {
        CacheEntry<TmdbMovie> entry = detailCache.get(tmdbId);
        if (entry != null && !entry.isExpired()) {
            return entry.data;
        }

        ensureGenresLoaded();
        String json = apiClient.getJson("/movie/" + tmdbId, "append_to_response=credits,videos,similar,recommendations&language=en-US");
        
        TmdbMovie movie = parseSingleMovie(json);
        if (movie == null) return null;
        
        // Parse credits
        String creditsJson = TmdbJsonParser.field(json, "credits");
        if (creditsJson != null) {
            String castArray = TmdbJsonParser.field(creditsJson, "cast");
            List<String> castObjects = TmdbJsonParser.splitObjects(castArray);
            List<TmdbCastMember> castMembers = new ArrayList<>();
            for (int i = 0; i < Math.min(10, castObjects.size()); i++) { // limit to top 10
                String obj = castObjects.get(i);
                castMembers.add(new TmdbCastMember(
                        TmdbJsonParser.field(obj, "name"),
                        TmdbJsonParser.field(obj, "character"),
                        TmdbJsonParser.field(obj, "profile_path")
                ));
            }
            movie.setCast(castMembers);
            
            String crewArray = TmdbJsonParser.field(creditsJson, "crew");
            List<String> crewObjects = TmdbJsonParser.splitObjects(crewArray);
            List<TmdbCrewMember> crewMembers = new ArrayList<>();
            for (String obj : crewObjects) {
                String job = TmdbJsonParser.field(obj, "job");
                if ("Director".equals(job) || "Screenplay".equals(job) || "Producer".equals(job)) {
                    crewMembers.add(new TmdbCrewMember(
                            TmdbJsonParser.field(obj, "name"),
                            job,
                            TmdbJsonParser.field(obj, "profile_path")
                    ));
                }
            }
            movie.setCrew(crewMembers);
        }
        
        // Parse videos
        String videosJson = TmdbJsonParser.field(json, "videos");
        if (videosJson != null) {
            String resultsArray = TmdbJsonParser.field(videosJson, "results");
            List<String> videoObjects = TmdbJsonParser.splitObjects(resultsArray);
            
            String bestKey = null;
            int bestScore = -1;
            
            for (String obj : videoObjects) {
                String site = TmdbJsonParser.field(obj, "site");
                String type = TmdbJsonParser.field(obj, "type");
                String officialStr = TmdbJsonParser.field(obj, "official");
                String key = TmdbJsonParser.field(obj, "key");
                
                if ("YouTube".equals(site) && key != null && !key.isBlank()) {
                    int score = 0;
                    boolean isOfficial = "true".equalsIgnoreCase(officialStr);
                    boolean isTrailer = "Trailer".equals(type);
                    
                    if (isTrailer && isOfficial) score = 3;
                    else if (isTrailer) score = 2;
                    else if (isOfficial) score = 1;
                    
                    if (score > bestScore) {
                        bestScore = score;
                        bestKey = key;
                    }
                }
            }
            
            if (bestKey != null) {
                movie.setTrailerKey(bestKey);
            }
        }
        
        // Parse similar
        String similarJson = TmdbJsonParser.field(json, "similar");
        if (similarJson != null) {
            movie.setSimilar(parseMovieList(similarJson));
        }
        
        // Parse recommendations
        String recsJson = TmdbJsonParser.field(json, "recommendations");
        if (recsJson != null) {
            movie.setRecommendations(parseMovieList(recsJson));
        }
        
        
        if (movie != null) {
            detailCache.put(tmdbId, new CacheEntry<>(movie));
        }
        return movie;
    }

    public List<TmdbMovie> getPopularMovies() {
        return getCachedList("popular", () -> parseMovieList(apiClient.getJson("/movie/popular", "language=en-US&page=1")));
    }

    public List<TmdbMovie> getTopRatedMovies() {
        return getCachedList("top_rated", () -> parseMovieList(apiClient.getJson("/movie/top_rated", "language=en-US&page=1")));
    }

    public List<TmdbMovie> getUpcomingMovies() {
        return getCachedList("upcoming", () -> parseMovieList(apiClient.getJson("/movie/upcoming", "language=en-US&page=1")));
    }

    public List<TmdbMovie> getTrendingMovies() {
        return getCachedList("trending", () -> parseMovieList(apiClient.getJson("/trending/movie/day", "language=en-US")));
    }

    public List<TmdbMovie> getNowPlayingMovies() {
        return getCachedList("now_playing", () -> parseMovieList(apiClient.getJson("/movie/now_playing", "language=en-US&page=1")));
    }

    public List<TmdbMovie> discoverMovies(String params) {
        // discover is typically dynamic, so we might not cache it, or cache by params
        return getCachedList("discover_" + params, () -> parseMovieList(apiClient.getJson("/discover/movie", params)));
    }

    private List<TmdbMovie> parseMovieList(String json) {
        ensureGenresLoaded();
        List<TmdbMovie> movies = new ArrayList<>();
        if (json == null) return movies;
        
        String resultsArray = TmdbJsonParser.field(json, "results");
        List<String> objects = TmdbJsonParser.splitObjects(resultsArray);
        
        for (String obj : objects) {
            TmdbMovie m = parseSingleMovie(obj);
            if (m != null) movies.add(m);
        }
        return movies;
    }

    private TmdbMovie parseSingleMovie(String obj) {
        if (obj == null) return null;
        TmdbMovie m = new TmdbMovie();
        
        String idStr = TmdbJsonParser.field(obj, "id");
        if (idStr == null) return null;
        m.setTmdbId(Integer.parseInt(idStr));
        
        m.setTitle(TmdbJsonParser.field(obj, "title"));
        m.setOriginalTitle(TmdbJsonParser.field(obj, "original_title"));
        m.setOverview(TmdbJsonParser.field(obj, "overview"));
        m.setReleaseDate(TmdbJsonParser.field(obj, "release_date"));
        m.setPosterPath(TmdbJsonParser.field(obj, "poster_path"));
        m.setBackdropPath(TmdbJsonParser.field(obj, "backdrop_path"));
        m.setOriginalLanguage(TmdbJsonParser.field(obj, "original_language"));
        m.setTagline(TmdbJsonParser.field(obj, "tagline"));
        m.setStatus(TmdbJsonParser.field(obj, "status"));
        
        String runtimeStr = TmdbJsonParser.field(obj, "runtime");
        if (runtimeStr != null && !runtimeStr.isEmpty()) {
            try { m.setRuntime(Integer.parseInt(runtimeStr)); } catch (Exception ignored) {}
        }
        
        String voteAvgStr = TmdbJsonParser.field(obj, "vote_average");
        if (voteAvgStr != null && !voteAvgStr.isEmpty()) {
            try { m.setVoteAverage(Double.parseDouble(voteAvgStr)); } catch (Exception ignored) {}
        }
        
        String voteCountStr = TmdbJsonParser.field(obj, "vote_count");
        if (voteCountStr != null && !voteCountStr.isEmpty()) {
            try { m.setVoteCount(Integer.parseInt(voteCountStr)); } catch (Exception ignored) {}
        }
        
        String popularityStr = TmdbJsonParser.field(obj, "popularity");
        if (popularityStr != null && !popularityStr.isEmpty()) {
            try { m.setPopularity(Double.parseDouble(popularityStr)); } catch (Exception ignored) {}
        }
        
        // Genres logic - can be from 'genre_ids' (array of ints) or 'genres' (array of objects)
        List<String> genres = new ArrayList<>();
        String genreIdsArray = TmdbJsonParser.field(obj, "genre_ids");
        if (genreIdsArray != null) {
            // It's [1, 2, 3] etc.
            String clean = genreIdsArray.replace("[", "").replace("]", "");
            for (String idPart : clean.split(",")) {
                try {
                    int gid = Integer.parseInt(idPart.trim());
                    String gname = genreMap.get(gid);
                    if (gname != null) genres.add(gname);
                } catch (Exception ignored) {}
            }
        } else {
            String genresArray = TmdbJsonParser.field(obj, "genres");
            if (genresArray != null) {
                List<String> gObjects = TmdbJsonParser.splitObjects(genresArray);
                for (String gobj : gObjects) {
                    String gname = TmdbJsonParser.field(gobj, "name");
                    if (gname != null) genres.add(gname);
                }
            }
        }
        m.setGenres(genres);
        
        return m;
    }

    public void syncLegacyMovies(java.util.List<com.movieticket.model.Movie> movies, java.util.function.Consumer<String> progressCallback, Runnable onComplete) {
        com.movieticket.dao.MovieDAO dao = new com.movieticket.dao.MovieDAO();
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        for (int i = 0; i < movies.size(); i++) {
            com.movieticket.model.Movie movie = movies.get(i);
            
            if (movie.getTmdbId() != null && movie.getTmdbId() > 0) {
                skipCount++;
                continue;
            }

            final int current = i + 1;
            final int total = movies.size();
            
            try {
                if (progressCallback != null) {
                    javax.swing.SwingUtilities.invokeLater(() -> progressCallback.accept("Syncing " + current + " / " + total + ": " + movie.getTitle()));
                }

                String query = movie.getTitle();

                List<TmdbMovie> results = searchMovies(query);
                if (results != null && !results.isEmpty()) {
                    TmdbMovie bestMatch = null;
                    for (TmdbMovie r : results) {
                        boolean titleMatch = r.getTitle().equalsIgnoreCase(movie.getTitle()) || r.getOriginalTitle().equalsIgnoreCase(movie.getTitle());
                        
                        if (!titleMatch) {
                            titleMatch = r.getTitle().toLowerCase().contains(movie.getTitle().toLowerCase()) || 
                                         movie.getTitle().toLowerCase().contains(r.getTitle().toLowerCase());
                        }

                        boolean yearMatch = true;
                        if (movie.getReleaseDate() != null && r.getReleaseDate() != null && !r.getReleaseDate().isBlank()) {
                            int localYear = movie.getReleaseDate().toLocalDate().getYear();
                            try {
                                int tmdbYear = Integer.parseInt(r.getReleaseDate().substring(0, 4));
                                yearMatch = Math.abs(localYear - tmdbYear) <= 1;
                            } catch (Exception ignored) {}
                        }

                        if (titleMatch && yearMatch) {
                            bestMatch = r;
                            break;
                        }
                    }

                    if (bestMatch != null) {
                        // We must fetch full details to get the runtime and other data for TMDB?
                        // Wait, updateTmdbMetadata just needs poster, backdrop, vote avg, vote count, popularity.
                        // searchMovies result has all of these! So we don't strictly need a full detail fetch for sync.
                        dao.updateTmdbMetadata(movie.getMovieId(), bestMatch);
                        // However, to make it even more complete in the DB, maybe fetch details?
                        // No, the DAO just takes the model.
                        successCount++;
                    } else {
                        System.out.println("[TMDB Sync] No confident match found for: " + movie.getTitle());
                        failCount++;
                    }
                } else {
                    failCount++;
                }

                Thread.sleep(100);

            } catch (Exception e) {
                System.err.println("[TMDB Sync] Error syncing movie: " + movie.getTitle() + " - " + e.getMessage());
                failCount++;
            }
        }

        if (progressCallback != null) {
            final String finalMsg = "TMDB Sync Complete: " + successCount + " updated, " + failCount + " failed, " + skipCount + " skipped.";
            javax.swing.SwingUtilities.invokeLater(() -> progressCallback.accept(finalMsg));
        }

        if (onComplete != null) {
            javax.swing.SwingUtilities.invokeLater(onComplete);
        }
    }
}
