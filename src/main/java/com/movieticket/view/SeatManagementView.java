package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Seat Management panel for Admin Dashboard — Seat Inventory & Layout.
 * Features translucent glass card containers over the dark cinematic background.
 */
public class SeatManagementView extends JPanel {

    // Form fields
    private JTextField seatIdField;
    private JComboBox<String> theatreCombo;
    private JComboBox<String> screenCombo;
    private JTextField rowField;
    private JTextField seatNumberField;
    private JComboBox<String> seatTypeCombo;
    private JComboBox<String> statusCombo;

    // Action buttons
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton searchButton;
    private JButton refreshButton;
    private JTextField searchField;

    // Filters
    private JComboBox<String> filterTypeCombo;
    private JComboBox<String> filterStatusCombo;

    // Table
    private JTable seatsTable;
    private DefaultTableModel tableModel;

    // Compatibility map for legacy visual layout
    private Map<String, JButton> seatButtons = new HashMap<>();

    public SeatManagementView() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
    }

    private void initComponents() {
        // 1. Top Panel: Header, Search & Quick Filters
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);

        JLabel titleLabel = new JLabel("Seat Inventory & Layout Management");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Configure theatre seating rows, numbers, categories, and availability");
        subtitleLabel.setFont(ThemeManager.getBodyFont());
        subtitleLabel.setForeground(CineBookTheme.TEXT_MUTED);

        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(3));
        titleContainer.add(subtitleLabel);
        topPanel.add(titleContainer, BorderLayout.WEST);

        JPanel controlsContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsContainer.setOpaque(false);

        // Filter Type
        JLabel lblType = new JLabel("Type:");
        lblType.setFont(ThemeManager.getLabelFont());
        lblType.setForeground(CineBookTheme.TEXT_MUTED);
        controlsContainer.add(lblType);

        filterTypeCombo = new JComboBox<>(new String[]{"ALL", "REGULAR", "PREMIUM", "VIP"});
        ThemeManager.styleComboBox(filterTypeCombo);
        filterTypeCombo.setPreferredSize(new Dimension(110, 36));
        controlsContainer.add(filterTypeCombo);

        // Filter Status
        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(ThemeManager.getLabelFont());
        lblStatus.setForeground(CineBookTheme.TEXT_MUTED);
        controlsContainer.add(lblStatus);

        filterStatusCombo = new JComboBox<>(new String[]{"ALL", "AVAILABLE", "INACTIVE"});
        ThemeManager.styleComboBox(filterStatusCombo);
        filterStatusCombo.setPreferredSize(new Dimension(120, 36));
        controlsContainer.add(filterStatusCombo);

        // Search Field
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(ThemeManager.getLabelFont());
        lblSearch.setForeground(CineBookTheme.TEXT_MUTED);
        controlsContainer.add(lblSearch);

        searchField = new JTextField();
        ThemeManager.styleTextField(searchField);
        searchField.setPreferredSize(new Dimension(150, 36));
        controlsContainer.add(searchField);

        searchButton = new JButton("Search");
        ThemeManager.stylePrimaryButton(searchButton);
        searchButton.setPreferredSize(new Dimension(100, 36));
        controlsContainer.add(searchButton);

        refreshButton = new JButton("Refresh");
        ThemeManager.styleSecondaryButton(refreshButton);
        refreshButton.setPreferredSize(new Dimension(100, 36));
        controlsContainer.add(refreshButton);

        topPanel.add(controlsContainer, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. Content Panel: Form (Left) & Table (Right)
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // Left Form Card
        GlassCardPanel formCard = new GlassCardPanel(new GridBagLayout());
        formCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);

        GridBagConstraints fGbc = new GridBagConstraints();
        fGbc.fill = GridBagConstraints.HORIZONTAL;
        fGbc.insets = new Insets(5, 5, 5, 5);
        fGbc.gridx = 0;

        int row = 0;
        fGbc.gridy = row++;
        fGbc.gridwidth = 2;
        JLabel formHeader = new JLabel("Seat Entry Form");
        formHeader.setFont(ThemeManager.getCardTitleFont());
        formHeader.setForeground(CineBookTheme.TEXT_PRIMARY);
        formCard.add(formHeader, fGbc);

        fGbc.gridwidth = 1;

        // Seat ID
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Seat ID:"), fGbc);
        fGbc.gridx = 1;
        seatIdField = new JTextField();
        seatIdField.setEditable(false);
        ThemeManager.styleTextField(seatIdField);
        seatIdField.setBackground(CineBookTheme.BG_HEADER);
        formCard.add(seatIdField, fGbc);

        // Theatre Combo
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Select Theatre: *"), fGbc);
        fGbc.gridx = 1;
        theatreCombo = new JComboBox<>();
        ThemeManager.styleComboBox(theatreCombo);
        theatreCombo.setPreferredSize(new Dimension(190, 32));
        formCard.add(theatreCombo, fGbc);

        // Screen Combo
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Select Screen: *"), fGbc);
        fGbc.gridx = 1;
        screenCombo = new JComboBox<>(new String[]{"Screen 1", "Screen 2", "Screen 3", "Screen 4"});
        ThemeManager.styleComboBox(screenCombo);
        screenCombo.setPreferredSize(new Dimension(190, 32));
        formCard.add(screenCombo, fGbc);

        // Row Name
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Row (e.g. A, B): *"), fGbc);
        fGbc.gridx = 1;
        rowField = new JTextField();
        ThemeManager.styleTextField(rowField);
        formCard.add(rowField, fGbc);

        // Seat Number
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Seat Number: *"), fGbc);
        fGbc.gridx = 1;
        seatNumberField = new JTextField();
        ThemeManager.styleTextField(seatNumberField);
        formCard.add(seatNumberField, fGbc);

        // Seat Type
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Seat Type: *"), fGbc);
        fGbc.gridx = 1;
        seatTypeCombo = new JComboBox<>(new String[]{"REGULAR", "PREMIUM", "VIP"});
        ThemeManager.styleComboBox(seatTypeCombo);
        seatTypeCombo.setPreferredSize(new Dimension(190, 32));
        formCard.add(seatTypeCombo, fGbc);

        // Status
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Status: *"), fGbc);
        fGbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"AVAILABLE", "INACTIVE"});
        ThemeManager.styleComboBox(statusCombo);
        statusCombo.setPreferredSize(new Dimension(190, 32));
        formCard.add(statusCombo, fGbc);

        // Action Buttons inside Form Card
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        fGbc.gridwidth = 2;
        fGbc.insets = new Insets(16, 5, 5, 5);
        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonGrid.setOpaque(false);

        addButton = new JButton("Add Seat");
        ThemeManager.styleSuccessButton(addButton);

        updateButton = new JButton("Update Seat");
        ThemeManager.stylePrimaryButton(updateButton);

        deleteButton = new JButton("Delete Seat");
        ThemeManager.styleDangerButton(deleteButton);

        clearButton = new JButton("Clear Form");
        ThemeManager.styleSecondaryButton(clearButton);

        buttonGrid.add(addButton);
        buttonGrid.add(updateButton);
        buttonGrid.add(deleteButton);
        buttonGrid.add(clearButton);
        formCard.add(buttonGrid, fGbc);

        // Right Table Card
        GlassCardPanel tableCard = new GlassCardPanel(new BorderLayout());
        tableCard.setTopAccent(CineBookTheme.ACCENT_BLUE, 3);

        String[] columns = {"ID", "Theatre", "Screen", "Row", "Seat Number", "Type", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        seatsTable = new JTable(tableModel);
        ThemeManager.styleTable(seatsTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        seatsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        seatsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        seatsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        seatsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        seatsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(seatsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CineBookTheme.BG_CARD);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Assemble Grid Layout
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 10);
        content.add(formCard, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 10, 0, 0);
        content.add(tableCard, gbc);

        add(content, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getLabelFont());
        l.setForeground(CineBookTheme.TEXT_SECONDARY);
        return l;
    }

    // Legacy backwards compatibility methods
    public void resetAllSeats() {}
    public void setSeatStatus(String seatNum, boolean isBooked) {}
    public Map<String, JButton> getSeatButtons() { return seatButtons; }

    // Getters for form fields and controls
    public JTextField getSeatIdField() { return seatIdField; }
    public JComboBox<String> getTheatreCombo() { return theatreCombo; }
    public JComboBox<String> getScreenCombo() { return screenCombo; }
    public JTextField getRowField() { return rowField; }
    public JTextField getSeatNumberField() { return seatNumberField; }
    public JComboBox<String> getSeatTypeCombo() { return seatTypeCombo; }
    public JComboBox<String> getStatusCombo() { return statusCombo; }
    public JButton getAddButton() { return addButton; }
    public JButton getUpdateButton() { return updateButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getClearButton() { return clearButton; }
    public JButton getSearchButton() { return searchButton; }
    public JButton getRefreshButton() { return refreshButton; }
    public JTextField getSearchField() { return searchField; }
    public JComboBox<String> getFilterTypeCombo() { return filterTypeCombo; }
    public JComboBox<String> getFilterStatusCombo() { return filterStatusCombo; }
    public JTable getSeatsTable() { return seatsTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
}
