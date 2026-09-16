package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;
import com.movieticket.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;

/**
 * AdminAnalyticsView — Professional Admin Analytics Dashboard.
 * Displays KPI stat cards, top-movies table, theatre revenue table,
 * and a daily booking bar chart — all loaded asynchronously via SwingWorker.
 * Part of Phase 6 (Admin Analytics Dashboard).
 */
public class AdminAnalyticsView extends JPanel {

    // ─── KPI Labels ──────────────────────────────────────────────────────────
    private JLabel lblTotalRevenue;
    private JLabel lblTodayRevenue;
    private JLabel lblMonthRevenue;
    private JLabel lblTotalBookings;
    private JLabel lblTodayBookings;
    private JLabel lblTicketsSold;

    // ─── Controls ────────────────────────────────────────────────────────────
    private JButton btnRefreshAnalytics;
    private JComboBox<String> dateRangeCombo;
    private JLabel lblLastUpdated;
    private JLabel lblStatus;

    // ─── Tables ────────────────────────────────────────────────────────────
    private JTable topMoviesTable;
    private DefaultTableModel topMoviesModel;
    private JTable theatreRevenueTable;
    private DefaultTableModel theatreRevenueModel;
    private JTable seatTypeTable;
    private DefaultTableModel seatTypeModel;
    private JTable topRatedTable;
    private DefaultTableModel topRatedModel;
    private JTable topGenresTable;
    private DefaultTableModel topGenresModel;

    // ─── Bar Chart Panel (custom-painted) ────────────────────────────────────
    private BarChartPanel barChartPanel;

