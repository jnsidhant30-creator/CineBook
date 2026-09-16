package com.movieticket.model;

import java.time.LocalDateTime;

public class UserPreference {
    private int userId;
    private String preferredGenres;
    private String preferredLanguages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserPreference() {
    }

    public UserPreference(int userId, String preferredGenres, String preferredLanguages) {
        this.userId = userId;
        this.preferredGenres = preferredGenres;
        this.preferredLanguages = preferredLanguages;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPreferredGenres() {
        return preferredGenres;
    }

    public void setPreferredGenres(String preferredGenres) {
        this.preferredGenres = preferredGenres;
    }

    public String getPreferredLanguages() {
        return preferredLanguages;
    }

    public void setPreferredLanguages(String preferredLanguages) {
        this.preferredLanguages = preferredLanguages;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "UserPreference{" +
                "userId=" + userId +
                ", preferredGenres='" + preferredGenres + '\'' +
                ", preferredLanguages='" + preferredLanguages + '\'' +
                '}';
    }
}
