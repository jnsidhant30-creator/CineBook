package com.movieticket.view.components;

import com.movieticket.util.CineBookTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * RoundedPanel.java — Container card with rounded corners, subtle top accent bar,
 * customizable border, and optional hover elevation effect.
 */
public class RoundedPanel extends JPanel {

    private int cornerRadius = 12;
    private Color backgroundColor = CineBookTheme.BG_CARD;
    private Color hoverBackgroundColor = CineBookTheme.BG_CARD_HOVER;
    private Color borderColor = CineBookTheme.BORDER_COLOR;
    private Color topAccentColor = null;
    private int topAccentHeight = 4;
    private boolean isHovered = false;
    private boolean hoverElevationEnabled = false;

    public RoundedPanel() {
        this(new BorderLayout(), 12, CineBookTheme.BG_CARD);
    }

    public RoundedPanel(LayoutManager layout) {
        this(layout, 12, CineBookTheme.BG_CARD);
    }

    public RoundedPanel(LayoutManager layout, int cornerRadius, Color bgColor) {
        super(layout);
        this.cornerRadius = cornerRadius;
        this.backgroundColor = bgColor;
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (hoverElevationEnabled) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hoverElevationEnabled) {
                    isHovered = false;
                    repaint();
                }
            }
        });
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public void setCardBackground(Color color) {
        this.backgroundColor = color;
        repaint();
    }

    public void setTopAccent(Color accentColor) {
        this.topAccentColor = accentColor;
        repaint();
    }

    public void setTopAccent(Color accentColor, int height) {
        this.topAccentColor = accentColor;
        this.topAccentHeight = height;
        repaint();
    }

    public void setHoverElevationEnabled(boolean enabled) {
        this.hoverElevationEnabled = enabled;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 1. Fill Rounded Panel Background
        Color bg = isHovered ? hoverBackgroundColor : backgroundColor;
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

        // 2. Draw Top Accent Bar if configured
        if (topAccentColor != null && topAccentHeight > 0) {
            g2.setColor(topAccentColor);
            g2.fillRoundRect(0, 0, width, topAccentHeight * 2, cornerRadius, cornerRadius);
            // Clip bottom part of accent bar rectangle
            g2.setColor(bg);
            g2.fillRect(0, topAccentHeight, width, height - topAccentHeight);
        }

        // 3. Draw Outer Border
        g2.setColor(isHovered ? CineBookTheme.BORDER_LIGHT : borderColor);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
