package com.movieticket.model;

public class RecommendedMovie {
    private Movie movie;
    private int score;
    private String reason;

    public RecommendedMovie(Movie movie, int score, String reason) {
        this.movie = movie;
        this.score = score;
        this.reason = reason;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
