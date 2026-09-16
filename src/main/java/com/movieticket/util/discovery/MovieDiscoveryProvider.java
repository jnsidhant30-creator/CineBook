package com.movieticket.util.discovery;

import com.movieticket.model.UpcomingMovie;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface defining a movie discovery source.
 * Because OMDb does not natively support an endpoint to fetch all upcoming movies
 * reliably, this architecture allows a specific discovery API (e.g. TMDb, internal DB) 
 * to be cleanly plugged in later.
 */
public interface MovieDiscoveryProvider {
    
    /**
     * Discovers movies released within the given window.
     * 
     * @param fromDate Start of the release date window
     * @param toDate End of the release date window
     * @return List of discovered UpcomingMovie candidates
     */
    List<UpcomingMovie> discoverUpcomingMovies(LocalDate fromDate, LocalDate toDate);
}
