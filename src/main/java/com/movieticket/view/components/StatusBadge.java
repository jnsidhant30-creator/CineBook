package com.movieticket.view.components;

import com.movieticket.util.CineBookTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * StatusBadge.java — Rounded pill-style status indicator badge component.
 * Supports AVAILABLE, ACTIVE, BOOKED, CANCELLED, PENDING, BLOCKED statuses.
 */
public class StatusBadge extends JLabel {

    private Color badgeColor = CineBookTheme.SUCCESS_COLOR;
    private int cornerRadius = 14;

    public StatusBadge() {
        this("ACTIVE");
    }

    public StatusBadge(String statusText) {
        super(statusText != null ? statusText.toUpperCase() : "UNKNOWN");
        setFont(new Font("Segoe UI", Font.BOLD, 11));
        setForeground(Color.WHITE);
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(new EmptyBorder(4, 10, 4, 10));
        setOpaque(false);
        updateStatusColor(getText());
    }

    public void setStatus(String statusText) {
        String upper = statusText != null ? statusText.toUpperCase() : "UNKNOWN";
        setText(upper);
        updateStatusColor(upper);
        repaint();
    }

    private void updateStatusColor(String status) {
        if (status == null) status = "";
        switch (status) {
            case "AVAILABLE":
            case "ACTIVE":
            case "CONFIRMED":
            case "SUCCESS":
                badgeColor = CineBookTheme.SUCCESS_COLOR;
                break;
            case "BOOKED":
            case "CANCELLED":
            case "DANGER":
            case "DELETED":
                badgeColor = CineBookTheme.DANGER_COLOR;
                break;
            case "PENDING":
            case "WARNING":
                badgeColor = CineBookTheme.WARNING_COLOR;
                break;
            case "BLOCKED":
            case "INACTIVE":
            default:
                badgeColor = new Color(100, 116, 139); // Slate Gray
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Draw pill-shaped background
        g2.setColor(badgeColor);
        g2.fillRoundRect(0, 0, width, height, height, height);

        g2.dispose();
        super.paintComponent(g);
    }
}
