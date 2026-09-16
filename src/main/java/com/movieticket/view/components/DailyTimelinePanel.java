package com.movieticket.view.components;

import com.movieticket.util.CineBookTheme;
import com.movieticket.model.Show;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DailyTimelinePanel extends JPanel {

    private List<Show> shows;
    private final LocalTime startOfDay = LocalTime.of(8, 0); // 8 AM
    private final LocalTime endOfDay = LocalTime.of(23, 59); // 11:59 PM
    private final int totalMinutes = 16 * 60; // 16 hours from 8 AM to 12 AM

    public DailyTimelinePanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(800, 100)); // Fixed height for timeline
        setMinimumSize(new Dimension(400, 100));
    }

    public void setShows(List<Show> shows) {
        this.shows = shows;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Background Bar
        int barY = height / 2 - 10;
        int barHeight = 20;
        g2d.setColor(CineBookTheme.BG_HEADER);
        g2d.fillRoundRect(10, barY, width - 20, barHeight, 10, 10);

        // Draw Time Markers (Every 2 hours starting from 8 AM)
        g2d.setColor(CineBookTheme.TEXT_MUTED);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int i = 0; i <= 16; i += 2) {
            int markerX = 10 + (int) ((i * 60.0 / totalMinutes) * (width - 20));
            LocalTime time = startOfDay.plusHours(i);
            String timeStr = time.format(DateTimeFormatter.ofPattern("ha"));
            int strWidth = g2d.getFontMetrics().stringWidth(timeStr);
            g2d.drawString(timeStr, markerX - strWidth / 2, barY + barHeight + 15);
            g2d.drawLine(markerX, barY, markerX, barY + barHeight);
        }

        if (shows != null && !shows.isEmpty()) {
            for (Show show : shows) {
                LocalTime sTime = show.getShowTime();
                LocalTime eTime = show.getEndTime();
                if (eTime == null) {
                    eTime = sTime.plusMinutes(120); // fallback
                }

                if (sTime.isBefore(startOfDay)) sTime = startOfDay;
                if (eTime.isAfter(endOfDay) || eTime.isBefore(startOfDay)) eTime = endOfDay;

                int startMins = (sTime.getHour() - 8) * 60 + sTime.getMinute();
                int endMins = (eTime.getHour() - 8) * 60 + eTime.getMinute();

                int x = 10 + (int) ((startMins / (double) totalMinutes) * (width - 20));
                int showWidth = (int) (((endMins - startMins) / (double) totalMinutes) * (width - 20));

                // Draw 15 min buffer (yellow)
                int bufferWidth = (int) ((15.0 / totalMinutes) * (width - 20));
                if (x + showWidth + bufferWidth <= width - 10) {
                    g2d.setColor(new Color(255, 193, 7, 100)); // Semi-transparent yellow for buffer
                    g2d.fill(new RoundRectangle2D.Double(x + showWidth, barY, bufferWidth, barHeight, 5, 5));
                }

                // Draw Show Block (accent color)
                g2d.setColor(CineBookTheme.ACCENT_PURPLE);
                g2d.fill(new RoundRectangle2D.Double(x, barY, showWidth, barHeight, 5, 5));
                
                // Show border
                g2d.setColor(CineBookTheme.ACCENT_PURPLE.darker());
                g2d.draw(new RoundRectangle2D.Double(x, barY, showWidth, barHeight, 5, 5));

                // Tooltip text (drawn if width permits)
                if (showWidth > 30) {
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    String lbl = show.getShowId() + "";
                    int lW = g2d.getFontMetrics().stringWidth(lbl);
                    g2d.drawString(lbl, x + (showWidth - lW) / 2, barY + 14);
                }
            }
        }

        g2d.dispose();
    }
}
