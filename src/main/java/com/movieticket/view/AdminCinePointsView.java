package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminCinePointsView extends JPanel {

    private JLabel totalIssuedLabel;
    private JLabel totalRedeemedLabel;

    public AdminCinePointsView() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
    }

    private void initComponents() {
        // Title
        JLabel titleLabel = new JLabel("CinePoints Administration");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        centerPanel.setOpaque(false);

        // Card 1: Issued Points
        GlassCardPanel issuedCard = new GlassCardPanel(new BorderLayout(15, 15));
        issuedCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 4);
        issuedCard.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblIssuedTitle = new JLabel("Total Points Issued");
        lblIssuedTitle.setFont(ThemeManager.getSectionHeaderFont());
        lblIssuedTitle.setForeground(CineBookTheme.TEXT_MUTED);

        totalIssuedLabel = new JLabel("0");
        totalIssuedLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        totalIssuedLabel.setForeground(CineBookTheme.ACCENT_PURPLE);

        issuedCard.add(lblIssuedTitle, BorderLayout.NORTH);
        issuedCard.add(totalIssuedLabel, BorderLayout.CENTER);

        // Card 2: Redeemed Points
        GlassCardPanel redeemedCard = new GlassCardPanel(new BorderLayout(15, 15));
        redeemedCard.setTopAccent(CineBookTheme.ACCENT_CYAN, 4);
        redeemedCard.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblRedeemedTitle = new JLabel("Total Points Redeemed");
        lblRedeemedTitle.setFont(ThemeManager.getSectionHeaderFont());
        lblRedeemedTitle.setForeground(CineBookTheme.TEXT_MUTED);

        totalRedeemedLabel = new JLabel("0");
        totalRedeemedLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        totalRedeemedLabel.setForeground(CineBookTheme.ACCENT_CYAN);

        redeemedCard.add(lblRedeemedTitle, BorderLayout.NORTH);
        redeemedCard.add(totalRedeemedLabel, BorderLayout.CENTER);

        centerPanel.add(issuedCard);
        centerPanel.add(redeemedCard);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(centerPanel, BorderLayout.NORTH);

        add(wrapperPanel, BorderLayout.CENTER);
    }

    public void setMetrics(int totalIssued, int totalRedeemed) {
        totalIssuedLabel.setText(String.valueOf(totalIssued));
        totalRedeemedLabel.setText(String.valueOf(totalRedeemed));
    }
}
