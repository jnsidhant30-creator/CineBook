package com.movieticket.model;

import java.sql.Date;
import java.sql.Timestamp;

public class UpcomingMovie {
    private int id;
    private String title;
    private Date releaseDate;
    private String imdbId;
    private String posterUrl;
    private String genre;
    private String description;
    private String status; // NEW, VIEWED, APPROVED, IGNORED
    private Timestamp discoveredAt;
    private Timestamp lastChecked;
    private String source;

    public UpcomingMovie() {}

    public UpcomingMovie(int id, String title, Date releaseDate, String imdbId, String posterUrl, String genre, String description, String status, Timestamp discoveredAt, Timestamp lastChecked, String source) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.imdbId = imdbId;
        this.posterUrl = posterUrl;
        this.genre = genre;
        this.description = description;
        this.status = status;
        this.discoveredAt = discoveredAt;
        this.lastChecked = lastChecked;
        this.source = source;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Date getReleaseDate() { return releaseDate; }
    public void setReleaseDate(Date releaseDate) { this.releaseDate = releaseDate; }

    public String getImdbId() { return imdbId; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(Timestamp discoveredAt) { this.discoveredAt = discoveredAt; }

    public Timestamp getLastChecked() { return lastChecked; }
    public void setLastChecked(Timestamp lastChecked) { this.lastChecked = lastChecked; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
