package com.movieticket.model;

public class Theatre {
    private int theatreId;
    private String theatreName;
    private String location;
    private int totalSeats;
    // Phase 5: Multi-theatre fields
    private String city;
    private String contactNumber;
    private int numScreens;
    private String status; // ACTIVE, INACTIVE

    public Theatre() {
        this.status = "ACTIVE";
        this.numScreens = 1;
    }

    /** Backward-compatible constructor used by existing code */
    public Theatre(int theatreId, String theatreName, String location, int totalSeats) {
        this.theatreId = theatreId;
        this.theatreName = theatreName;
        this.location = location;
        this.totalSeats = totalSeats;
        this.status = "ACTIVE";
        this.numScreens = 1;
    }

    /** Full constructor including new Phase 5 fields */
    public Theatre(int theatreId, String theatreName, String location, int totalSeats,
                   String city, String contactNumber, int numScreens, String status) {
        this.theatreId = theatreId;
        this.theatreName = theatreName;
        this.location = location;
        this.totalSeats = totalSeats;
        this.city = city;
        this.contactNumber = contactNumber;
        this.numScreens = numScreens;
        this.status = status != null ? status : "ACTIVE";
    }

    public int getTheatreId() { return theatreId; }
    public void setTheatreId(int theatreId) { this.theatreId = theatreId; }

    public String getTheatreName() { return theatreName; }
    public void setTheatreName(String theatreName) { this.theatreName = theatreName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public int getNumScreens() { return numScreens; }
    public void setNumScreens(int numScreens) { this.numScreens = numScreens; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /** Display name including city when available */
    public String getDisplayName() {
        if (city != null && !city.isBlank() && !city.equals("Unknown City")) {
            return theatreName + " — " + city;
        }
        return theatreName;
    }

    @Override
    public String toString() {
        return "Theatre{" +
                "theatreId=" + theatreId +
                ", theatreName='" + theatreName + '\'' +
                ", location='" + location + '\'' +
                ", city='" + city + '\'' +
                ", totalSeats=" + totalSeats +
                ", status='" + status + '\'' +
                '}';
    }
}
