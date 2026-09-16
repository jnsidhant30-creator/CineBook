package com.movieticket.util;

import com.movieticket.dao.MovieDAO;
import com.movieticket.dao.NotificationDAO;
import com.movieticket.dao.UpcomingMovieDAO;
import com.movieticket.dao.UserDAO;
import com.movieticket.model.Movie;
import com.movieticket.model.Notification;
import com.movieticket.model.UpcomingMovie;
import com.movieticket.model.User;
import com.movieticket.util.discovery.MovieDiscoveryProvider;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MovieMonitoringService {

    private final MovieDiscoveryProvider discoveryProvider;
    private final UpcomingMovieDAO upcomingMovieDAO;
    private final MovieDAO movieDAO;
    private final UserDAO userDAO;
    private final NotificationDAO notificationDAO;
    
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    private Timestamp lastCheckTime = null;

    public MovieMonitoringService(MovieDiscoveryProvider discoveryProvider, 
                                  UpcomingMovieDAO upcomingMovieDAO,
                                  MovieDAO movieDAO,
                                  UserDAO userDAO,
                                  NotificationDAO notificationDAO) {
        this.discoveryProvider = discoveryProvider;
        this.upcomingMovieDAO = upcomingMovieDAO;
        this.movieDAO = movieDAO;
        this.userDAO = userDAO;
        this.notificationDAO = notificationDAO;
    }

    public void startAutomaticMonitoring() {
        // Run check every 24 hours
        scheduler.scheduleAtFixedRate(this::checkForNewMovies, 0, 24, TimeUnit.HOURS);
    }

    public void stopAutomaticMonitoring() {
        scheduler.shutdown();
    }

    public synchronized CheckResult checkForNewMovies() {
        CheckResult result = new CheckResult();
        LocalDate today = LocalDate.now();
        // Discovery Window: Today to +2 months
        LocalDate upcomingEnd = today.plusMonths(2);
        
        List<UpcomingMovie> candidates = discoveryProvider.discoverUpcomingMovies(today, upcomingEnd);
        
        for (UpcomingMovie candidate : candidates) {
            boolean isDuplicate = false;
            
            // 1. Check if already in upcoming_movies
            if (candidate.getImdbId() != null) {
                UpcomingMovie existing = upcomingMovieDAO.getUpcomingMovieByImdbId(candidate.getImdbId());
                if (existing != null) {
                    isDuplicate = true;
                }
            }
            if (!isDuplicate && candidate.getTitle() != null && candidate.getReleaseDate() != null) {
                UpcomingMovie existing = upcomingMovieDAO.getUpcomingMovieByTitleAndDate(candidate.getTitle(), candidate.getReleaseDate());
                if (existing != null) {
                    isDuplicate = true;
                }
            }
            
            // 2. Check if already in main movies table
            if (!isDuplicate) {
                List<Movie> existingMovies = movieDAO.getAllMovies();
                for (Movie m : existingMovies) {
                    if (candidate.getImdbId() != null && candidate.getImdbId().equals(m.getImdbId())) {
                        isDuplicate = true;
                        break;
                    }
                    if (m.getTitle().equalsIgnoreCase(candidate.getTitle()) &&
                        m.getReleaseDate() != null && m.getReleaseDate().equals(candidate.getReleaseDate())) {
                        isDuplicate = true;
                        break;
                    }
                }
            }
            
            if (isDuplicate) {
                result.alreadyKnown++;
                continue;
            }
            
            // 3. Insert new candidate
            boolean success = upcomingMovieDAO.addUpcomingMovie(candidate);
            if (success) {
                result.newFound++;
                notifyAdmins(candidate);
            } else {
                result.failed++;
            }
        }
        
        lastCheckTime = new Timestamp(System.currentTimeMillis());
        return result;
    }
    
    private void notifyAdmins(UpcomingMovie movie) {
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            if ("ADMIN".equals(u.getRole())) {
                Notification n = new Notification(
                        0,
                        u.getUserId(),
                        "New Upcoming Movie: " + movie.getTitle(),
                        "A new upcoming movie was discovered. Release Date: " + movie.getReleaseDate() + ". Go to Upcoming Movies dashboard to review and add to CineBook.",
                        "SYSTEM",
                        false,
                        null
                );
                notificationDAO.createNotification(n);
            }
        }
    }
    
    public Timestamp getLastCheckTime() {
        return lastCheckTime;
    }
    
    public static class CheckResult {
        public int newFound = 0;
        public int alreadyKnown = 0;
        public int failed = 0;
    }
}
