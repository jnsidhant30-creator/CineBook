package com.movieticket.model;

import java.time.LocalDateTime;

public class SeatHold {
    private int id;
    private int showId;
    private int seatId;
    private int userId;
    private LocalDateTime heldAt;
    private LocalDateTime expiresAt;
    private String status; // ACTIVE, EXPIRED, RELEASED, CONVERTED

    public SeatHold() {}

    public SeatHold(int showId, int seatId, int userId, LocalDateTime heldAt, LocalDateTime expiresAt, String status) {
        this.showId = showId;
        this.seatId = seatId;
        this.userId = userId;
        this.heldAt = heldAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }

    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getHeldAt() { return heldAt; }
    public void setHeldAt(LocalDateTime heldAt) { this.heldAt = heldAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
