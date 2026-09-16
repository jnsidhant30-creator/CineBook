package com.movieticket.util.discovery;

import com.movieticket.model.UpcomingMovie;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A simulated implementation of the MovieDiscoveryProvider.
 * In a production scenario where an API like TMDb is used, this class would perform
 * HTTP requests. Here, we simulate discovering upcoming and recent movies for testing.
 */
public class SimulatedDiscoveryProvider implements MovieDiscoveryProvider {

    @Override
    public List<UpcomingMovie> discoverUpcomingMovies(LocalDate fromDate, LocalDate toDate) {
        List<UpcomingMovie> discovered = new ArrayList<>();
        
        LocalDate today = LocalDate.now();

        // Simulate 1 recent movie
        UpcomingMovie m1 = new UpcomingMovie();
        m1.setTitle("Deadpool & Wolverine");
        m1.setReleaseDate(Date.valueOf(today.minusDays(15)));
        m1.setImdbId("tt6263850");
        m1.setGenre("Action, Comedy");
        m1.setSource("SIMULATED");
        discovered.add(m1);

        // Simulate 2 upcoming movies
        UpcomingMovie m2 = new UpcomingMovie();
        m2.setTitle("Gladiator II");
        m2.setReleaseDate(Date.valueOf(today.plusDays(30)));
        m2.setImdbId("tt9218128");
        m2.setGenre("Action, Adventure");
        m2.setSource("SIMULATED");
        discovered.add(m2);

        UpcomingMovie m3 = new UpcomingMovie();
        m3.setTitle("Kraven the Hunter");
        m3.setReleaseDate(Date.valueOf(today.plusDays(50)));
        m3.setImdbId("tt8743628");
        m3.setGenre("Action, Adventure");
        m3.setSource("SIMULATED");
        discovered.add(m3);

        // Filter the simulated list to only return movies that fall within the requested window
        List<UpcomingMovie> filtered = new ArrayList<>();
        for (UpcomingMovie m : discovered) {
            LocalDate rd = m.getReleaseDate().toLocalDate();
            if ((rd.isEqual(fromDate) || rd.isAfter(fromDate)) &&
                (rd.isEqual(toDate) || rd.isBefore(toDate))) {
                filtered.add(m);
            }
        }
        
        return filtered;
    }
}
