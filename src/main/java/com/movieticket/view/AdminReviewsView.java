package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminReviewsView extends JPanel {

    private JTable reviewsTable;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;
    private JButton btnHideReview;
    private JButton btnRestoreReview;

    public AdminReviewsView() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Review Moderation");
        lblTitle.setFont(ThemeManager.getPageTitleFont());
        lblTitle.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel("Moderate user reviews for movies and theatres across the platform.");
        lblSubtitle.setFont(ThemeManager.getBodyFont());
        lblSubtitle.setForeground(CineBookTheme.TEXT_MUTED);

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(lblSubtitle);

        headerPanel.add(titleBox, BorderLayout.WEST);

        btnRefresh = new JButton("Refresh");
        ThemeManager.styleSecondaryButton(btnRefresh);
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Table Card
        GlassCardPanel cardPanel = new GlassCardPanel(new BorderLayout());
        cardPanel.setTopAccent(CineBookTheme.ACCENT_PURPLE, 4);

        String[] columns = {"ID", "Date", "User", "Movie", "Theatre", "Rating", "Review Text", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reviewsTable = new JTable(tableModel);
        ThemeManager.styleTable(reviewsTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        reviewsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        reviewsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        reviewsTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        
        reviewsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        reviewsTable.getColumnModel().getColumn(6).setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(reviewsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CineBookTheme.BG_CARD);

        cardPanel.add(scrollPane, BorderLayout.CENTER);
        add(cardPanel, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomActions.setOpaque(false);

        btnRestoreReview = new JButton("Restore Review");
        ThemeManager.stylePrimaryButton(btnRestoreReview);
        
        btnHideReview = new JButton("Hide Review");
        ThemeManager.styleDangerButton(btnHideReview);

        bottomActions.add(btnRestoreReview);
        bottomActions.add(btnHideReview);

        add(bottomActions, BorderLayout.SOUTH);
    }

    public JTable getReviewsTable() { return reviewsTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnHideReview() { return btnHideReview; }
    public JButton getBtnRestoreReview() { return btnRestoreReview; }
}
