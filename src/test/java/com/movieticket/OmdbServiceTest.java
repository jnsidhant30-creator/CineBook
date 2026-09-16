package com.movieticket;

import com.movieticket.model.OmdbMovie;
import com.movieticket.util.OmdbService;
import com.movieticket.util.OmdbService.OmdbException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OmdbServiceTest.java — Unit tests for OmdbService.
 *
 * All tests use private method reflection to test JSON parsing directly,
 * so NO live network calls are made. The OMDB_API_KEY env var is not required.
 *
 * Tests verify:
 *  1. Missing API key → OmdbException with clear message
 *  2. Response=False in search JSON → empty list (no exception)
 *  3. Valid search JSON → correctly populated OmdbMovie list
 *  4. "N/A" poster in detail JSON → posterUrl == null
 *  5. Valid detail JSON → all fields mapped correctly
 *  6. OmdbMovie.getRuntimeMinutes() → correct parsing
 *  7. OmdbMovie.getPrimaryGenre() / getPrimaryLanguage() → first segment
 */
public class OmdbServiceTest {

    // =========================================================================
    // 1. Missing API key
    // =========================================================================

    @Test
    @DisplayName("searchMovies() throws OmdbException when OMDB_API_KEY is not set")
    public void testMissingApiKey_throwsOmdbException() {
        // Only run this test if the env var is actually absent
        // (if set, we skip to avoid real network calls)
        String key = System.getenv("OMDB_API_KEY");
        if (key != null && !key.isBlank()) {
            System.out.println("[OmdbServiceTest] OMDB_API_KEY is set — skipping missing-key test.");
            return;
        }

        OmdbService service = new OmdbService();
        OmdbException ex = assertThrows(OmdbException.class,
                () -> service.searchMovies("Avatar"),
                "Should throw OmdbException when API key is missing");

        assertNotNull(ex.getMessage(), "Exception message must not be null");
        assertTrue(ex.getMessage().contains("OMDB_API_KEY"),
                "Exception message should mention OMDB_API_KEY");
        assertFalse(ex.getMessage().toLowerCase().contains("apikey"),
                "Exception message must NOT expose the raw key parameter name in URL form");
    }

    @Test
    @DisplayName("getMovieByTitleAndYear() throws OmdbException when OMDB_API_KEY is not set")
    public void testGetMovieByTitleAndYear_missingApiKey_throwsOmdbException() {
        String key = System.getenv("OMDB_API_KEY");
        if (key != null && !key.isBlank()) {
            System.out.println("[OmdbServiceTest] OMDB_API_KEY is set — skipping missing-key test.");
            return;
        }

        OmdbService service = new OmdbService();
        assertThrows(OmdbException.class,
                () -> service.getMovieByTitleAndYear("Avatar", "2009"),
                "Should throw OmdbException when API key is missing");
    }

    // =========================================================================
    // 2. JSON parsing — Response=False → empty list
    // =========================================================================

    @Test
    @DisplayName("parseSearchResults() returns empty list when Response=False")
    public void testSearchResultsResponseFalse_returnsEmptyList() throws Exception {
        String json = "{\"Response\":\"False\",\"Error\":\"Movie not found!\"}";

        List<OmdbMovie> results = invokeParseSearchResults(json);

        assertNotNull(results, "Result list must not be null");
        assertTrue(results.isEmpty(), "Result list must be empty when Response=False");
    }

    // =========================================================================
    // 3. JSON parsing — valid search results
    // =========================================================================

    @Test
    @DisplayName("parseSearchResults() maps Avatar search results correctly")
    public void testSearchResultsValid_mapsFields() throws Exception {
        String json = "{"
                + "\"Search\":["
                + "{\"Title\":\"Avatar\",\"Year\":\"2009\",\"imdbID\":\"tt0499549\","
                + "\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/test.jpg\"},"
                + "{\"Title\":\"Avatar: The Way of Water\",\"Year\":\"2022\",\"imdbID\":\"tt1630029\","
                + "\"Type\":\"movie\",\"Poster\":\"N/A\"}"
                + "],"
                + "\"totalResults\":\"2\",\"Response\":\"True\"}";

        List<OmdbMovie> results = invokeParseSearchResults(json);

        assertEquals(2, results.size(), "Should parse 2 search results");

        OmdbMovie avatar = results.get(0);
        assertEquals("Avatar", avatar.getTitle());
        assertEquals("2009", avatar.getYear());
        assertEquals("tt0499549", avatar.getImdbId());
        assertEquals("https://m.media-amazon.com/images/M/test.jpg", avatar.getPosterUrl());

        OmdbMovie sequel = results.get(1);
        assertEquals("Avatar: The Way of Water", sequel.getTitle());
        assertEquals("tt1630029", sequel.getImdbId());
        assertNull(sequel.getPosterUrl(), "Poster 'N/A' must be normalized to null");
    }

    // =========================================================================
    // 4. Poster "N/A" → null
    // =========================================================================

