package com.movieticket.model;

/**
 * Screen model — represents a cinema screen within a Theatre.
 * Part of the multi-theatre hierarchy: Theatre -> Screen -> Show.
 */
public class Screen {

    private int screenId;
    private int theatreId;
    private String screenName;
    private String screenType;  // STANDARD, IMAX, 4DX, DOLBY, GOLD_CLASS
    private int capacity;
    private String status;      // ACTIVE, MAINTENANCE, CLOSED

    public Screen() {
        this.screenType = "STANDARD";
        this.status = "ACTIVE";
    }

    public Screen(int screenId, int theatreId, String screenName, String screenType, int capacity, String status) {
        this.screenId = screenId;
        this.theatreId = theatreId;
        this.screenName = screenName;
        this.screenType = screenType != null ? screenType : "STANDARD";
        this.capacity = capacity;
        this.status = status != null ? status : "ACTIVE";
    }

    // --- Getters & Setters ---

    public int getScreenId() { return screenId; }
    public void setScreenId(int screenId) { this.screenId = screenId; }

    public int getTheatreId() { return theatreId; }
    public void setTheatreId(int theatreId) { this.theatreId = theatreId; }

    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }

    public String getScreenType() { return screenType; }
    public void setScreenType(String screenType) { this.screenType = screenType; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /** Convenience display name: "Screen 1 (IMAX)" */
    public String getDisplayName() {
        return screenName + " (" + screenType + ")";
    }

    @Override
    public String toString() {
        return "Screen{" +
                "screenId=" + screenId +
                ", theatreId=" + theatreId +
                ", screenName='" + screenName + '\'' +
                ", screenType='" + screenType + '\'' +
                ", capacity=" + capacity +
                ", status='" + status + '\'' +
                '}';
    }
}