    public AdminAnalyticsView() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
    }

    private void initComponents() {
        // ── Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Analytics Dashboard");
        title.setFont(ThemeManager.getPageTitleFont());
        title.setForeground(CineBookTheme.TEXT_PRIMARY);
        lblLastUpdated = new JLabel("Loading…");
        lblLastUpdated.setFont(ThemeManager.getSmallFont());
        lblLastUpdated.setForeground(CineBookTheme.TEXT_MUTED);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblLastUpdated);
        header.add(titleBox, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JLabel lblRange = new JLabel("Date Range:");
        lblRange.setFont(ThemeManager.getLabelFont());
        lblRange.setForeground(CineBookTheme.TEXT_MUTED);
        controls.add(lblRange);

        dateRangeCombo = new JComboBox<>(new String[]{"Last 7 Days", "Last 14 Days", "Last 30 Days", "Last 90 Days"});
        ThemeManager.styleComboBox(dateRangeCombo);
        dateRangeCombo.setPreferredSize(new Dimension(150, 34));
        controls.add(dateRangeCombo);

        btnRefreshAnalytics = new JButton("↻  Refresh");
        ThemeManager.stylePrimaryButton(btnRefreshAnalytics);
        btnRefreshAnalytics.setPreferredSize(new Dimension(120, 34));
        controls.add(btnRefreshAnalytics);

        header.add(controls, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Status bar ──
        lblStatus = new JLabel(" ");
        lblStatus.setFont(ThemeManager.getSmallFont());
        lblStatus.setForeground(CineBookTheme.ACCENT_CYAN);
        add(lblStatus, BorderLayout.SOUTH);

        // ── Main scroll area ──
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setOpaque(false);

        // Row 1: KPI stat cards
        main.add(buildKpiRow());
        main.add(Box.createVerticalStrut(14));

        // Row 2: Bar chart + Seat type breakdown
        main.add(buildChartRow());
        main.add(Box.createVerticalStrut(14));

        // Row 3: Top Movies + Theatre Revenue
        main.add(buildTableRow());
        main.add(Box.createVerticalStrut(14));
        
        // Row 4: Top Genres
        main.add(buildBottomRow());

        JScrollPane scrollPane = new JScrollPane(main);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ─── KPI Row ─────────────────────────────────────────────────────────────

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 6, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        lblTotalRevenue   = addStatCard(row, "Total Revenue", "₹ —", CineBookTheme.ACCENT_GOLD);
        lblTodayRevenue   = addStatCard(row, "Today's Revenue", "₹ —", CineBookTheme.SUCCESS_COLOR);
        lblMonthRevenue   = addStatCard(row, "This Month", "₹ —", CineBookTheme.ACCENT_CYAN);
        lblTotalBookings  = addStatCard(row, "Total Bookings", "—", CineBookTheme.ACCENT_PURPLE);
        lblTodayBookings  = addStatCard(row, "Bookings Today", "—", CineBookTheme.ACCENT_BLUE);
        lblTicketsSold    = addStatCard(row, "Tickets Sold", "—", new Color(255, 107, 107));

        return row;
    }

    private JLabel addStatCard(JPanel container, String title, String initialValue, Color accentColor) {
        GlassCardPanel card = new GlassCardPanel(new GridBagLayout());
        card.setTopAccent(accentColor, 3);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; gbc.gridx = 0;
        gbc.insets = new Insets(4, 10, 0, 10);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ThemeManager.getSmallFont());
        titleLbl.setForeground(CineBookTheme.TEXT_MUTED);
        card.add(titleLbl, gbc);

        gbc.gridy = 1;
        JLabel valueLbl = new JLabel(initialValue);
        valueLbl.setFont(ThemeManager.getFont(Font.BOLD, 20));
        valueLbl.setForeground(accentColor);
        card.add(valueLbl, gbc);

        container.add(card);
        return valueLbl;
    }

    // ─── Chart Row ───────────────────────────────────────────────────────────

    private JPanel buildChartRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Bar chart
        GlassCardPanel chartCard = new GlassCardPanel(new BorderLayout(0, 8));
        chartCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);
        chartCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel chartTitle = new JLabel("Daily Bookings (Last Period)");
        chartTitle.setFont(ThemeManager.getCardTitleFont());
        chartTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        chartCard.add(chartTitle, BorderLayout.NORTH);
        barChartPanel = new BarChartPanel();
        chartCard.add(barChartPanel, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.weightx = 0.65; gbc.insets = new Insets(0, 0, 0, 8);
        row.add(chartCard, gbc);

        // Seat type breakdown
        GlassCardPanel seatCard = new GlassCardPanel(new BorderLayout(0, 8));
        seatCard.setTopAccent(CineBookTheme.ACCENT_GOLD, 3);
        seatCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel seatTitle = new JLabel("Tickets by Seat Category");
        seatTitle.setFont(ThemeManager.getCardTitleFont());
        seatTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        seatCard.add(seatTitle, BorderLayout.NORTH);

        String[] seatCols = {"Seat Type", "Tickets Sold"};
        seatTypeModel = new DefaultTableModel(seatCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        seatTypeTable = new JTable(seatTypeModel);
        ThemeManager.styleTable(seatTypeTable);
        JScrollPane sp = new JScrollPane(seatTypeTable);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CineBookTheme.BG_CARD);
        seatCard.add(sp, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 0.35; gbc.insets = new Insets(0, 8, 0, 0);
        row.add(seatCard, gbc);

        return row;
    }

    // ─── Table Row ───────────────────────────────────────────────────────────

    private JPanel buildTableRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Top Movies table
        GlassCardPanel moviesCard = new GlassCardPanel(new BorderLayout(0, 8));
        moviesCard.setTopAccent(CineBookTheme.ACCENT_BLUE, 3);
        moviesCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel moviesTitle = new JLabel("🎬  Top Movies by Tickets Sold");
        moviesTitle.setFont(ThemeManager.getCardTitleFont());
        moviesTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        moviesCard.add(moviesTitle, BorderLayout.NORTH);

        String[] movieCols = {"#", "Movie Title", "Tickets Sold"};
        topMoviesModel = new DefaultTableModel(movieCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        topMoviesTable = new JTable(topMoviesModel);
        ThemeManager.styleTable(topMoviesTable);
        centerAlignColumn(topMoviesTable, 0);
        centerAlignColumn(topMoviesTable, 2);
        topMoviesTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        JScrollPane sp1 = new JScrollPane(topMoviesTable);
        sp1.setBorder(BorderFactory.createEmptyBorder());
        sp1.getViewport().setBackground(CineBookTheme.BG_CARD);
        moviesCard.add(sp1, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.weightx = 0.33; gbc.insets = new Insets(0, 0, 0, 8);
        row.add(moviesCard, gbc);

        // Top Rated Movies table
        GlassCardPanel ratedCard = new GlassCardPanel(new BorderLayout(0, 8));
        ratedCard.setTopAccent(CineBookTheme.ACCENT_GOLD, 3);
        ratedCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel ratedTitle = new JLabel(" Top Rated Movies");
        ratedTitle.setIcon(org.kordamp.ikonli.swing.FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.STAR, 16, CineBookTheme.ACCENT_GOLD));
        ratedTitle.setFont(ThemeManager.getCardTitleFont());
        ratedTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        ratedCard.add(ratedTitle, BorderLayout.NORTH);

        String[] ratedCols = {"#", "Movie Title", "Avg Rating"};
        topRatedModel = new DefaultTableModel(ratedCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        topRatedTable = new JTable(topRatedModel);
        ThemeManager.styleTable(topRatedTable);
        centerAlignColumn(topRatedTable, 0);
        centerAlignColumn(topRatedTable, 2);
        topRatedTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        JScrollPane sp3 = new JScrollPane(topRatedTable);
        sp3.setBorder(BorderFactory.createEmptyBorder());
        sp3.getViewport().setBackground(CineBookTheme.BG_CARD);
        ratedCard.add(sp3, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 0.33; gbc.insets = new Insets(0, 4, 0, 4);
        row.add(ratedCard, gbc);

        // Theatre Revenue table
        GlassCardPanel theatreCard = new GlassCardPanel(new BorderLayout(0, 8));
        theatreCard.setTopAccent(CineBookTheme.ACCENT_CYAN, 3);
        theatreCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel theatreTitle = new JLabel("🏛  Theatre Performance");
        theatreTitle.setFont(ThemeManager.getCardTitleFont());
        theatreTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        theatreCard.add(theatreTitle, BorderLayout.NORTH);

        String[] thCols = {"#", "Theatre", "Total Revenue", "Occ %"};
        theatreRevenueModel = new DefaultTableModel(thCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        theatreRevenueTable = new JTable(theatreRevenueModel);
        ThemeManager.styleTable(theatreRevenueTable);
        centerAlignColumn(theatreRevenueTable, 0);
        centerAlignColumn(theatreRevenueTable, 2);
        centerAlignColumn(theatreRevenueTable, 3);
        theatreRevenueTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        JScrollPane sp2 = new JScrollPane(theatreRevenueTable);
        sp2.setBorder(BorderFactory.createEmptyBorder());
        sp2.getViewport().setBackground(CineBookTheme.BG_CARD);
        theatreCard.add(sp2, BorderLayout.CENTER);

        gbc.gridx = 2; gbc.weightx = 0.33; gbc.insets = new Insets(0, 8, 0, 0);
        row.add(theatreCard, gbc);

        return row;
    }

    private JPanel buildBottomRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;

        GlassCardPanel genresCard = new GlassCardPanel(new BorderLayout(0, 8));
        genresCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);
        genresCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel genresTitle = new JLabel("🔥 Top Preferred Genres (Based on Users)");
        genresTitle.setFont(ThemeManager.getCardTitleFont());
        genresTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        genresCard.add(genresTitle, BorderLayout.NORTH);

        String[] cols = {"#", "Genre (Count)"};
        topGenresModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        topGenresTable = new JTable(topGenresModel);
        ThemeManager.styleTable(topGenresTable);
        centerAlignColumn(topGenresTable, 0);
        topGenresTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        JScrollPane sp = new JScrollPane(topGenresTable);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CineBookTheme.BG_CARD);
        genresCard.add(sp, BorderLayout.CENTER);

        row.add(genresCard, gbc);
        return row;
    }

    private void centerAlignColumn(JTable table, int col) {
        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(col).setCellRenderer(cr);
    }

    // ─── Public update methods (called by controller/SwingWorker) ─────────────

    public void updateKpis(BigDecimal totalRevenue, BigDecimal todayRevenue, BigDecimal monthRevenue,
                           int totalBookings, int todayBookings, int ticketsSold) {
        lblTotalRevenue.setText("₹ " + String.format("%,.0f", totalRevenue));
        lblTodayRevenue.setText("₹ " + String.format("%,.0f", todayRevenue));
        lblMonthRevenue.setText("₹ " + String.format("%,.0f", monthRevenue));
        lblTotalBookings.setText(String.valueOf(totalBookings));
        lblTodayBookings.setText(String.valueOf(todayBookings));
        lblTicketsSold.setText(String.valueOf(ticketsSold));
    }

    public void updateDailyChart(Map<String, Integer> dailyCounts) {
        barChartPanel.setData(dailyCounts);
    }

    public void updateTopMovies(Map<String, Integer> topMovies) {
        topMoviesModel.setRowCount(0);
        int rank = 1;
        for (Map.Entry<String, Integer> e : topMovies.entrySet()) {
            topMoviesModel.addRow(new Object[]{rank++, e.getKey(), e.getValue()});
        }
    }

    public void updateTopRatedMovies(Map<String, Double> topRated) {
        topRatedModel.setRowCount(0);
        int rank = 1;
        for (Map.Entry<String, Double> e : topRated.entrySet()) {
            topRatedModel.addRow(new Object[]{
                    rank++,
                    e.getKey(),
                    String.format("%.1f", e.getValue())
            });
        }
    }

    public void updateTopGenres(java.util.List<String> genres) {
        topGenresModel.setRowCount(0);
        int rank = 1;
        for (String g : genres) {
            topGenresModel.addRow(new Object[]{rank++, g});
        }
    }

    public void updateTheatreRevenue(Map<String, BigDecimal> revenueMap, Map<String, Double> occupancyMap) {
        theatreRevenueModel.setRowCount(0);
        int rank = 1;
        for (Map.Entry<String, BigDecimal> e : revenueMap.entrySet()) {
            String name = e.getKey();
            Double occ = occupancyMap.getOrDefault(name, 0.0);
            theatreRevenueModel.addRow(new Object[]{rank++, name, "₹ " + String.format("%,.0f", e.getValue()), occ + "%"});
        }
    }

    public void updateSeatTypeBreakdown(Map<String, Integer> seatTypeMap) {
        seatTypeModel.setRowCount(0);
        for (Map.Entry<String, Integer> e : seatTypeMap.entrySet()) {
            seatTypeModel.addRow(new Object[]{e.getKey(), e.getValue()});
        }
    }

    public void setStatus(String message) {
        lblStatus.setText(message);
    }

    public void setLastUpdated(String text) {
        lblLastUpdated.setText(text);
    }

    // ─── Getters ─────────────────────────────────────────────────────────────
    public JButton getBtnRefreshAnalytics() { return btnRefreshAnalytics; }
    public JComboBox<String> getDateRangeCombo() { return dateRangeCombo; }

    // ─── Inner: Bar Chart ─────────────────────────────────────────────────────

    /**
     * Simple lightweight bar chart drawn with Graphics2D.
     * Avoids any chart library dependency.
     */
    public static class BarChartPanel extends JPanel {

        private Map<String, Integer> data;
        private final Color barColor = CineBookTheme.ACCENT_PURPLE;
        private final Color gridColor = new Color(255, 255, 255, 18);
        private final Color textColor = CineBookTheme.TEXT_MUTED;

        public BarChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 160));
        }

        public void setData(Map<String, Integer> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(textColor);
                g2.setFont(new Font("Inter", Font.PLAIN, 12));
                g2.drawString("No data available", getWidth() / 2 - 50, getHeight() / 2);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int padL = 40, padR = 10, padT = 10, padB = 28;
            int chartW = w - padL - padR;
            int chartH = h - padT - padB;

            int maxVal = data.values().stream().mapToInt(v -> v).max().orElse(1);
            if (maxVal == 0) maxVal = 1;

            int n = data.size();
            float barW = (float) chartW / (n * 1.6f);
            float gap = (float) chartW / n;

            // Grid lines
            g2.setColor(gridColor);
            for (int i = 0; i <= 4; i++) {
                int y = padT + (int) (chartH * i / 4.0);
                g2.drawLine(padL, y, padL + chartW, y);
            }

            int idx = 0;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int val = entry.getValue();
                int barH = (int) ((float) val / maxVal * chartH);
                int x = padL + (int) (gap * idx + gap / 2 - barW / 2);
                int y = padT + chartH - barH;

                // Bar with gradient
                GradientPaint gp = new GradientPaint(x, y, barColor, x, y + barH, barColor.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, (int) barW, barH, 4, 4);

                // Value label
                if (val > 0) {
                    g2.setColor(CineBookTheme.TEXT_PRIMARY);
                    g2.setFont(new Font("Inter", Font.BOLD, 10));
                    String valStr = String.valueOf(val);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(valStr, x + (int) (barW / 2) - fm.stringWidth(valStr) / 2, y - 2);
                }

                // Date label (last 5 chars like "09-01")
                String key = entry.getKey();
                String label = key.length() > 5 ? key.substring(key.length() - 5) : key;
                g2.setColor(textColor);
                g2.setFont(new Font("Inter", Font.PLAIN, 9));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, x + (int) (barW / 2) - fm.stringWidth(label) / 2, h - padB + 14);

                idx++;
            }

            g2.dispose();
        }
    }
}
