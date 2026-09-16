package com.movieticket;

import com.movieticket.dao.ShowDAO;
import com.movieticket.model.Show;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class TestInsertShow {
    public static void main(String[] args) {
        System.out.println("Attempting to add ONE show...");
        
        ShowDAO showDAO = new ShowDAO();
        
        // Simulating the exact fields from ShowController
        Show show = new Show(
            0,                      // showId
            31,                     // movieId (exists)
            1,                      // theatreId (exists)
            LocalDate.of(2026, 12, 1), // showDate
            LocalTime.of(14, 30),   // startTime
            LocalTime.of(16, 30),   // endTime
            "Screen 1",             // screen
            "ACTIVE",               // status
            new BigDecimal("150.00")// ticketPrice
        );
        // Premium and VIP prices
        show.setPremiumPrice(new BigDecimal("200.00"));
        show.setVipPrice(new BigDecimal("300.00"));
        
        int generatedId = showDAO.addShow(show);
        
        if (generatedId > 0) {
            System.out.println("Show added successfully, ID: " + generatedId);
        } else {
            System.out.println("Unable to add show. Please try again.");
        }
    }
}
