package com.movieticket;

import com.movieticket.util.MovieTrailerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MovieTrailerServiceTest {

    @Test
    @DisplayName("Verify official trailers for required 5 key movies")
    public void testKeyMoviesTrailers() {
        MovieTrailerService service = MovieTrailerService.getInstance();

        String[][] keyMovies = {
            {"Avatar", "2009"},
            {"Avengers: Endgame", "2019"},
            {"Inception", "2010"},
            {"Interstellar", "2014"},
            {"The Dark Knight", "2008"}
        };

        for (String[] entry : keyMovies) {
            String title = entry[0];
            String year = entry[1];

            String url = service.getTrailerUrl(title, year);
            assertNotNull(url, "Trailer URL must not be null for key movie: " + title);
            assertTrue(url.startsWith("http://") || url.startsWith("https://"), "Trailer URL must be a valid http/https URL for: " + title);
            assertTrue(url.contains("youtube.com") || url.contains("youtu.be"), "Trailer URL must link to a valid video platform for: " + title);
            System.out.println("Verified Trailer [" + title + " (" + year + ")]: " + url);
        }
    }

    @Test
    @DisplayName("Verify trailer caching works idempotently")
    public void testTrailerCaching() {
        MovieTrailerService service = MovieTrailerService.getInstance();

        String url1 = service.getTrailerUrl("Avatar", "2009");
        String url2 = service.getTrailerUrl("Avatar", "2009");

        assertNotNull(url1);
        assertEquals(url1, url2, "Cached trailer URL must match subsequent calls");
    }

    @Test
    @DisplayName("Verify handling of null or blank title gracefully")
    public void testNullOrBlankTitle() {
        MovieTrailerService service = MovieTrailerService.getInstance();

        assertNull(service.getTrailerUrl(null, "2020"));
        assertNull(service.getTrailerUrl("", "2020"));
        assertNull(service.getTrailerUrl("   ", null));
    }
}