    @Test
    @DisplayName("parseMovieDetail() normalizes 'N/A' poster to null")
    public void testMovieDetail_naPosternormalized() throws Exception {
        String json = "{"
                + "\"Title\":\"Test Movie\","
                + "\"Year\":\"2009\","
                + "\"imdbID\":\"tt9999999\","
                + "\"Genre\":\"Action\","
                + "\"Director\":\"John Doe\","
                + "\"Actors\":\"Actor One, Actor Two\","
                + "\"Plot\":\"A great story.\","
                + "\"Language\":\"English\","
                + "\"Country\":\"USA\","
                + "\"Poster\":\"N/A\","
                + "\"imdbRating\":\"7.5\","
                + "\"imdbVotes\":\"100,000\","
                + "\"Runtime\":\"120 min\","
                + "\"Response\":\"True\""
                + "}";

        OmdbMovie movie = invokeParseMovieDetail(json);

        assertNotNull(movie, "Parsed movie must not be null");
        assertNull(movie.getPosterUrl(), "N/A poster must be normalized to null");
        assertEquals("7.5", movie.getImdbRating());
        assertEquals("tt9999999", movie.getImdbId());
    }

    // =========================================================================
    // 5. Valid detail JSON — all fields
    // =========================================================================

    @Test
    @DisplayName("parseMovieDetail() maps all Avatar fields correctly")
    public void testMovieDetail_allFieldsMapped() throws Exception {
        String json = "{"
                + "\"Title\":\"Avatar\","
                + "\"Year\":\"2009\","
                + "\"Genre\":\"Action, Adventure, Fantasy\","
                + "\"Director\":\"James Cameron\","
                + "\"Actors\":\"Sam Worthington, Zoe Saldana, Sigourney Weaver\","
                + "\"Plot\":\"A paraplegic Marine dispatched to the moon Pandora...\","
                + "\"Language\":\"English, Spanish\","
                + "\"Country\":\"United States, United Kingdom\","
                + "\"Poster\":\"https://poster.url/avatar.jpg\","
                + "\"imdbRating\":\"7.9\","
                + "\"imdbVotes\":\"1,234,567\","
                + "\"Runtime\":\"162 min\","
                + "\"imdbID\":\"tt0499549\","
                + "\"Response\":\"True\""
                + "}";

        OmdbMovie movie = invokeParseMovieDetail(json);

        assertNotNull(movie);
        assertEquals("Avatar", movie.getTitle());
        assertEquals("2009", movie.getYear());
        assertEquals("tt0499549", movie.getImdbId());
        assertEquals("7.9", movie.getImdbRating());
        assertEquals("162 min", movie.getRuntime());
        assertEquals("https://poster.url/avatar.jpg", movie.getPosterUrl());
        assertEquals("James Cameron", movie.getDirector());
        assertEquals("Action, Adventure, Fantasy", movie.getGenre());
        assertEquals("English, Spanish", movie.getLanguage());
    }

    // =========================================================================
    // 6. OmdbMovie helper methods
    // =========================================================================

    @Test
    @DisplayName("OmdbMovie.getRuntimeMinutes() parses '162 min' → 162")
    public void testGetRuntimeMinutes() {
        OmdbMovie m = new OmdbMovie();
        m.setRuntime("162 min");
        assertEquals(162, m.getRuntimeMinutes());
    }

    @Test
    @DisplayName("OmdbMovie.getRuntimeMinutes() handles null / N/A gracefully → 0")
    public void testGetRuntimeMinutes_nullOrNA() {
        OmdbMovie m = new OmdbMovie();
        m.setRuntime(null);
        assertEquals(0, m.getRuntimeMinutes());

        m.setRuntime("N/A");
        assertEquals(0, m.getRuntimeMinutes());
    }

    @Test
    @DisplayName("OmdbMovie.getPrimaryGenre() returns first genre segment")
    public void testGetPrimaryGenre() {
        OmdbMovie m = new OmdbMovie();
        m.setGenre("Action, Adventure, Fantasy");
        assertEquals("Action", m.getPrimaryGenre());
    }

    @Test
    @DisplayName("OmdbMovie.getPrimaryLanguage() returns first language")
    public void testGetPrimaryLanguage() {
        OmdbMovie m = new OmdbMovie();
        m.setLanguage("English, Spanish");
        assertEquals("English", m.getPrimaryLanguage());
    }

    @Test
    @DisplayName("OmdbMovie.getImdbRatingDouble() parses '7.9' correctly")
    public void testGetImdbRatingDouble() {
        OmdbMovie m = new OmdbMovie();
        m.setImdbRating("7.9");
        assertEquals(7.9, m.getImdbRatingDouble(), 0.001);
    }

    @Test
    @DisplayName("OmdbMovie.getImdbRatingDouble() returns 0.0 for N/A or null")
    public void testGetImdbRatingDouble_naOrNull() {
        OmdbMovie m = new OmdbMovie();
        m.setImdbRating("N/A");
        assertEquals(0.0, m.getImdbRatingDouble(), 0.001);

        m.setImdbRating(null);
        assertEquals(0.0, m.getImdbRatingDouble(), 0.001);
    }

    // =========================================================================
    // Reflection helpers — invoke private methods for white-box parsing tests
    // =========================================================================

    @SuppressWarnings("unchecked")
    private List<OmdbMovie> invokeParseSearchResults(String json) throws Exception {
        OmdbService service = new OmdbService();
        Method method = OmdbService.class.getDeclaredMethod("parseSearchResults", String.class);
        method.setAccessible(true);
        return (List<OmdbMovie>) method.invoke(service, json);
    }

    private OmdbMovie invokeParseMovieDetail(String json) throws Exception {
        OmdbService service = new OmdbService();
        Method method = OmdbService.class.getDeclaredMethod("parseMovieDetail", String.class);
        method.setAccessible(true);
        return (OmdbMovie) method.invoke(service, json);
    }
}
