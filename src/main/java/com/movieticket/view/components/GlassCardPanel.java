package com.movieticket.view.components;

import com.movieticket.util.CineBookTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * GlassCardPanel.java — Semi-transparent modern card container for CineBook.
 * Features rounded corners, subtle translucent border rgba(148,163,184,0.15),
 * dark semi-transparent fill rgba(17,31,51,0.90), and optional top accent line.
 */
public class GlassCardPanel extends JPanel {

    private int cornerRadius;
    private Color cardBackground;
    private Color borderColor;
    private Color topAccentColor;
    private int topAccentHeight;
    private boolean useGradientBorder = false;

    public GlassCardPanel() {
        this(new BorderLayout(), 14, new Color(17, 31, 51, 230), new Color(148, 163, 184, 38));
    }

    public GlassCardPanel(LayoutManager layout) {
        this(layout, 14, new Color(17, 31, 51, 230), new Color(148, 163, 184, 38));
    }

    public GlassCardPanel(LayoutManager layout, int cornerRadius, Color cardBackground, Color borderColor) {
        super(layout);
        this.cornerRadius = cornerRadius;
        this.cardBackground = cardBackground;
        this.borderColor = borderColor;
        this.topAccentColor = null;
        this.topAccentHeight = 0;
        setOpaque(false);
        setBorder(new EmptyBorder(16, 18, 16, 18));
    }

    public void setTopAccent(Color accentColor, int height) {
        this.topAccentColor = accentColor;
        this.topAccentHeight = height;
        repaint();
    }

    public void setCardBackground(Color bg) {
        this.cardBackground = bg;
        repaint();
    }

    public void setBorderColor(Color border) {
        this.borderColor = border;
        repaint();
    }

    public void setUseGradientBorder(boolean useGradientBorder) {
        this.useGradientBorder = useGradientBorder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Semi-transparent Dark Card Surface Fill
        g2.setColor(cardBackground != null ? cardBackground : new Color(17, 31, 51, 230));
        g2.fillRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

        // 2. Top Accent Line (if specified)
        if (topAccentColor != null && topAccentHeight > 0) {
            g2.setColor(topAccentColor);
            g2.fillRoundRect(0, 0, w - 1, topAccentHeight * 2, cornerRadius, cornerRadius);
            g2.setColor(cardBackground);
            g2.fillRect(0, topAccentHeight, w - 1, h - topAccentHeight);
        }

        // 3. Border Drawing
        if (useGradientBorder) {
            // Animate angle over time (one full rotation every 8 seconds)
            double angle = (System.currentTimeMillis() / 8000.0) * 2.0 * Math.PI;
            float cx = w / 2.0f;
            float cy = h / 2.0f;
            float r = (float) Math.sqrt(cx * cx + cy * cy);
            float x1 = cx + (float) Math.cos(angle) * r;
            float y1 = cy + (float) Math.sin(angle) * r;
            float x2 = cx - (float) Math.cos(angle) * r;
            float y2 = cy - (float) Math.sin(angle) * r;

            // Gradient: Cyan (#00f0ff) to Magenta/Purple (#a855f7)
            Color cyanColor = new Color(0, 240, 255, 220);
            Color magentaColor = new Color(168, 85, 247, 220);

            LinearGradientPaint gradient = new LinearGradientPaint(
                x1, y1, x2, y2,
                new float[]{0.0f, 1.0f},
                new Color[]{cyanColor, magentaColor}
            );
            g2.setPaint(gradient);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
        } else {
            g2.setColor(borderColor != null ? borderColor : new Color(148, 163, 184, 38));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
