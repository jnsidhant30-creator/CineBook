package com.movieticket.model;

/**
 * OmdbMovie.java — Lightweight POJO representing a movie returned by the OMDb API.
 *
 * This is a separate, read-only data carrier used only within the OMDb integration layer.
 * It does NOT replace, extend, or duplicate the existing {@link Movie} class.
 * When an admin imports an OMDb result, its fields are mapped into the existing Movie
 * model and persisted via the existing MovieDAO.
 *
 * Fields marked "N/A" by OMDb are normalized to null before being stored.
 */
public class OmdbMovie {

    /** IMDb identifier (e.g. "tt0499549"). Primary external key for duplicate prevention. */
    private String imdbId;

    /** Movie title as returned by OMDb. */
    private String title;

    /** Release year (string, may contain range like "2009–2012" for series). */
    private String year;

    /** Comma-separated genres (e.g. "Action, Adventure, Fantasy"). */
    private String genre;

    /** Director name(s). */
    private String director;

    /** Main cast (comma-separated). */
    private String actors;

    /** Full plot synopsis. */
    private String plot;

    /** Language(s) of the movie (e.g. "English, Spanish"). */
    private String language;

    /** Country/countries of origin. */
    private String country;

    /** Direct URL to OMDb poster image, or null if unavailable / "N/A". */
    private String posterUrl;

    /** IMDb rating out of 10 (e.g. "7.9"), or null if not available. */
    private String imdbRating;

    /** IMDb vote count formatted string (e.g. "1,234,567"), or null if not available. */
    private String imdbVotes;

    /** Runtime in minutes as a string (e.g. "162 min"), or null. */
    private String runtime;

    public OmdbMovie() {}

    // =========================================================================
    // Getters & Setters
    // =========================================================================

    public String getImdbId() { return imdbId; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }

    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getImdbRating() { return imdbRating; }
    public void setImdbRating(String imdbRating) { this.imdbRating = imdbRating; }

    public String getImdbVotes() { return imdbVotes; }
    public void setImdbVotes(String imdbVotes) { this.imdbVotes = imdbVotes; }

    public String getRuntime() { return runtime; }
    public void setRuntime(String runtime) { this.runtime = runtime; }

    /**
     * Attempts to parse the runtime string (e.g. "162 min") into an integer number of minutes.
     * Returns 0 if the runtime is null, "N/A", or unparseable.
     */
    public int getRuntimeMinutes() {
        if (runtime == null || runtime.isBlank() || "N/A".equalsIgnoreCase(runtime)) return 0;
        try {
            String cleaned = runtime.trim().replaceAll("[^0-9].*", "").trim();
            return cleaned.isEmpty() ? 0 : Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Returns the IMDb rating as a double, or 0.0 if unavailable.
     */
    public double getImdbRatingDouble() {
        if (imdbRating == null || imdbRating.isBlank() || "N/A".equalsIgnoreCase(imdbRating)) return 0.0;
        try {
            return Double.parseDouble(imdbRating.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Returns the first language listed (before the first comma), for mapping to Movie.language.
     */
    public String getPrimaryLanguage() {
        if (language == null || language.isBlank() || "N/A".equalsIgnoreCase(language)) return "English";
        int comma = language.indexOf(',');
        return (comma > 0 ? language.substring(0, comma) : language).trim();
    }

    /**
     * Returns the first genre listed (before the first comma), for mapping to Movie.genre.
     */
    public String getPrimaryGenre() {
        if (genre == null || genre.isBlank() || "N/A".equalsIgnoreCase(genre)) return "General";
        int comma = genre.indexOf(',');
        return (comma > 0 ? genre.substring(0, comma) : genre).trim();
    }

    @Override
    public String toString() {
        return "OmdbMovie{" +
                "imdbId='" + imdbId + '\'' +
                ", title='" + title + '\'' +
                ", year='" + year + '\'' +
                ", genre='" + genre + '\'' +
                ", imdbRating='" + imdbRating + '\'' +
                '}';
    }
}
