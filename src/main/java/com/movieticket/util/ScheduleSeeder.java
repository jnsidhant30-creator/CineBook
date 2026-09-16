package com.movieticket.util;

import com.movieticket.dao.*;
import com.movieticket.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ScheduleSeeder {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("CINEBOOK 2-DAY SCHEDULE GENERATOR");
        System.out.println("========================================");

        MovieDAO movieDAO = new MovieDAO();
        TheatreDAO theatreDAO = new TheatreDAO();
        ScreenDAO screenDAO = new ScreenDAO();
        ShowDAO showDAO = new ShowDAO();
        SeatDAO seatDAO = new SeatDAO();

        List<Movie> allMovies = movieDAO.getAllMovies();
        if (allMovies.isEmpty()) {
            System.out.println("No movies found in database. Exiting.");
            return;
        }

        String[] targetTheatres = {"PVR Cinemas", "INOX", "DD Cinemas", "CineBook Cinemas"};
        List<Theatre> theatres = new ArrayList<>();

        // Ensure theatres exist
        for (String tName : targetTheatres) {
            Theatre found = null;
            for (Theatre t : theatreDAO.getAllTheatres()) {
                if (t.getTheatreName().equalsIgnoreCase(tName)) {
                    found = t;
                    break;
                }
            }
            if (found == null) {
                Theatre t = new Theatre();
                t.setTheatreName(tName);
                t.setLocation("City Center");
                t.setCity("Metropolis");
                t.setContactNumber("1234567890");
                t.setTotalSeats(300);
                t.setStatus("ACTIVE");
                int id = theatreDAO.addTheatre(t);
                t.setTheatreId(id);
                theatres.add(t);
            } else {
                theatres.add(found);
            }
        }

        int screensUsed = 0;
        int showsCreated = 0;
        int duplicatesSkipped = 0;
        int invalidRecords = 0;
        
        Map<String, int[]> showsCountMap = new LinkedHashMap<>();
        for (String tName : targetTheatres) {
            showsCountMap.put(tName, new int[]{0, 0}); // [0]: Monday, [1]: Tuesday
        }

        LocalDate day1 = LocalDate.of(2026, 9, 7);
        LocalDate day2 = LocalDate.of(2026, 9, 8);

        Map<String, Integer> basePrices = new HashMap<>();
        basePrices.put("PVR Cinemas", 180);
        basePrices.put("INOX", 170);
        basePrices.put("DD Cinemas", 150);
        basePrices.put("CineBook Cinemas", 130);

        Show sampleShow = null;

        for (Theatre theatre : theatres) {
            // Ensure 3 screens
            List<Screen> currentScreens = screenDAO.getScreensByTheatre(theatre.getTheatreId());
            List<Screen> myScreens = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                String sName = "Screen " + i;
                Screen foundS = null;
                for (Screen s : currentScreens) {
                    if (s.getScreenName().equalsIgnoreCase(sName)) {
                        foundS = s;
                        break;
                    }
                }
                if (foundS == null) {
                    Screen s = new Screen();
                    s.setTheatreId(theatre.getTheatreId());
                    s.setScreenName(sName);
                    s.setScreenType("STANDARD");
                    s.setCapacity(96);
                    s.setStatus("ACTIVE");
                    int sid = screenDAO.addScreen(s);
                    s.setScreenId(sid);
                    myScreens.add(s);
                    
                    // Generate 96 seats
                    generateSeatsForScreen(seatDAO, theatre.getTheatreId(), sid);
                } else {
                    myScreens.add(foundS);
                    // check if seats exist, if not generate
                    boolean hasSeats = false;
                    for (Seat seat : seatDAO.getSeatsByTheatre(theatre.getTheatreId())) {
                        if (seat.getScreenNumber() != null && seat.getScreenNumber().equals(foundS.getScreenName())) {
                            hasSeats = true;
                            break;
                        }
                    }
                    if (!hasSeats) {
                        generateSeatsForScreen(seatDAO, theatre.getTheatreId(), foundS.getScreenId());
                    }
                }
            }

            screensUsed += myScreens.size();

            // Schedule shows
            int movieIndex = 0;
            for (int day = 0; day < 2; day++) {
                LocalDate showDate = (day == 0) ? day1 : day2;
                
                // Distribute showtimes
                LocalTime[] times;
                if (day == 0) {
                    times = new LocalTime[]{
                        LocalTime.of(10, 0), LocalTime.of(13, 0), LocalTime.of(16, 0), LocalTime.of(19, 0), LocalTime.of(22, 0)
                    };
                } else {
                    times = new LocalTime[]{
                        LocalTime.of(9, 30), LocalTime.of(12, 30), LocalTime.of(15, 30), LocalTime.of(18, 30), LocalTime.of(21, 30)
                    };
                }

                for (Screen screen : myScreens) {
                    for (LocalTime time : times) {
                        Movie m = allMovies.get(movieIndex % allMovies.size());
                        movieIndex++;

                        int duration = m.getDuration();
                        if (duration <= 0) duration = 120;
                        LocalTime endTime = time.plusMinutes(duration);

                        // Check overlap
                        List<Show> existingShows = showDAO.getShowsByTheatreAndDate(theatre.getTheatreId(), showDate);
                        boolean overlap = false;
                        for (Show ex : existingShows) {
                            if (ex.getScreenNumber().equals(screen.getScreenName())) {
                                LocalTime exEnd = ex.getEndTime() != null ? ex.getEndTime() : ex.getShowTime().plusMinutes(120);
                                if (ex.getShowTime().isBefore(endTime) && exEnd.isAfter(time)) {
                                    overlap = true;
                                    break;
                                }
                            }
                        }

                        if (overlap) {
                            invalidRecords++;
                            continue;
                        }

                        // Check exact duplicate
                        boolean duplicate = false;
                        for (Show ex : existingShows) {
                            if (ex.getMovieId() == m.getMovieId() && 
                                ex.getScreenNumber().equals(screen.getScreenName()) &&
                                ex.getShowTime().equals(time)) {
                                duplicate = true;
                                break;
                            }
                        }

                        if (duplicate) {
                            duplicatesSkipped++;
                            continue;
                        }

                        // Create show
                        int regPrice = basePrices.get(theatre.getTheatreName());
                        // Apply reasonable premium for evening/night shows (>= 18:00)
                        if (time.isAfter(LocalTime.of(17, 59))) {
                            regPrice += 20;
                        }

                        double premiumPrice = regPrice + 70;
                        double vipPrice = regPrice + 170;

                        Show show = new Show(0, m.getMovieId(), theatre.getTheatreId(),
                                showDate, time, endTime, screen.getScreenName(), "SCHEDULED", java.math.BigDecimal.valueOf(regPrice));
                        show.setScreenId(screen.getScreenId());
                        show.setPremiumPrice(java.math.BigDecimal.valueOf(premiumPrice));
                        show.setVipPrice(java.math.BigDecimal.valueOf(vipPrice));

                        int generatedShowId = showDAO.addShow(show);
                        if (generatedShowId > 0) {
                            showsCreated++;
                            showsCountMap.get(theatre.getTheatreName())[day]++;
                            if (sampleShow == null) {
                                sampleShow = show;
                            }
                        } else {
                            invalidRecords++;
                        }
                    }
                }
            }
        }

        // Output Report
        System.out.println("========================================");
        System.out.println("CINEBOOK 2-DAY SCHEDULE REPORT");
        System.out.println("========================================");
        System.out.println("Date 1: Monday, 07 September 2026");
        System.out.println("Date 2: Tuesday, 08 September 2026");
        System.out.println();
        System.out.println("Cinemas: " + targetTheatres.length);
        System.out.println("Movies scheduled: " + allMovies.size());
        System.out.println("Screens used: " + screensUsed);
        System.out.println("Shows created: " + showsCreated);
        System.out.println("Duplicates skipped: " + duplicatesSkipped);
        System.out.println("Invalid records: " + invalidRecords);
        System.out.println("========================================");

        for (String tName : targetTheatres) {
            System.out.println(tName);
            System.out.println("-------------------------");
            System.out.println("Movies: " + allMovies.size()); // It cycles through all available movies
            System.out.println("Shows Monday: " + showsCountMap.get(tName)[0]);
            System.out.println("Shows Tuesday: " + showsCountMap.get(tName)[1]);
            System.out.println();
        }

        if (sampleShow != null) {
            System.out.println("========================================");
            System.out.println("Sample Show Data:");
            System.out.println("Movie: " + movieDAO.getMovieById(sampleShow.getMovieId()).get().getTitle());
            
            Theatre t = null;
            for(Theatre th : theatreDAO.getAllTheatres()) {
                if (th.getTheatreId() == sampleShow.getTheatreId()) t = th;
            }
            System.out.println("Cinema: " + t.getTheatreName());
            System.out.println("Screen: " + sampleShow.getScreenNumber());
            System.out.println("Date: " + sampleShow.getShowDate());
            System.out.println("Time: " + sampleShow.getShowTime());
            System.out.println("Regular: ₹" + sampleShow.getTicketPrice());
            System.out.println("Premium: ₹" + sampleShow.getPremiumPrice());
            System.out.println("VIP: ₹" + sampleShow.getVipPrice());
            System.out.println("Seats: 96");
            System.out.println("========================================");
        }
    }

    private static void generateSeatsForScreen(SeatDAO seatDAO, int theatreId, int screenId) {
        String[] regularRows = {"A", "B", "C", "D", "E"};
        for (String r : regularRows) {
            for (int i = 1; i <= 12; i++) {
                Seat seat = new Seat();
                seat.setTheatreId(theatreId);
                seat.setScreenId(screenId);
                seat.setRowName(r);
                seat.setSeatNumber(r + i);
                seat.setSeatType("REGULAR");
                seat.setStatus("AVAILABLE");
                seatDAO.addSeat(seat);
            }
        }
        
        String[] premiumRows = {"F", "G"};
        for (String r : premiumRows) {
            for (int i = 1; i <= 12; i++) {
                Seat seat = new Seat();
                seat.setTheatreId(theatreId);
                seat.setScreenId(screenId);
                seat.setRowName(r);
                seat.setSeatNumber(r + i);
                seat.setSeatType("PREMIUM");
                seat.setStatus("AVAILABLE");
                seatDAO.addSeat(seat);
            }
        }
        
        String[] vipRows = {"H"};
        for (String r : vipRows) {
            for (int i = 1; i <= 12; i++) {
                Seat seat = new Seat();
                seat.setTheatreId(theatreId);
                seat.setScreenId(screenId);
                seat.setRowName(r);
                seat.setSeatNumber(r + i);
                seat.setSeatType("VIP");
                seat.setStatus("AVAILABLE");
                seatDAO.addSeat(seat);
            }
        }
    }
}
