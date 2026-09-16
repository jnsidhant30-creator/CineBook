package com.movieticket.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class Show {
    private int showId;
    private int movieId;
    private int theatreId;
    private int screenId;        // Phase 5: FK to screens table
    private LocalDate showDate;
    private LocalTime showTime;
    private LocalTime endTime;
    private String screenNumber = "Screen 1";
    private String status = "ACTIVE";
    private BigDecimal ticketPrice;
    private BigDecimal premiumPrice;
    private BigDecimal vipPrice;

    public Show() {
    }

    public Show(int showId, int movieId, int theatreId, LocalDate showDate, LocalTime showTime, BigDecimal ticketPrice) {
        this.showId = showId;
        this.movieId = movieId;
        this.theatreId = theatreId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.ticketPrice = ticketPrice;
    }

    public Show(int showId, int movieId, int theatreId, LocalDate showDate, LocalTime showTime, LocalTime endTime, String screenNumber, String status, BigDecimal ticketPrice) {
        this.showId = showId;
        this.movieId = movieId;
        this.theatreId = theatreId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.endTime = endTime;
        this.screenNumber = screenNumber != null ? screenNumber : "Screen 1";
        this.status = status != null ? status : "ACTIVE";
        this.ticketPrice = ticketPrice;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
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

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public LocalTime getShowTime() {
        return showTime;
    }

    public void setShowTime(LocalTime showTime) {
        this.showTime = showTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getScreenNumber() {
        return screenNumber;
    }

    public void setScreenNumber(String screenNumber) {
        this.screenNumber = screenNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public BigDecimal getPremiumPrice() {
        return premiumPrice;
    }

    public void setPremiumPrice(BigDecimal premiumPrice) {
        this.premiumPrice = premiumPrice;
    }

    public BigDecimal getVipPrice() {
        return vipPrice;
    }

    public void setVipPrice(BigDecimal vipPrice) {
        this.vipPrice = vipPrice;
    }

    @Override
    public String toString() {
        return "Show{" +
                "showId=" + showId +
                ", movieId=" + movieId +
                ", theatreId=" + theatreId +
                ", showDate=" + showDate +
                ", showTime=" + showTime +
                ", endTime=" + endTime +
                ", screenNumber='" + screenNumber + '\'' +
                ", status='" + status + '\'' +
                ", ticketPrice=" + ticketPrice +
                ", premiumPrice=" + premiumPrice +
                ", vipPrice=" + vipPrice +
                '}';
    }
}
