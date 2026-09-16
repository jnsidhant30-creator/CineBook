package com.movieticket.model;

public class Seat {
    private int seatId;
    private int theatreId;
    private int screenId;   // Phase 5: FK to screens table
    private String screenNumber = "Screen 1";
    private String rowName = "A";
    private String seatNumber;
    private String seatType = "REGULAR";
    private String status = "AVAILABLE";

    public Seat() {
    }

    public Seat(int seatId, int theatreId, String seatNumber, String status) {
        this.seatId = seatId;
        this.theatreId = theatreId;
        this.seatNumber = seatNumber;
        this.status = status != null ? status : "AVAILABLE";
        this.rowName = deriveRow(seatNumber);
        this.seatType = deriveCategory(this.rowName);
    }

    public Seat(int seatId, int theatreId, String screenNumber, String rowName, String seatNumber, String seatType, String status) {
        this.seatId = seatId;
        this.theatreId = theatreId;
        this.screenNumber = screenNumber != null ? screenNumber : "Screen 1";
        this.rowName = rowName != null ? rowName : deriveRow(seatNumber);
        this.seatNumber = seatNumber;
        this.seatType = (seatType != null && !seatType.isEmpty()) ? seatType : deriveCategory(this.rowName);
        this.status = status != null ? status : "AVAILABLE";
    }

    private static String deriveCategory(String row) {
        if (row == null || row.isEmpty()) return "REGULAR";
        char r = row.toUpperCase().charAt(0);
        if (r == 'A' || r == 'B') return "REGULAR";
        if (r == 'C' || r == 'D') return "PREMIUM";
        return "VIP";
    }

    private static String deriveRow(String seatNum) {
        if (seatNum == null || seatNum.trim().isEmpty()) return "A";
        String trimmed = seatNum.trim();
        StringBuilder sb = new StringBuilder();
        for (char c : trimmed.toCharArray()) {
            if (Character.isLetter(c)) {
                sb.append(c);
            } else {
                break;
            }
        }
        return sb.length() > 0 ? sb.toString().toUpperCase() : "A";
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(int theatreId) {
        this.theatreId = theatreId;
    }

    public int getScreenId() {
        return screenId;
    }

    public void setScreenId(int screenId) {
        this.screenId = screenId;
    }

    public String getScreenNumber() {
        return screenNumber;
    }

    public void setScreenNumber(String screenNumber) {
        this.screenNumber = screenNumber;
    }

    public String getRowName() {
        return rowName;
    }

    public void setRowName(String rowName) {
        this.rowName = rowName;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
        if (this.rowName == null || this.rowName.isEmpty()) {
            this.rowName = deriveRow(seatNumber);
        }
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "seatId=" + seatId +
                ", theatreId=" + theatreId +
                ", screenNumber='" + screenNumber + '\'' +
                ", rowName='" + rowName + '\'' +
                ", seatNumber='" + seatNumber + '\'' +
                ", seatType='" + seatType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
