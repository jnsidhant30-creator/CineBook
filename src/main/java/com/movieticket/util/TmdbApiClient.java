package com.movieticket.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * TmdbApiClient.java — Lightweight HTTP client for TMDB API.
 * 
 * Uses Java 21's built-in HttpClient. All methods are blocking and must be
 * called from a background thread (e.g., SwingWorker).
 * Automatically injects the Bearer token from TmdbConfig.
 */
public class TmdbApiClient {

    private final HttpClient httpClient;

    public TmdbApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Thrown when an API request fails.
     */
    public static class TmdbException extends RuntimeException {
        public TmdbException(String message) { super(message); }
        public TmdbException(String message, Throwable cause) { super(message, cause); }
    }

    /**
     * Performs a GET request to the specified endpoint with query parameters.
     * 
     * @param endpoint    The API endpoint path starting with '/' (e.g., "/search/movie").
     * @param queryParams The query string without leading '?' (e.g., "query=Avatar&page=1").
     *                    May be null or empty.
     * @return The JSON response string.
     * @throws TmdbException if the request fails or returns a non-200 status code.
     */
    public String getJson(String endpoint, String queryParams) {
        TmdbConfig config = TmdbConfig.getInstance();
        String urlString = config.getBaseUrl() + endpoint;
        
        if (queryParams != null && !queryParams.isBlank()) {
            urlString += "?" + queryParams + "&api_key=" + config.getApiKey();
        } else {
            urlString += "?api_key=" + config.getApiKey();
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200) {
                return response.body();
            } else if (statusCode == 401) {
                throw new TmdbException("TMDB API Error: Unauthorized (401). Please check your API token.");
            } else if (statusCode == 404) {
                throw new TmdbException("TMDB API Error: Resource not found (404).");
            } else if (statusCode == 429) {
                throw new TmdbException("TMDB API Error: Rate limit exceeded (429). Please try again later.");
            } else {
                throw new TmdbException("TMDB API Error: Unexpected status code " + statusCode + ".\nResponse: " + response.body());
            }

        } catch (TmdbException e) {
            throw e; // rethrow custom exception
        } catch (Exception e) {
            throw new TmdbException("Failed to connect to TMDB API: " + e.getMessage(), e);
        }
    }

    /**
     * Performs a GET request to the specified endpoint without additional query parameters.
     * 
     * @param endpoint The API endpoint path starting with '/' (e.g., "/movie/popular").
     * @return The JSON response string.
     * @throws TmdbException if the request fails or returns a non-200 status code.
     */
    public String getJson(String endpoint) {
        return getJson(endpoint, null);
    }
}
