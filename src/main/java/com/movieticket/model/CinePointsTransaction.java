package com.movieticket.model;

import java.time.LocalDateTime;

public class CinePointsTransaction {
    private int id;
    private int userId;
    private int bookingId;
    private int points;
    private String transactionType; // EARNED, REDEEMED
    private String description;
    private int balanceAfter;
    private LocalDateTime createdAt;
    private Integer showId;
    private String status; // PENDING, AVAILABLE, CANCELLED
    private LocalDateTime showStartTime;

    public CinePointsTransaction() {
    }

    public CinePointsTransaction(int id, int userId, int bookingId, int points, String transactionType, String description, int balanceAfter, LocalDateTime createdAt, Integer showId, String status, LocalDateTime showStartTime) {
        this.id = id;
        this.userId = userId;
        this.bookingId = bookingId;
        this.points = points;
        this.transactionType = transactionType;
        this.description = description;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
        this.showId = showId;
        this.status = status;
        this.showStartTime = showStartTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(int balanceAfter) { this.balanceAfter = balanceAfter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Integer getShowId() { return showId; }
    public void setShowId(Integer showId) { this.showId = showId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getShowStartTime() { return showStartTime; }
    public void setShowStartTime(LocalDateTime showStartTime) { this.showStartTime = showStartTime; }
}
