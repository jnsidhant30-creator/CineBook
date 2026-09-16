package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Booking Management panel for Admin Dashboard — Customer Booking Operations.
 * Features translucent glass card containers over the dark cinematic background.
 */
public class BookingManagementView extends JPanel {

    private JTextField searchField;
    private JButton searchButton;
    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private JButton cancelBookingButton;

    public BookingManagementView() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
    }

    private void initComponents() {
        // 1. Top Panel: Header and Search
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);

        JLabel titleLabel = new JLabel("Customer Booking Operations");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Audit customer transactions, review seats, and manage booking statuses");
        subtitleLabel.setFont(ThemeManager.getBodyFont());
        subtitleLabel.setForeground(CineBookTheme.TEXT_MUTED);

        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(3));
        titleContainer.add(subtitleLabel);
        topPanel.add(titleContainer, BorderLayout.WEST);

        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchContainer.setOpaque(false);

        searchField = new JTextField();
        ThemeManager.styleTextField(searchField);
        searchField.setPreferredSize(new Dimension(220, 36));
        searchContainer.add(searchField);

        searchButton = new JButton("Search by ID");
        ThemeManager.stylePrimaryButton(searchButton);
        searchButton.setPreferredSize(new Dimension(130, 36));
        searchContainer.add(searchButton);

        topPanel.add(searchContainer, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. Table Container Card
        GlassCardPanel tableCard = new GlassCardPanel(new BorderLayout());
        tableCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);

        String[] columns = {"Booking ID", "Customer Account", "Show", "Theatre Hall", "Seats Booked", "Paid (₹)", "Status", "Pay Method", "Txn ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        bookingsTable = new JTable(tableModel);
        ThemeManager.styleTable(bookingsTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        bookingsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        bookingsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        bookingsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CineBookTheme.BG_CARD);
        tableCard.add(scrollPane, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        // 3. Actions Panel
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);

        cancelBookingButton = new JButton("Cancel Selected Booking");
        ThemeManager.styleDangerButton(cancelBookingButton);
        cancelBookingButton.setPreferredSize(new Dimension(230, 38));
        actions.add(cancelBookingButton);

        add(actions, BorderLayout.SOUTH);
    }

    // Getters
    public JTextField getSearchField() { return searchField; }
    public JButton getSearchButton() { return searchButton; }
    public JTable getBookingsTable() { return bookingsTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getCancelBookingButton() { return cancelBookingButton; }
}
