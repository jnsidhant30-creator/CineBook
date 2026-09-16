package com.movieticket.model;

import java.math.BigDecimal;

public class Movie {
    private int movieId;
    private String title;
    private String genre;
    private int duration;
    private String language;
    private BigDecimal rating;
    private java.sql.Date releaseDate;

    // OMDb integration fields (nullable — only set when movie was imported from OMDb)
    private String imdbId;    // e.g. "tt0499549"
    private String posterUrl; // OMDb poster image URL

    // TMDB integration fields (nullable)
    private Integer tmdbId;
    private String tmdbPosterPath;
    private String tmdbBackdropPath;
    private BigDecimal tmdbVoteAvg;
    private Integer tmdbVoteCount;
    private BigDecimal tmdbPopularity;
    private java.sql.Timestamp tmdbLastUpdated;

    public Movie() {
    }

    public Movie(int movieId, String title, String genre, int duration, String language, BigDecimal rating) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.language = language;
        this.rating = rating;
    }

    public Movie(int movieId, String title, String genre, int duration, String language, BigDecimal rating, java.sql.Date releaseDate) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.language = language;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public java.sql.Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(java.sql.Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    // TMDB Getters and Setters
    public Integer getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(Integer tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getTmdbPosterPath() {
        return tmdbPosterPath;
    }

    public void setTmdbPosterPath(String tmdbPosterPath) {
        this.tmdbPosterPath = tmdbPosterPath;
    }

    public String getTmdbBackdropPath() {
        return tmdbBackdropPath;
    }

    public void setTmdbBackdropPath(String tmdbBackdropPath) {
        this.tmdbBackdropPath = tmdbBackdropPath;
    }

    public BigDecimal getTmdbVoteAvg() {
        return tmdbVoteAvg;
    }

    public void setTmdbVoteAvg(BigDecimal tmdbVoteAvg) {
        this.tmdbVoteAvg = tmdbVoteAvg;
    }

    public Integer getTmdbVoteCount() {
        return tmdbVoteCount;
    }

    public void setTmdbVoteCount(Integer tmdbVoteCount) {
        this.tmdbVoteCount = tmdbVoteCount;
    }

    public BigDecimal getTmdbPopularity() {
        return tmdbPopularity;
    }

    public void setTmdbPopularity(BigDecimal tmdbPopularity) {
        this.tmdbPopularity = tmdbPopularity;
    }

    public java.sql.Timestamp getTmdbLastUpdated() {
        return tmdbLastUpdated;
    }

    public void setTmdbLastUpdated(java.sql.Timestamp tmdbLastUpdated) {
        this.tmdbLastUpdated = tmdbLastUpdated;
    }

    public String getStatusCalculated() {
        if (releaseDate == null) return "NOW SHOWING";
        java.time.LocalDate rel = releaseDate.toLocalDate();
        java.time.LocalDate today = java.time.LocalDate.now();
        return rel.isAfter(today) ? "UPCOMING" : "NOW SHOWING";
    }

    @Override
    public String toString() {
        return "Movie{" +
                "movieId=" + movieId +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", duration=" + duration +
                ", language='" + language + '\'' +
                ", rating=" + rating +
                ", releaseDate=" + releaseDate +
                '}';
    }
}
