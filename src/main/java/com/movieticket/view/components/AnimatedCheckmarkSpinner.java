package com.movieticket.view.components;

import com.movieticket.util.AnimationUtils;
import com.movieticket.util.CineBookTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * AnimatedCheckmarkSpinner.java — Canvas Component for Booking Confirmation Sequence.
 * Morphing sequence:
 * Mode 1: SPINNING -> Rotating arc spinner ("Processing Booking...")
 * Mode 2: CHECKMARK -> Morphing animated checkmark (✓) with scale pulse ("BOOKING CONFIRMED")
 */
public class AnimatedCheckmarkSpinner extends JPanel {

    public enum Mode {
        SPINNER,
        CHECKMARK
    }

    private Mode mode = Mode.SPINNER;

    // Spinner State
    private float spinnerAngle = 0.0f;
    private Timer spinnerTimer;

    // Checkmark State
    private float checkmarkProgress = 0.0f; // 0.0 to 1.0
    private Timer checkmarkTimer;

    public AnimatedCheckmarkSpinner() {
        setPreferredSize(new Dimension(100, 100));
        setOpaque(false);
        setupTimers();
    }

    private void setupTimers() {
        // Spinner Timer (30 fps rotation)
        spinnerTimer = new Timer(30, e -> {
            if (mode == Mode.SPINNER && isShowing()) {
                spinnerAngle = (spinnerAngle + 12.0f) % 360.0f;
                repaint();
            }
        });

        // Checkmark Animation Timer
        checkmarkTimer = AnimationUtils.animate(AnimationUtils.SLOW, (rawProgress, easedProgress) -> {
            checkmarkProgress = easedProgress;
            repaint();
        }, null);
        checkmarkTimer.stop(); // Started when mode changes to CHECKMARK
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.SPINNER) {
            checkmarkProgress = 0.0f;
            if (!spinnerTimer.isRunning()) spinnerTimer.start();
            if (checkmarkTimer.isRunning()) checkmarkTimer.stop();
        } else if (mode == Mode.CHECKMARK) {
            if (spinnerTimer.isRunning()) spinnerTimer.stop();
            checkmarkProgress = 0.0f;
            checkmarkTimer.restart();
        }
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (mode == Mode.SPINNER && !spinnerTimer.isRunning()) {
            spinnerTimer.start();
        }
    }

    @Override
    public void removeNotify() {
        if (spinnerTimer.isRunning()) spinnerTimer.stop();
        if (checkmarkTimer.isRunning()) checkmarkTimer.stop();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = h / 2;
        int radius = Math.min(w, h) / 2 - 8;

        if (mode == Mode.SPINNER) {
            // Draw Rotating Arc Spinner
            g2.setColor(new Color(38, 54, 77));
            g2.setStroke(new BasicStroke(4.0f));
            g2.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);

            g2.setColor(CineBookTheme.ACCENT_PURPLE);
            g2.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(cx - radius, cy - radius, radius * 2, radius * 2, (int) spinnerAngle, 100);

        } else if (mode == Mode.CHECKMARK) {
            // Draw Success Circle Base with Scale Pulse
            float scale = 0.8f + 0.2f * AnimationUtils.easeInOut(Math.min(1.0f, checkmarkProgress * 1.5f));
            int r = (int) (radius * scale);

            g2.setColor(CineBookTheme.SUCCESS_COLOR);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);

            // Draw Animated Checkmark (✓) Path
            if (checkmarkProgress > 0.2f) {
                float checkProgress = Math.min(1.0f, (checkmarkProgress - 0.2f) / 0.8f);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(5.0f * scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                Path2D checkPath = new Path2D.Float();
                float x1 = cx - r * 0.40f;
                float y1 = cy + r * 0.05f;

                float x2 = cx - r * 0.10f;
                float y2 = cy + r * 0.35f;

                float x3 = cx + r * 0.45f;
                float y3 = cy - r * 0.30f;

                if (checkProgress < 0.5f) {
                    float p = checkProgress / 0.5f;
                    checkPath.moveTo(x1, y1);
                    checkPath.lineTo(AnimationUtils.lerp(x1, x2, p), AnimationUtils.lerp(y1, y2, p));
                } else {
                    float p = (checkProgress - 0.5f) / 0.5f;
                    checkPath.moveTo(x1, y1);
                    checkPath.lineTo(x2, y2);
                    checkPath.lineTo(AnimationUtils.lerp(x2, x3, p), AnimationUtils.lerp(y2, y3, p));
                }
                g2.draw(checkPath);
            }
        }

        g2.dispose();
    }
}
