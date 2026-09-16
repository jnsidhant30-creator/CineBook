package com.movieticket.util;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

/**
 * Utility for loading and caching images from URLs with local disk cache support.
 *
 * Threading: loadImage is blocking — always call from a background thread
 * (e.g. inside a SwingWorker). It MUST NOT be called on the Swing EDT.
 */
public class ImageLoader {

    private static final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<Image>> inFlightRequests = new ConcurrentHashMap<>();
    private static final String CACHE_DIR = "cache/posters/";

    private static final int CONNECT_TIMEOUT_MS = 8_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    static {
        try {
            Files.createDirectories(Paths.get(CACHE_DIR));
        } catch (Exception e) {
            System.err.println("[ImageLoader] Failed to create cache directory: " + e.getMessage());
        }
    }

    public static Image loadImage(String urlStr, int width, int height) {
        if (urlStr == null || urlStr.trim().isEmpty() || "N/A".equalsIgnoreCase(urlStr.trim())) {
            return null;
        }

        String key = urlStr.trim();
        String cacheKey = generateCacheKey(key, width, height);

        // 1. Check in-memory cache
        Image cached = imageCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 1.5 Check in-flight requests
        CompletableFuture<Image> future = new CompletableFuture<>();
        CompletableFuture<Image> existing = inFlightRequests.putIfAbsent(cacheKey, future);
        if (existing != null) {
            try {
                return existing.get();
            } catch (Exception e) {
                return null;
            }
        }

        try {
            // 2. Check local disk cache
            File cachedFile = new File(CACHE_DIR, cacheKey + ".jpg");
            if (cachedFile.exists()) {
                try {
                    BufferedImage img = ImageIO.read(cachedFile);
                    if (img != null) {
                        imageCache.put(cacheKey, img);
                        future.complete(img);
                        return img;
                    }
                } catch (Exception e) {
                    System.err.println("[ImageLoader] Failed to read cached file: " + e.getMessage());
                }
            }

            // 3. Download, scale, and cache
            try {
                URL url = java.net.URI.create(key).toURL();
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
                conn.setReadTimeout(READ_TIMEOUT_MS);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) CineBook/1.0");
                conn.connect();

                BufferedImage originalImg = ImageIO.read(conn.getInputStream());
                if (originalImg != null) {
                    BufferedImage finalImg = originalImg;
                    
                    // Scale if dimensions provided
                    if (width > 0 && height > 0) {
                        Image scaled = originalImg.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        finalImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                        java.awt.Graphics2D g2d = finalImg.createGraphics();
                        g2d.drawImage(scaled, 0, 0, null);
                        g2d.dispose();
                    }

                    // Save to disk
                    try {
                        ImageIO.write(finalImg, "jpg", cachedFile);
                    } catch (Exception e) {
                        System.err.println("[ImageLoader] Failed to write cache file: " + e.getMessage());
                    }

                    // Save to memory
                    imageCache.put(cacheKey, finalImg);
                    future.complete(finalImg);
                    return finalImg;
                }
            } catch (Exception e) {
                System.err.println("[ImageLoader] Failed to load poster from URL: " + e.getMessage());
            }
            future.complete(null);
            return null;
        } finally {
            inFlightRequests.remove(cacheKey);
        }
    }

    public static Image loadImage(String urlStr) {
        return loadImage(urlStr, -1, -1);
    }

    public static Image loadTmdbImage(String posterPath, String size, int targetWidth, int targetHeight) {
        if (posterPath == null || posterPath.isBlank()) return null;
        String fullUrl = TmdbConfig.getInstance().buildImageUrl(posterPath, size);
        return loadImage(fullUrl, targetWidth, targetHeight);
    }

    public static Image loadTmdbImage(String posterPath, String size) {
        return loadTmdbImage(posterPath, size, -1, -1);
    }

    public static Image loadMoviePoster(com.movieticket.model.Movie movie, String tmdbSize, int width, int height) {
        if (movie == null) return null;
        
        if (movie.getTmdbPosterPath() != null && !movie.getTmdbPosterPath().isBlank()) {
            Image tmdbImg = loadTmdbImage(movie.getTmdbPosterPath(), tmdbSize, width, height);
            if (tmdbImg != null) return tmdbImg;
        }
        
        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isBlank() && !"N/A".equalsIgnoreCase(movie.getPosterUrl())) {
            return loadImage(movie.getPosterUrl(), width, height);
        }
        return null;
    }

    public static Image loadMoviePoster(com.movieticket.model.Movie movie, String tmdbSize) {
        return loadMoviePoster(movie, tmdbSize, -1, -1);
    }

    private static String generateCacheKey(String url, int width, int height) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(url.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            if (width > 0 && height > 0) {
                sb.append("_").append(width).append("x").append(height);
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(url.hashCode()) + (width > 0 ? "_" + width + "x" + height : "");
        }
    }

    public static void evict(String urlStr) {
        if (urlStr != null) {
            String keyRaw = generateCacheKey(urlStr.trim(), -1, -1);
            imageCache.remove(keyRaw);
        }
    }

    public static void clearCache() {
        imageCache.clear();
    }
}
