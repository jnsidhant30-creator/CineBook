package com.movieticket.view;

import com.movieticket.model.CinePointsTransaction;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CinePointsWalletView extends JPanel {

    private JLabel balanceLabel;
    private JLabel pendingLabel;
    private JTable historyTable;
    private DefaultTableModel tableModel;

    public CinePointsWalletView() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
    }

    private void initComponents() {
        // Title
        JLabel titleLabel = new JLabel("CinePoints Wallet");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setOpaque(false);

        // Top Card: Balance
        GlassCardPanel balanceCard = new GlassCardPanel(new BorderLayout(15, 15));
        balanceCard.setTopAccent(CineBookTheme.ACCENT_GOLD, 4);
        balanceCard.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("Available Points");
        lblTitle.setFont(ThemeManager.getSectionHeaderFont());
        lblTitle.setForeground(CineBookTheme.TEXT_MUTED);

        balanceLabel = new JLabel("0");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        balanceLabel.setForeground(CineBookTheme.ACCENT_GOLD);
        
        pendingLabel = new JLabel("Pending: 0");
        pendingLabel.setFont(ThemeManager.getLabelFont());
        pendingLabel.setForeground(CineBookTheme.ACCENT_BLUE);

        JLabel lblInfo = new JLabel("Earn 10 CinePoints for every ₹100 spent. 100 CinePoints = ₹50 off!");
        lblInfo.setFont(ThemeManager.getLabelFont());
        lblInfo.setForeground(CineBookTheme.TEXT_SECONDARY);

        JPanel balanceContent = new JPanel(new GridLayout(4, 1));
        balanceContent.setOpaque(false);
        balanceContent.add(lblTitle);
        balanceContent.add(balanceLabel);
        balanceContent.add(pendingLabel);
        balanceContent.add(lblInfo);

        balanceCard.add(balanceContent, BorderLayout.CENTER);
        
        // Add an icon or illustration if possible (using a simple label for now)
        JLabel iconLabel = new JLabel("🎁");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        balanceCard.add(iconLabel, BorderLayout.EAST);

        centerPanel.add(balanceCard, BorderLayout.NORTH);

        // Bottom Card: History
        GlassCardPanel historyCard = new GlassCardPanel(new BorderLayout(10, 10));
        historyCard.setTopAccent(CineBookTheme.ACCENT_BLUE, 3);
        historyCard.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel historyTitle = new JLabel("Points History");
        historyTitle.setFont(ThemeManager.getCardTitleFont());
        historyTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        historyCard.add(historyTitle, BorderLayout.NORTH);

        String[] columns = {"Date", "Type", "Points", "Description", "Status", "Balance After"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        historyTable = new JTable(tableModel);
        ThemeManager.styleTable(historyTable);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.getViewport().setBackground(CineBookTheme.BG_SECONDARY);
        scrollPane.setBorder(BorderFactory.createLineBorder(CineBookTheme.BORDER_COLOR));

        historyCard.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(historyCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void updateWallet(int balance, List<CinePointsTransaction> history) {
        balanceLabel.setText(String.valueOf(balance));
        tableModel.setRowCount(0);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        int pendingPoints = 0;
        for (CinePointsTransaction tx : history) {
            if ("PENDING".equalsIgnoreCase(tx.getStatus())) {
                pendingPoints += tx.getPoints();
            }
            
            String desc = tx.getDescription();
            if ("PENDING".equalsIgnoreCase(tx.getStatus())) {
                desc += " (Pending until show starts)";
            }

            tableModel.addRow(new Object[]{
                    tx.getCreatedAt() != null ? tx.getCreatedAt().format(dtf) : "—",
                    tx.getTransactionType(),
                    (tx.getTransactionType().equals("EARNED") ? "+" : "-") + tx.getPoints(),
                    desc,
                    tx.getStatus(),
                    tx.getBalanceAfter()
            });
        }
        pendingLabel.setText("Pending: " + pendingPoints);
    }
}
