package com.movieticket.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BackgroundImageManager.java — High-performance background image caching and scaling manager.
 * Loads background assets once from classpath, caches scaled instances, and manages procedural
 * background buffers to prevent frame lag during Swing repaints.
 */
public class BackgroundImageManager {

    private static final Map<String, BufferedImage> rawImageCache = new ConcurrentHashMap<>();
    private static final Map<String, BufferedImage> scaledCache = new ConcurrentHashMap<>();

    private static final String DEFAULT_BG_PATH = "/images/cinema_background.jpg";

    /**
     * Loads a background image from local classpath safely.
     * Returns null if missing or error occurs without throwing.
     */
    public static BufferedImage getRawImage(String path) {
        if (path == null) path = DEFAULT_BG_PATH;
        if (rawImageCache.containsKey(path)) {
            return rawImageCache.get(path);
        }

        try (InputStream is = BackgroundImageManager.class.getResourceAsStream(path)) {
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                if (img != null) {
                    rawImageCache.put(path, img);
                    return img;
                }
            }
        } catch (Exception e) {
            System.err.println("[BackgroundImageManager] Info: Local background image not found at " + path + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves or creates a high-quality scaled image cached by resolution key.
     */
    public static BufferedImage getScaledImage(String path, int targetWidth, int targetHeight) {
        if (targetWidth <= 0 || targetHeight <= 0) return null;

        BufferedImage raw = getRawImage(path);
        if (raw == null) return null;

        String cacheKey = (path != null ? path : DEFAULT_BG_PATH) + "_" + targetWidth + "x" + targetHeight;
        if (scaledCache.containsKey(cacheKey)) {
            return scaledCache.get(cacheKey);
        }

        // Create high-quality scaled buffer
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(raw, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        scaledCache.put(cacheKey, scaled);
        return scaled;
    }

    /**
     * Clear caches when memory needs to be reclaimed.
     */
    public static void clearCache() {
        scaledCache.clear();
        rawImageCache.clear();
    }
}
