package com.movieticket.model;

import java.util.ArrayList;
import java.util.List;

/**
 * TmdbMovie.java — Rich POJO representing a movie from TMDB.
 * 
 * Used entirely within the TMDB integration layer. Maps to the existing Movie model
 * when imported into the database by an administrator.
 */
public class TmdbMovie {

    private int tmdbId;
    private String title;
    private String originalTitle;
    private String overview;
    private String tagline;
    private String status;
    private String releaseDate;
    private String originalLanguage;
    private int runtime;
    private double voteAverage;
    private int voteCount;
    private double popularity;
    private String posterPath;
    private String backdropPath;
    private String trailerKey;
    private String trailerUrl;
    
    private List<String> genres = new ArrayList<>();
    private List<TmdbCastMember> cast = new ArrayList<>();
    private List<TmdbCrewMember> crew = new ArrayList<>();
    private List<TmdbMovie> similar = new ArrayList<>();
    private List<TmdbMovie> recommendations = new ArrayList<>();

    public TmdbMovie() {}

    public int getTmdbId() { return tmdbId; }
    public void setTmdbId(int tmdbId) { this.tmdbId = tmdbId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOriginalTitle() { return originalTitle; }
    public void setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public int getRuntime() { return runtime; }
    public void setRuntime(int runtime) { this.runtime = runtime; }

    public double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }

    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }

    public double getPopularity() { return popularity; }
    public void setPopularity(double popularity) { this.popularity = popularity; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getBackdropPath() { return backdropPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }

    public String getTrailerKey() { return trailerKey; }
    public void setTrailerKey(String trailerKey) { 
        this.trailerKey = trailerKey; 
        if (trailerKey != null && !trailerKey.isBlank()) {
            this.trailerUrl = "https://www.youtube.com/watch?v=" + trailerKey;
        }
    }

    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public List<TmdbCastMember> getCast() { return cast; }
    public void setCast(List<TmdbCastMember> cast) { this.cast = cast; }

    public List<TmdbCrewMember> getCrew() { return crew; }
    public void setCrew(List<TmdbCrewMember> crew) { this.crew = crew; }

    public List<TmdbMovie> getSimilar() { return similar; }
    public void setSimilar(List<TmdbMovie> similar) { this.similar = similar; }

    public List<TmdbMovie> getRecommendations() { return recommendations; }
    public void setRecommendations(List<TmdbMovie> recommendations) { this.recommendations = recommendations; }
    
    /**
     * Gets the first genre as a primary genre string.
     */
    public String getPrimaryGenre() {
        if (genres != null && !genres.isEmpty()) {
            return genres.get(0);
        }
        return "General";
    }

    /**
     * Returns the release year.
     */
    public String getYear() {
        if (releaseDate != null && releaseDate.length() >= 4) {
            return releaseDate.substring(0, 4);
        }
        return "N/A";
    }
}
