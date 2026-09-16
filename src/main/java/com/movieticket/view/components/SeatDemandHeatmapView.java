package com.movieticket.view.components;

import com.movieticket.dao.ReportDAO;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * SeatDemandHeatmapView.java — SIH Admin Seat Demand Heatmap Component.
 * Calculates historical booking frequency ratios for seats across shows:
 * - LOW DEMAND (< 30%): Cyan (#06B6D4)
 * - MEDIUM DEMAND (30% - 70%): Amber (#F59E0B)
 * - HIGH DEMAND (>= 70%): Glowing Red (#EF4444)
 */
public class SeatDemandHeatmapView extends JPanel {

    private final ReportDAO reportDAO;
    private JPanel gridPanel;
    private JLabel summaryLabel;

    public SeatDemandHeatmapView() {
        this.reportDAO = new ReportDAO();
        setLayout(new BorderLayout(12, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(14, 16, 14, 16));

        initComponents();
        loadHeatmapData();
    }

    private void initComponents() {
        // 1. Header Bar
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLbl = new JLabel("HISTORICAL SEAT DEMAND HEATMAP", FontIcon.of(FontAwesomeSolid.FIRE, 18, CineBookTheme.ACCENT_GOLD), SwingConstants.LEFT);
        titleLbl.setFont(ThemeManager.getHeadingFont(Font.BOLD, 16));
        titleLbl.setForeground(CineBookTheme.TEXT_PRIMARY);
        titleLbl.setIconTextGap(8);

        summaryLabel = new JLabel("Based on actual database historical booking volume");
        summaryLabel.setFont(ThemeManager.getSmallFont());
        summaryLabel.setForeground(CineBookTheme.TEXT_MUTED);

        header.add(titleLbl, BorderLayout.WEST);
        header.add(summaryLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 2. Center Content: Screen Arc + Heatmap Grid
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);

        // Screen Arc Banner
        JPanel screenArc = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                GradientPaint grad = new GradientPaint(0, 0, CineBookTheme.ACCENT_PURPLE, w, 0, CineBookTheme.ACCENT_BLUE);
                g2.setPaint(grad);
                g2.fillArc(20, -h + 12, w - 40, h * 2, 210, 120);

                g2.setFont(ThemeManager.getSmallFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String text = "SCREEN THIS WAY";
                g2.drawString(text, (w - fm.stringWidth(text)) / 2, 16);
                g2.dispose();
            }
        };
        screenArc.setPreferredSize(new Dimension(600, 26));
        screenArc.setMaximumSize(new Dimension(2000, 26));
        screenArc.setOpaque(false);

        centerContainer.add(screenArc);
        centerContainer.add(Box.createVerticalStrut(16));

        // Seat Grid Map (Rows A-D, Cols 1-8)
        gridPanel = new JPanel(new GridLayout(4, 8, 8, 8));
        gridPanel.setOpaque(false);
        gridPanel.setMaximumSize(new Dimension(680, 220));

        centerContainer.add(gridPanel);
        centerContainer.add(Box.createVerticalStrut(14));

        add(centerContainer, BorderLayout.CENTER);

        // 3. Legend Box
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        legendPanel.setOpaque(false);

        legendPanel.add(createLegendTag("LOW DEMAND (<30%)", CineBookTheme.ACCENT_CYAN));
        legendPanel.add(createLegendTag("MEDIUM DEMAND (30-70%)", CineBookTheme.WARNING_COLOR));
        legendPanel.add(createLegendTag("HIGH DEMAND (≥70%)", CineBookTheme.DANGER_COLOR));

        add(legendPanel, BorderLayout.SOUTH);
    }

    public void loadHeatmapData() {
        SwingWorker<Map<String, Integer>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Integer> doInBackground() {
                return reportDAO.getSeatBookingFrequencies();
            }

            @Override
            protected void done() {
                try {
                    Map<String, Integer> freqMap = get();
                    buildHeatmapGrid(freqMap);
                } catch (Exception e) {
                    System.err.println("[SeatDemandHeatmapView] Error building heatmap: " + e.getMessage());
                    buildHeatmapGrid(new HashMap<>());
                }
            }
        };
        worker.execute();
    }

    private void buildHeatmapGrid(Map<String, Integer> freqMap) {
        gridPanel.removeAll();
        String[] rows = {"A", "B", "C", "D"};

        // Find max frequency for percentage calculation
        int maxFreq = freqMap.values().stream().mapToInt(v -> v).max().orElse(1);
        if (maxFreq <= 0) maxFreq = 1;

        for (String r : rows) {
            for (int c = 1; c <= 8; c++) {
                String seatNum = r + c;
                int count = freqMap.getOrDefault(seatNum, 0);
                double pct = (double) count / maxFreq * 100.0;

                gridPanel.add(createHeatmapSeatButton(seatNum, count, pct));
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createHeatmapSeatButton(String seatNum, int count, double pct) {
        Color borderClr;
        Color bgClr;
        String demandText;

        if (pct >= 70.0) {
            borderClr = CineBookTheme.DANGER_COLOR;
            bgClr = new Color(239, 68, 68, 40);
            demandText = "HIGH DEMAND";
        } else if (pct >= 30.0) {
            borderClr = CineBookTheme.WARNING_COLOR;
            bgClr = new Color(245, 158, 11, 35);
            demandText = "MEDIUM DEMAND";
        } else {
            borderClr = CineBookTheme.ACCENT_CYAN;
            bgClr = new Color(6, 182, 212, 25);
            demandText = "LOW DEMAND";
        }

        JPanel btn = new JPanel(new BorderLayout());
        btn.setOpaque(true);
        btn.setBackground(bgClr);
        btn.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(borderClr, 2, true),
                new EmptyBorder(4, 4, 4, 4)
        ));
        btn.setToolTipText("Seat " + seatNum + " | Bookings: " + count + " (" + (int) pct + "%) | " + demandText);

        JLabel lbl = new JLabel(seatNum, SwingConstants.CENTER);
        lbl.setFont(ThemeManager.getFont(Font.BOLD, 12));
        lbl.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel pctLbl = new JLabel((int) pct + "%", SwingConstants.CENTER);
        pctLbl.setFont(ThemeManager.getSmallFont());
        pctLbl.setForeground(borderClr);

        btn.add(lbl, BorderLayout.CENTER);
        btn.add(pctLbl, BorderLayout.SOUTH);

        return btn;
    }

    private JPanel createLegendTag(String text, Color color) {
        JPanel tag = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tag.setOpaque(false);

        JLabel dot = new JLabel("●");
        dot.setFont(ThemeManager.getFont(Font.BOLD, 14));
        dot.setForeground(color);

        JLabel label = new JLabel(text);
        label.setFont(ThemeManager.getSmallFont());
        label.setForeground(CineBookTheme.TEXT_MUTED);

        tag.add(dot);
        tag.add(label);
        return tag;
    }
}
