package com.movieticket.dao;

import com.movieticket.model.*;

import java.util.List;
import java.util.Optional;

public class DAOTest {

    public static void main(String[] args) {
        System.out.println("Starting DAO Layer Tests...\n");

        // 1. UserDAO Test
        System.out.println("--- UserDAO ---");
        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.getAllUsers();
        System.out.println("Total users: " + users.size());
        Optional<User> admin = userDAO.findByUsername("admin");
        admin.ifPresent(u -> System.out.println("Found admin user ID: " + u.getUserId()));

        // 2. MovieDAO Test
        System.out.println("\n--- MovieDAO ---");
        MovieDAO movieDAO = new MovieDAO();
        List<Movie> movies = movieDAO.getAllMovies();
        System.out.println("Total movies: " + movies.size());
        if (!movies.isEmpty()) {
            Movie first = movies.get(0);
            Optional<Movie> found = movieDAO.getMovieById(first.getMovieId());
            found.ifPresent(m -> System.out.println("Found movie by ID: " + m.getTitle()));
            List<Movie> searched = movieDAO.searchMovies("Inception");
            System.out.println("Searched for 'Inception': found " + searched.size());
        }

        // 3. TheatreDAO Test
        System.out.println("\n--- TheatreDAO ---");
        TheatreDAO theatreDAO = new TheatreDAO();
        List<Theatre> theatres = theatreDAO.getAllTheatres();
        System.out.println("Total theatres: " + theatres.size());

        // 4. ShowDAO Test
        System.out.println("\n--- ShowDAO ---");
        ShowDAO showDAO = new ShowDAO();
        List<Show> shows = showDAO.getAllShows();
        System.out.println("Total shows: " + shows.size());
        if (!shows.isEmpty()) {
            System.out.println("First show date/time: " + shows.get(0).getShowDate() + " " + shows.get(0).getShowTime());
        }

        // 5. SeatDAO Test
        System.out.println("\n--- SeatDAO ---");
        SeatDAO seatDAO = new SeatDAO();
        if (!theatres.isEmpty()) {
            int firstTheatreId = theatres.get(0).getTheatreId();
            List<Seat> seats = seatDAO.getSeatsByTheatre(firstTheatreId);
            System.out.println("Total seats in Theatre " + firstTheatreId + ": " + seats.size());
        }

        // 6. BookingDAO Test
        System.out.println("\n--- BookingDAO ---");
        BookingDAO bookingDAO = new BookingDAO();
        List<Booking> bookings = bookingDAO.getAllBookings();
        System.out.println("Total bookings: " + bookings.size());
        
        // 7. BookingSeatDAO Test
        System.out.println("\n--- BookingSeatDAO ---");
        BookingSeatDAO bookingSeatDAO = new BookingSeatDAO();
        if (!bookings.isEmpty()) {
            int firstBookingId = bookings.get(0).getBookingId();
            List<BookingSeat> bSeats = bookingSeatDAO.getDetailsByBookingId(firstBookingId);
            System.out.println("Total seats for Booking " + firstBookingId + ": " + bSeats.size());
        }

        System.out.println("\nDAO Layer Tests Completed.");
    }
}
