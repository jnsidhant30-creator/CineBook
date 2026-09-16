package com.movieticket.view.components;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * BookingsOverviewChart.java — Custom Swing Component for rendering modern area/line chart
 * matching the Bookings Overview (This Week) chart from the reference image.
 */
public class BookingsOverviewChart extends JPanel {

    private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private final int[] values = {25, 45, 60, 52, 78, 50, 75};
    private float animationProgress = 1.0f;

    public BookingsOverviewChart() {
        setPreferredSize(new Dimension(500, 200));
        setOpaque(false);
    }

    public void animateReveal() {
        com.movieticket.util.AnimationUtils.animate(com.movieticket.util.AnimationUtils.SLOW, (rawProgress, easedProgress) -> {
            animationProgress = easedProgress;
            repaint();
        }, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        int paddingLeft = 40;
        int paddingRight = 20;
        int paddingTop = 20;
        int paddingBottom = 35;

        int chartW = w - paddingLeft - paddingRight;
        int chartH = h - paddingTop - paddingBottom;

        // 1. Draw Y-Axis Grid Lines & Labels (0, 20, 40, 60, 80, 100)
        g2.setFont(ThemeManager.getSmallFont());
        g2.setColor(CineBookTheme.TEXT_MUTED);

        int ySteps = 5;
        for (int i = 0; i <= ySteps; i++) {
            int yVal = 100 - (i * 20);
            int yPos = paddingTop + (int) ((float) i / ySteps * chartH);

            // Y-Label
            String label = String.valueOf(yVal);
            g2.drawString(label, 8, yPos + 4);

            // Grid Line
            g2.setColor(new Color(38, 54, 77, 80));
            g2.drawLine(paddingLeft, yPos, w - paddingRight, yPos);
            g2.setColor(CineBookTheme.TEXT_MUTED);
        }

        // 2. Compute Points
        int numPoints = values.length;
        Point[] points = new Point[numPoints];
        for (int i = 0; i < numPoints; i++) {
            int x = paddingLeft + (int) ((float) i / (numPoints - 1) * chartW);
            float normVal = (float) values[i] / 100.0f * animationProgress;
            int y = paddingTop + chartH - (int) (normVal * chartH);
            points[i] = new Point(x, y);
        }

        // 3. Draw Gradient Filled Area under line
        Path2D areaPath = new Path2D.Float();
        areaPath.moveTo(points[0].x, paddingTop + chartH);
        areaPath.lineTo(points[0].x, points[0].y);

        for (int i = 1; i < numPoints; i++) {
            int cx = (points[i - 1].x + points[i].x) / 2;
            areaPath.curveTo(cx, points[i - 1].y, cx, points[i].y, points[i].x, points[i].y);
        }
        areaPath.lineTo(points[numPoints - 1].x, paddingTop + chartH);
        areaPath.closePath();

        Paint fillGradient = new GradientPaint(
                0, paddingTop, new Color(124, 58, 237, 100),
                0, paddingTop + chartH, new Color(124, 58, 237, 5)
        );
        g2.setPaint(fillGradient);
        g2.fill(areaPath);

        // 4. Draw Purple Smooth Line Curve
        Path2D linePath = new Path2D.Float();
        linePath.moveTo(points[0].x, points[0].y);
        for (int i = 1; i < numPoints; i++) {
            int cx = (points[i - 1].x + points[i].x) / 2;
            linePath.curveTo(cx, points[i - 1].y, cx, points[i].y, points[i].x, points[i].y);
        }

        g2.setColor(CineBookTheme.ACCENT_PURPLE);
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(linePath);

        // 5. Draw Points & X-Axis Day Labels
        for (int i = 0; i < numPoints; i++) {
            Point p = points[i];

            // Outer Glow Dot
            g2.setColor(new Color(124, 58, 237, 140));
            g2.fillOval(p.x - 6, p.y - 6, 12, 12);

            // Inner Dot
            g2.setColor(Color.WHITE);
            g2.fillOval(p.x - 3, p.y - 3, 6, 6);

            // X-Day Label
            g2.setFont(ThemeManager.getSmallFont());
            g2.setColor(CineBookTheme.TEXT_MUTED);
            String day = days[i];
            int dayW = g2.getFontMetrics().stringWidth(day);
            g2.drawString(day, p.x - dayW / 2, paddingTop + chartH + 20);
        }

        g2.dispose();
    }
}
