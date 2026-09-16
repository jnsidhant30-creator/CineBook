package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Admin Reports & Business Analytics screen — Financial Audits & Sales Reports.
 * Features translucent glass card containers over the dark cinematic background.
 */
public class ReportManagementView extends JPanel {

    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel lblReportTitle;
    private JLabel lblSummaryText;
    private JLabel lblEmptyState;
    private JPanel emptyStatePanel;
    private JScrollPane tableScrollPane;

    private JButton btnBookingReport;
    private JButton btnMovieReport;
    private JButton btnShowReport;
    private JButton btnRefresh;
    private JButton btnBack;

    public ReportManagementView() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initComponents();
    }

    private void initComponents() {
        // 1. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);

        JLabel titleLabel = new JLabel("System Analytics & Financial Reports");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Inspect live transaction audits, movie ticket sales, and show revenues");
        subtitleLabel.setFont(ThemeManager.getBodyFont());
        subtitleLabel.setForeground(CineBookTheme.TEXT_MUTED);

        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(4));
        titleContainer.add(subtitleLabel);
        headerPanel.add(titleContainer, BorderLayout.WEST);

        // Header Action Buttons
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topButtons.setOpaque(false);

        btnRefresh = new JButton("Refresh Report");
        ThemeManager.stylePrimaryButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(140, 36));

        btnBack = new JButton("Back to Dashboard");
        ThemeManager.styleSecondaryButton(btnBack);
        btnBack.setPreferredSize(new Dimension(160, 36));

        topButtons.add(btnRefresh);
        topButtons.add(btnBack);
        headerPanel.add(topButtons, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Card Panel
        GlassCardPanel centerCard = new GlassCardPanel(new BorderLayout(10, 10));
        centerCard.setTopAccent(CineBookTheme.ACCENT_GOLD, 3);

        // Report Type Switcher Tabs
        JPanel tabSwitcherPanel = new JPanel(new BorderLayout());
        tabSwitcherPanel.setOpaque(false);

        JPanel tabsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabsLeft.setOpaque(false);

        btnBookingReport = createTabButton("All Bookings Report", true);
        btnMovieReport = createTabButton("Movie-Wise Report", false);
        btnShowReport = createTabButton("Show-Wise Report", false);

        tabsLeft.add(btnBookingReport);
        tabsLeft.add(btnMovieReport);
        tabsLeft.add(btnShowReport);

        tabSwitcherPanel.add(tabsLeft, BorderLayout.WEST);

        lblReportTitle = new JLabel("All System Bookings");
        lblReportTitle.setFont(ThemeManager.getCardTitleFont());
        lblReportTitle.setForeground(CineBookTheme.ACCENT_GOLD);
        tabSwitcherPanel.add(lblReportTitle, BorderLayout.EAST);

        centerCard.add(tabSwitcherPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        reportTable = new JTable(tableModel);
        ThemeManager.styleTable(reportTable);

        tableScrollPane = new JScrollPane(reportTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(CineBookTheme.BG_CARD);

        // Empty state container
        emptyStatePanel = new JPanel(new GridBagLayout());
        emptyStatePanel.setOpaque(false);
        lblEmptyState = new JLabel("No report data available.");
        lblEmptyState.setFont(ThemeManager.getCardTitleFont());
        lblEmptyState.setForeground(CineBookTheme.TEXT_MUTED);
        emptyStatePanel.add(lblEmptyState);
        emptyStatePanel.setVisible(false);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.add(tableScrollPane, BorderLayout.CENTER);
        tableContainer.add(emptyStatePanel, BorderLayout.SOUTH);

        centerCard.add(tableContainer, BorderLayout.CENTER);

        // Summary Bar
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, CineBookTheme.BORDER_COLOR),
                new EmptyBorder(10, 12, 10, 12)
        ));

        lblSummaryText = new JLabel("Total Records: 0 | Tickets Sold: 0 | Total Revenue: ₹0.00");
        lblSummaryText.setFont(ThemeManager.getCardTitleFont());
        lblSummaryText.setForeground(CineBookTheme.SUCCESS_COLOR);
        summaryPanel.add(lblSummaryText, BorderLayout.WEST);

        centerCard.add(summaryPanel, BorderLayout.SOUTH);

        add(centerCard, BorderLayout.CENTER);
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeManager.getButtonFont());
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(170, 34));
        updateTabStyle(btn, active);
        return btn;
    }

    public void updateTabStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(CineBookTheme.ACCENT_PURPLE);
            btn.setForeground(CineBookTheme.TEXT_PRIMARY);
            btn.setBorder(BorderFactory.createEmptyBorder());
        } else {
            btn.setBackground(CineBookTheme.BG_INPUT);
            btn.setForeground(CineBookTheme.TEXT_MUTED);
            btn.setBorder(BorderFactory.createLineBorder(CineBookTheme.BORDER_COLOR, 1));
        }
    }

    public void setEmptyStateVisible(boolean visible) {
        emptyStatePanel.setVisible(visible);
        tableScrollPane.setVisible(!visible);
        revalidate();
        repaint();
    }

    // Getters & Setters
    public JTable getReportTable() { return reportTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JLabel getLblReportTitle() { return lblReportTitle; }
    public JLabel getLblSummaryText() { return lblSummaryText; }
    public JButton getBtnBookingReport() { return btnBookingReport; }
    public JButton getBtnMovieReport() { return btnMovieReport; }
    public JButton getBtnShowReport() { return btnShowReport; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnBack() { return btnBack; }
}
