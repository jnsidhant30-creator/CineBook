package com.movieticket.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * AnimationUtils.java — Centralized Lightweight Animation Engine for CineBook.
 * Encapsulates timing constants, mathematical interpolators, color blenders,
 * component alpha fade utilities, and EDT-safe Timer helpers.
 */
public class AnimationUtils {

    // Centralized Timing Constants (in milliseconds)
    public static final int FAST   = 150;  // Hover states, seat state toggles
    public static final int NORMAL = 250;  // Page slide/fade transitions
    public static final int MEDIUM = 400;  // Card entrance transitions
    public static final int SLOW   = 800;  // Count-up counters, chart reveals, ambient sweeps

    // Frame rate for smooth Swing animation timers (approx 30 fps = 33ms interval)
    public static final int TIMER_INTERVAL_MS = 33;

    /**
     * Interpolates smoothly between 0.0 and 1.0 using standard Ease-InOut curve.
     */
    public static float easeInOut(float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        return (float) (t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2);
    }

    /**
     * Interpolates smoothly between 0.0 and 1.0 using Ease-Out curve (fast start, soft finish).
     */
    public static float easeOut(float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        return (float) (1 - Math.pow(1 - t, 3));
    }

    /**
     * Blends two colors according to ratio (0.0 = c1, 1.0 = c2).
     */
    public static Color blendColors(Color c1, Color c2, float ratio) {
        if (c1 == null) return c2;
        if (c2 == null) return c1;
        float r = Math.max(0.0f, Math.min(1.0f, ratio));
        float ir = 1.0f - r;

        int red   = Math.round(c1.getRed() * ir + c2.getRed() * r);
        int green = Math.round(c1.getGreen() * ir + c2.getGreen() * r);
        int blue  = Math.round(c1.getBlue() * ir + c2.getBlue() * r);
        int alpha = Math.round(c1.getAlpha() * ir + c2.getAlpha() * r);

        return new Color(red, green, blue, alpha);
    }

    /**
     * Linear interpolation between start and end values.
     */
    public static float lerp(float start, float end, float t) {
        return start + (end - start) * Math.max(0.0f, Math.min(1.0f, t));
    }

    /**
     * Integer linear interpolation.
     */
    public static int lerpInt(int start, int end, float t) {
        return Math.round(start + (end - start) * Math.max(0.0f, Math.min(1.0f, t)));
    }

    /**
     * Creates a one-shot EDT animation timer.
     */
    public static Timer animate(int durationMs, AnimationCallback callback, Runnable onComplete) {
        long startTime = System.currentTimeMillis();

        Timer timer = new Timer(TIMER_INTERVAL_MS, null);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1.0f, (float) elapsed / durationMs);
                float eased = easeInOut(progress);

                if (callback != null) {
                    callback.onFrame(progress, eased);
                }

                if (progress >= 1.0f) {
                    timer.stop();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        });
        timer.start();
        return timer;
    }

    @FunctionalInterface
    public interface AnimationCallback {
        void onFrame(float rawProgress, float easedProgress);
    }
}
