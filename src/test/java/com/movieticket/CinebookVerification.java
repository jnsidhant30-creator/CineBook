package com.movieticket;

import com.movieticket.dao.UserDAO;
import com.movieticket.dao.MovieDAO;
import com.movieticket.dao.TheatreDAO;
import com.movieticket.dao.ShowDAO;
import com.movieticket.dao.SeatDAO;
import com.movieticket.dao.BookingDAO;
import com.movieticket.dao.CouponDAO;
import com.movieticket.dao.CinePointsDAO;
import com.movieticket.model.User;
import com.movieticket.model.Movie;
import com.movieticket.model.Theatre;
import com.movieticket.model.Show;
import com.movieticket.model.Seat;
import com.movieticket.model.Booking;
import com.movieticket.model.Coupon;
import com.movieticket.util.UserSession;
import com.movieticket.util.CouponService;
import com.movieticket.util.CinePointsService;
import com.movieticket.util.SeatHoldService;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class CinebookVerification {
    public static void main(String[] args) {
        System.out.println("=== CINEBOOK VERIFICATION ===");
        try {
            UserDAO userDAO = new UserDAO();
            MovieDAO movieDAO = new MovieDAO();
            ShowDAO showDAO = new ShowDAO();
            TheatreDAO theatreDAO = new TheatreDAO();
            SeatDAO seatDAO = new SeatDAO();
            BookingDAO bookingDAO = new BookingDAO();
            CouponDAO couponDAO = new CouponDAO();
            CinePointsDAO pointsDAO = new CinePointsDAO();
            
            CouponService couponService = new CouponService();
            CinePointsService pointsService = new CinePointsService();
            SeatHoldService holdService = new SeatHoldService();

            // 1. Simulate Login
            Optional<User> optUser = userDAO.findByUsername("john_doe");
            User testUser;
            if (optUser.isPresent()) {
                testUser = optUser.get();
            } else {
                System.out.println("Test user not found.");
                return;
            }
            UserSession.getInstance().startSession(testUser);
            System.out.println("Logged in as: " + testUser.getUsername());

            // 2. Initial Balances
            int initialPoints = pointsService.getBalance(testUser.getUserId());
            System.out.println("Initial CinePoints: " + initialPoints);

            // 3. Find a show and hold seats
            List<Show> shows = showDAO.getAllShows();
            if (shows.isEmpty()) {
                System.out.println("No shows available.");
                return;
            }
            Show testShow = shows.get(0);
            System.out.println("Selected Show ID: " + testShow.getShowId());

            List<Seat> allSeats = seatDAO.getSeatsByTheatre(testShow.getTheatreId());
            List<Integer> bookedSeats = bookingDAO.getBookedSeatIdsForShow(testShow.getShowId());
            
            List<Seat> seatsToBook = new ArrayList<>();
            for (Seat s : allSeats) {
                if (!bookedSeats.contains(s.getSeatId())) {
                    seatsToBook.add(s);
                    if (seatsToBook.size() == 2) break;
                }
            }
            
            if (seatsToBook.size() < 2) {
                System.out.println("Not enough available seats.");
                return;
            }
            
            List<Integer> seatIds = new ArrayList<>();
            for (Seat s : seatsToBook) seatIds.add(s.getSeatId());
            
            List<Integer> failedHolds = holdService.tryHoldSeats(testShow.getShowId(), seatIds, testUser.getUserId());
            System.out.println("Seat Hold Success: " + failedHolds.isEmpty());

            // 4. Calculate Subtotal
            BigDecimal subtotal = new BigDecimal("400.00"); // simulate 2 seats * 200
            System.out.println("Ticket Subtotal: \u20b9" + subtotal);

            // 5. Apply Coupon
            Optional<Coupon> optCoupon = couponDAO.findByCode("WELCOME10");
            BigDecimal couponDiscount = BigDecimal.ZERO;
            if (optCoupon.isPresent()) {
                Coupon coupon = optCoupon.get();
                CouponService.CouponValidationResult cv = couponService.validateCoupon(coupon.getCode(), testUser.getUserId(), subtotal);
                System.out.println("Coupon " + coupon.getCode() + " Validation: " + cv.isValid() + " (" + cv.getMessage() + ")");
                if (cv.isValid()) {
                    couponDiscount = couponService.calculateDiscount(coupon, subtotal);
                    System.out.println("Coupon Discount: -\u20b9" + couponDiscount);
                }
            } else {
                System.out.println("Test coupon not found in DB.");
            }

            BigDecimal remainingAfterCoupon = subtotal.subtract(couponDiscount);

            // 6. Apply CinePoints
            BigDecimal pointsDiscount = BigDecimal.ZERO;
            int pointsToRedeem = 0;
            if (initialPoints >= 100) {
                pointsToRedeem = pointsService.calculateMaxRedeemablePoints(initialPoints, remainingAfterCoupon);
                pointsDiscount = pointsService.calculateDiscount(pointsToRedeem);
                System.out.println("CinePoints to redeem: " + pointsToRedeem);
                System.out.println("CinePoints Discount: -\u20b9" + pointsDiscount);
            }

            BigDecimal finalPayable = remainingAfterCoupon.subtract(pointsDiscount);
            System.out.println("Final Payable: \u20b9" + finalPayable);
            
            // Clean up hold
            holdService.releaseSeats(testShow.getShowId(), seatIds, testUser.getUserId());
            
            System.out.println("=== END TEST ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
