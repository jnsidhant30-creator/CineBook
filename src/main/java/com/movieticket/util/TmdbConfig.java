package com.movieticket.util;

/**
 * TmdbConfig.java — Centralized TMDB API configuration.
 *
 * Reads credentials ONLY from environment variables:
 *   TMDB_API_KEY                (v3 API Key)
 *   TMDB_API_BASE_URL           (optional override, default https://api.themoviedb.org/3)
 *   TMDB_IMAGE_BASE_URL         (optional override, default https://image.tmdb.org/t/p/)
 *
 * The token is NEVER hard-coded, logged, printed to console, or shown in the UI.
 * Validation is performed once at startup; callers receive a clear message if absent.
 *
 * Thread-safe: all fields are final after initialization.
 */
public final class TmdbConfig {

    // =========================================================================
    // Environment Variable Names (only used for lookup — never printed in full)
    // =========================================================================
    private static final String ENV_TOKEN    = "TMDB_API_KEY";
    private static final String ENV_BASE_URL = "TMDB_API_BASE_URL";
    private static final String ENV_IMG_URL  = "TMDB_IMAGE_BASE_URL";

    // =========================================================================
    // Defaults
    // =========================================================================
    public static final String DEFAULT_BASE_URL  = "https://api.themoviedb.org/3";
    public static final String DEFAULT_IMAGE_URL = "https://image.tmdb.org/t/p/";

    // =========================================================================
    // Cached configuration (initialized once)
    // =========================================================================
    private static volatile TmdbConfig instance;

    private final String token;
    private final String baseUrl;
    private final String imageBaseUrl;

    // =========================================================================
    // Exception Type
    // =========================================================================

    /**
     * Thrown when TMDB_API_KEY is missing or blank.
     * Message is user-friendly and never contains the actual token.
     */
    public static class MissingTokenException extends RuntimeException {
        public MissingTokenException(String message) { super(message); }
    }

    // =========================================================================
    // Singleton
    // =========================================================================

    private TmdbConfig() {
        String raw = System.getenv(ENV_TOKEN);
        if (raw == null || raw.isBlank()) {
            // Also check Java system properties as fallback (for IDE launcher configs)
            raw = System.getProperty(ENV_TOKEN);
        }
        if (raw == null || raw.isBlank()) {
            throw new MissingTokenException(
                "TMDB API token not configured.\n" +
                "Please set the environment variable: " + ENV_TOKEN + "\n" +
                "Then restart the application.\n" +
                "You can obtain a free token at: https://www.themoviedb.org/settings/api"
            );
        }
        this.token = raw.trim();

        String base = System.getenv(ENV_BASE_URL);
        this.baseUrl = (base != null && !base.isBlank()) ? base.trim() : DEFAULT_BASE_URL;

        String img = System.getenv(ENV_IMG_URL);
        this.imageBaseUrl = (img != null && !img.isBlank()) ? img.trim() : DEFAULT_IMAGE_URL;

        System.out.println("[TmdbConfig] TMDB token configured: true (length=" + this.token.length() + ")");
        System.out.println("[TmdbConfig] TMDB base URL: " + this.baseUrl);
        System.out.println("[TmdbConfig] TMDB image URL: " + this.imageBaseUrl);
    }

    /**
     * Returns the singleton TmdbConfig.
     *
     * @throws MissingTokenException if TMDB_API_KEY is not set
     */
    public static TmdbConfig getInstance() {
        if (instance == null) {
            synchronized (TmdbConfig.class) {
                if (instance == null) {
                    instance = new TmdbConfig();
                }
            }
        }
        return instance;
    }

    /**
     * Returns true if the TMDB token environment variable is set (non-blank).
     * Never throws. Useful for conditional UI enabling.
     */
    public static boolean isConfigured() {
        try {
            String raw = System.getenv(ENV_TOKEN);
            if (raw == null || raw.isBlank()) {
                raw = System.getProperty(ENV_TOKEN);
            }
            return raw != null && !raw.isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the API key for TMDB API requests.
     * This value must NEVER be logged, printed, or shown in the UI.
     */
    public String getApiKey() {
        return token;
    }

    /**
     * Returns the TMDB API base URL (e.g., https://api.themoviedb.org/3).
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the TMDB image CDN base URL (e.g., https://image.tmdb.org/t/p/).
     */
    public String getImageBaseUrl() {
        return imageBaseUrl;
    }

    /**
     * Constructs a full poster URL from a poster_path and a size code.
     *
     * @param posterPath the path returned by TMDB API (e.g. "/abc123.jpg")
     * @param size       the TMDB size code (e.g. "w342", "w500", "w780", "original")
     * @return full URL string, or null if posterPath is null/blank
     */
    public String buildImageUrl(String posterPath, String size) {
        if (posterPath == null || posterPath.isBlank()) return null;
        String path = posterPath.startsWith("/") ? posterPath : "/" + posterPath;
        return imageBaseUrl + size + path;
    }

    /**
     * Resets the singleton (for testing only — not for production use).
     */
    static void resetForTesting() {
        instance = null;
    }
}
