package com.movieticket.util;

import com.movieticket.model.OmdbMovie;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * OmdbService.java — HTTP client for the OMDb REST API.
 *
 * Uses Java 21's built-in {@link java.net.http.HttpClient} — no extra Maven dependency.
 * The API key is read ONLY from the environment variable {@code OMDB_API_KEY} via
 * {@link System#getenv(String)}.  It is NEVER stored in source code, SQL, logs or the GUI.
 *
 * Threading: all public methods are blocking and must be called from a background thread
 * (e.g. inside a SwingWorker). They must never be called on the Swing EDT.
 *
 * Error handling:
 *  - Missing API key           → throws OmdbException with clear message
 *  - Network/timeout failure   → throws OmdbException wrapping the cause
 *  - HTTP non-200 response     → throws OmdbException with status code
 *  - OMDb Response="False"     → returns empty list / returns null
 *  - "N/A" fields              → normalized to null before returning
 */
public class OmdbService {

    private static final String BASE_URL   = "https://www.omdbapi.com/";
    private static final Duration TIMEOUT  = Duration.ofSeconds(10);
    private static final String ENV_KEY    = "OMDB_API_KEY";

    /** Singleton HttpClient (thread-safe, reusable). */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    // =========================================================================
    // Exception type
    // =========================================================================

    /**
     * Checked exception thrown for all OMDb errors so callers can handle them distinctly
     * from unexpected RuntimeExceptions.
     */
    public static class OmdbException extends Exception {
        public OmdbException(String message) { super(message); }
        public OmdbException(String message, Throwable cause) { super(message, cause); }
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Searches OMDb for movies matching {@code title}.
     * Calls: {@code ?apikey=KEY&s=TITLE&type=movie}
     *
     * @param title movie title search term (URL-encoded automatically)
     * @return list of matching {@link OmdbMovie} objects (may be empty, never null)
     * @throws OmdbException if the API key is missing, network fails, or OMDb returns an error
     */
    public List<OmdbMovie> searchMovies(String title) throws OmdbException {
        String apiKey = getApiKey();
        if (title == null || title.isBlank()) {
            return new ArrayList<>();
        }

        String encoded = URLEncoder.encode(title.trim(), StandardCharsets.UTF_8);
        String url = BASE_URL + "?apikey=" + apiKey + "&s=" + encoded + "&type=movie";

        String json = fetchJson(url);
        return parseSearchResults(json);
    }

    /**
     * Fetches full movie details from OMDb by IMDb ID.
     * Calls: {@code ?apikey=KEY&i=IMDB_ID&plot=full}
     *
     * @param imdbId the IMDb identifier (e.g. "tt0499549")
     * @return populated {@link OmdbMovie}, or {@code null} if OMDb returns Response=False
     * @throws OmdbException if the API key is missing, network fails, or HTTP error occurs
     */
    public OmdbMovie getMovieDetails(String imdbId) throws OmdbException {
        String apiKey = getApiKey();
        if (imdbId == null || imdbId.isBlank()) {
            return null;
        }

        String url = BASE_URL + "?apikey=" + apiKey + "&i=" + imdbId.trim() + "&plot=full";
        String json = fetchJson(url);
        return parseMovieDetail(json);
    }

    /**
     * Fetches full movie details from OMDb by exact title and year.
     * Calls: {@code ?apikey=KEY&t=TITLE&y=YEAR&plot=full}
     *
     * @param title exact movie title (e.g. "Avatar")
     * @param year  exact movie year (e.g. "2009"), or null/blank if not specified
     * @return populated {@link OmdbMovie}, or {@code null} if OMDb returns Response=False
     * @throws OmdbException if the API key is missing, network fails, or HTTP error occurs
     */
    public OmdbMovie getMovieByTitleAndYear(String title, String year) throws OmdbException {
        String apiKey = getApiKey();
        if (title == null || title.isBlank()) {
            return null;
        }

        String encodedTitle = URLEncoder.encode(title.trim(), StandardCharsets.UTF_8);
        String yearParam = (year != null && !year.isBlank())
                ? "&y=" + URLEncoder.encode(year.trim(), StandardCharsets.UTF_8)
                : "";
        String url = BASE_URL + "?apikey=" + apiKey + "&t=" + encodedTitle + yearParam + "&plot=full";
        String json = fetchJson(url);
        return parseMovieDetail(json);
    }

    // =========================================================================
    // API Key
    // =========================================================================

    /**
     * Reads the API key from the environment variable OMDB_API_KEY.
     * Never returns null — throws OmdbException if missing or blank.
     * The key value is never logged or exposed.
     */
    private String getApiKey() throws OmdbException {
        String key = System.getenv(ENV_KEY);
        if (key == null || key.isBlank() || "YOUR_FULL_ACTIVATED_KEY".equalsIgnoreCase(key.trim()) || "YOUR_OMDB_API_KEY".equalsIgnoreCase(key.trim())) {
            key = System.getProperty(ENV_KEY);
        }
        
        System.out.println("OMDb API key configured: " + (key != null && !key.isBlank()));
        System.out.println("OMDb API key length: " + (key == null ? 0 : key.trim().length()));
        
        if (key == null || key.isBlank()) {
            throw new OmdbException(
                "OMDb API key not configured. " +
                "Please set the environment variable OMDB_API_KEY and restart the application."
            );
        }
        return key.trim();
    }

    // =========================================================================
    // HTTP
    // =========================================================================

    private String fetchJson(String url) throws OmdbException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            if (status == 401) {
                System.out.println("HTTP status: 401");
                throw new OmdbException("OMDb API authentication failed. Please verify the OMDB_API_KEY configuration.");
            } else if (status != 200) {
                throw new OmdbException("OMDb API returned HTTP " + status + ". Please try again later.");
            }

            return response.body();

        } catch (OmdbException e) {
            throw e;
        } catch (java.net.http.HttpTimeoutException e) {
            throw new OmdbException("OMDb request timed out. Please check your internet connection.", e);
        } catch (java.io.IOException e) {
            throw new OmdbException("Network error connecting to OMDb: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OmdbException("OMDb request was interrupted.", e);
        } catch (Exception e) {
            throw new OmdbException("Unexpected error contacting OMDb: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // JSON Parsing  (manual, no external library)
    // =========================================================================

    /**
     * Parses the OMDb search response JSON.
     *
     * Example structure:
     * {"Search":[{"Title":"Avatar","Year":"2009","imdbID":"tt0499549","Type":"movie","Poster":"https://..."},...],
     *  "totalResults":"12","Response":"True"}
     */
    private List<OmdbMovie> parseSearchResults(String json) throws OmdbException {
        List<OmdbMovie> results = new ArrayList<>();
        if (json == null || json.isBlank()) return results;

        // Check Response field
        if (extractField(json, "Response").equalsIgnoreCase("False")) {
            // Not an error worth throwing — just no results
            return results;
        }

        // Locate the Search array
        int searchStart = json.indexOf("\"Search\":[");
        if (searchStart == -1) return results;

        int arrStart = json.indexOf('[', searchStart);
        int arrEnd   = findMatchingBracket(json, arrStart, '[', ']');
        if (arrEnd == -1) return results;

        String arrayContent = json.substring(arrStart + 1, arrEnd);

        // Split into individual JSON objects
        List<String> objects = splitJsonObjects(arrayContent);
        for (String obj : objects) {
            OmdbMovie movie = new OmdbMovie();
            movie.setTitle(  nullIfNA(extractField(obj, "Title")));
            movie.setYear(   nullIfNA(extractField(obj, "Year")));
            movie.setImdbId( nullIfNA(extractField(obj, "imdbID")));
            movie.setPosterUrl(nullIfNA(extractField(obj, "Poster")));
            if (movie.getImdbId() != null) {
                results.add(movie);
            }
        }
        return results;
    }

    /**
     * Parses the OMDb detail response JSON.
     *
     * Returns null if Response=False.
     */
    private OmdbMovie parseMovieDetail(String json) throws OmdbException {
        if (json == null || json.isBlank()) return null;

        if (extractField(json, "Response").equalsIgnoreCase("False")) {
            return null;
        }

        OmdbMovie movie = new OmdbMovie();
        movie.setImdbId(   nullIfNA(extractField(json, "imdbID")));
        movie.setTitle(    nullIfNA(extractField(json, "Title")));
        movie.setYear(     nullIfNA(extractField(json, "Year")));
        movie.setGenre(    nullIfNA(extractField(json, "Genre")));
        movie.setDirector( nullIfNA(extractField(json, "Director")));
        movie.setActors(   nullIfNA(extractField(json, "Actors")));
        movie.setPlot(     nullIfNA(extractField(json, "Plot")));
        movie.setLanguage( nullIfNA(extractField(json, "Language")));
        movie.setCountry(  nullIfNA(extractField(json, "Country")));
        movie.setPosterUrl(nullIfNA(extractField(json, "Poster")));
        movie.setImdbRating(nullIfNA(extractField(json, "imdbRating")));
        movie.setImdbVotes( nullIfNA(extractField(json, "imdbVotes")));
        movie.setRuntime(   nullIfNA(extractField(json, "Runtime")));

        return movie;
    }

    // =========================================================================
    // JSON Helpers  (simple, no regex — safe for OMDb response format)
    // =========================================================================

    /**
     * Extracts a simple string value for {@code "key":"value"} or {@code "key": "value"} pairs.
     * Returns an empty string if the key is not found.
     */
    private String extractField(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return "";

        int colonIdx = json.indexOf(':', keyIdx + search.length());
        if (colonIdx == -1) return "";

        int quoteStart = json.indexOf('"', colonIdx + 1);
        if (quoteStart == -1) return "";

        // Read until the closing unescaped quote
        StringBuilder sb = new StringBuilder();
        for (int i = quoteStart + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case '"'  -> { sb.append('"');  i++; }
                    case '\\'  -> { sb.append('\\'); i++; }
                    case 'n'  -> { sb.append('\n'); i++; }
                    case 'r'  -> { sb.append('\r'); i++; }
                    case 't'  -> { sb.append('\t'); i++; }
                    default   -> sb.append(next); // keep other escapes as-is
                }
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Finds the matching closing bracket/brace for the opening one at {@code startIdx}.
     */
    private int findMatchingBracket(String s, int startIdx, char open, char close) {
        int depth = 0;
        boolean inString = false;
        for (int i = startIdx; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && inString) { i++; continue; } // skip escape
            if (c == '"') { inString = !inString; continue; }
            if (inString) continue;
            if (c == open)  { depth++; }
            if (c == close) { depth--; if (depth == 0) return i; }
        }
        return -1;
    }

    /**
     * Splits a JSON array body (without surrounding brackets) into individual object strings.
     */
    private List<String> splitJsonObjects(String arrayBody) {
        List<String> result = new ArrayList<>();
        int i = 0;
        while (i < arrayBody.length()) {
            int objStart = arrayBody.indexOf('{', i);
            if (objStart == -1) break;
            int objEnd = findMatchingBracket(arrayBody, objStart, '{', '}');
            if (objEnd == -1) break;
            result.add(arrayBody.substring(objStart, objEnd + 1));
            i = objEnd + 1;
        }
        return result;
    }

    /**
     * Returns null if the value equals "N/A", is blank, or is empty. Otherwise returns the value.
     */
    private String nullIfNA(String value) {
        if (value == null || value.isBlank() || "N/A".equalsIgnoreCase(value.trim())) return null;
        return value.trim();
    }
}
