package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;
import com.movieticket.view.components.DailyTimelinePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Show Management panel for Admin Dashboard — Showtime Scheduling & Management.
 * Features dark glass card containers over the subtle cinematic background.
 */
public class ShowManagementView extends JPanel {

    // Form fields
    private JTextField showIdField;
    private JComboBox<String> movieCombo;
    private JComboBox<String> theatreCombo;
    private JComboBox<com.movieticket.model.Screen> screenCombo;
    private JTextField dateField;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JTextField priceField;
    private JTextField premiumPriceField;
    private JTextField vipPriceField;
    private JComboBox<String> statusCombo;

    // Action buttons
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton searchButton;
    private JButton refreshButton;
    private JTextField searchField;

    // Table
    private JTable showsTable;
    private DefaultTableModel tableModel;
    
    // Timeline
    private DailyTimelinePanel timelinePanel;

    public ShowManagementView() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
    }

    private void initComponents() {
        // 1. Top Panel: Header and Search Bar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);

        JLabel titleLabel = new JLabel("Showtime Scheduling & Management");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Schedule showtimes, assign theatre screens, and set ticket pricing");
        subtitleLabel.setFont(ThemeManager.getBodyFont());
        subtitleLabel.setForeground(CineBookTheme.TEXT_MUTED);

        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(3));
        titleContainer.add(subtitleLabel);
        topPanel.add(titleContainer, BorderLayout.WEST);

        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchContainer.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(ThemeManager.getLabelFont());
        lblSearch.setForeground(CineBookTheme.TEXT_MUTED);
        searchContainer.add(lblSearch);

        searchField = new JTextField();
        ThemeManager.styleTextField(searchField);
        searchField.setPreferredSize(new Dimension(200, 36));
        searchContainer.add(searchField);

        searchButton = new JButton("Search");
        ThemeManager.stylePrimaryButton(searchButton);
        searchButton.setPreferredSize(new Dimension(100, 36));
        searchContainer.add(searchButton);

        refreshButton = new JButton("Refresh");
        ThemeManager.styleSecondaryButton(refreshButton);
        refreshButton.setPreferredSize(new Dimension(100, 36));
        searchContainer.add(refreshButton);

        topPanel.add(searchContainer, BorderLayout.EAST);
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
        fGbc.insets = new Insets(4, 5, 4, 5);
        fGbc.gridx = 0;

        int row = 0;
        fGbc.gridy = row++;
        fGbc.gridwidth = 2;
        JLabel formHeader = new JLabel("Show Schedule Form");
        formHeader.setFont(ThemeManager.getCardTitleFont());
        formHeader.setForeground(CineBookTheme.TEXT_PRIMARY);
        formCard.add(formHeader, fGbc);

        fGbc.gridwidth = 1;

        // Show ID
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Show ID:"), fGbc);
        fGbc.gridx = 1;
        showIdField = new JTextField();
        showIdField.setEditable(false);
        ThemeManager.styleTextField(showIdField);
        showIdField.setBackground(CineBookTheme.BG_HEADER);
        formCard.add(showIdField, fGbc);

        // Movie Combo
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Select Movie: *"), fGbc);
        fGbc.gridx = 1;
        movieCombo = new JComboBox<>();
        ThemeManager.styleComboBox(movieCombo);
        movieCombo.setPreferredSize(new Dimension(190, 32));
        formCard.add(movieCombo, fGbc);

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
        screenCombo = new JComboBox<>();
        ThemeManager.styleComboBox(screenCombo);
        screenCombo.setPreferredSize(new Dimension(190, 32));
        formCard.add(screenCombo, fGbc);

        // Date
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Date (YYYY-MM-DD): *"), fGbc);
        fGbc.gridx = 1;
        dateField = new JTextField();
        ThemeManager.styleTextField(dateField);
        formCard.add(dateField, fGbc);

        // Start Time
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Start Time (HH:MM): *"), fGbc);
        fGbc.gridx = 1;
        startTimeField = new JTextField();
        ThemeManager.styleTextField(startTimeField);
        formCard.add(startTimeField, fGbc);

        // End Time
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("End Time (HH:MM): *"), fGbc);
        fGbc.gridx = 1;
        endTimeField = new JTextField();
        ThemeManager.styleTextField(endTimeField);
        formCard.add(endTimeField, fGbc);

        // Ticket Price
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Regular Price (₹): *"), fGbc);
        fGbc.gridx = 1;
        priceField = new JTextField();
        ThemeManager.styleTextField(priceField);
        formCard.add(priceField, fGbc);

        // Premium Price
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Premium Price (₹):"), fGbc);
        fGbc.gridx = 1;
        premiumPriceField = new JTextField();
        ThemeManager.styleTextField(premiumPriceField);
        premiumPriceField.setToolTipText("Leave blank to default to Regular + 50");
        formCard.add(premiumPriceField, fGbc);

        // VIP Price
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("VIP Price (₹):"), fGbc);
        fGbc.gridx = 1;
        vipPriceField = new JTextField();
        ThemeManager.styleTextField(vipPriceField);
        vipPriceField.setToolTipText("Leave blank to default to Regular + 100");
        formCard.add(vipPriceField, fGbc);

        // Status
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Status: *"), fGbc);
        fGbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        ThemeManager.styleComboBox(statusCombo);
        statusCombo.setPreferredSize(new Dimension(190, 32));
        formCard.add(statusCombo, fGbc);

        // Action Buttons inside Form Card
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        fGbc.gridwidth = 2;
        fGbc.insets = new Insets(12, 5, 5, 5);
        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonGrid.setOpaque(false);

        addButton = new JButton("Add Show");
        ThemeManager.styleSuccessButton(addButton);

        updateButton = new JButton("Update Show");
        ThemeManager.stylePrimaryButton(updateButton);

        deleteButton = new JButton("Delete Show");
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

        String[] columns = {"ID", "Movie Title", "Theatre", "Screen", "Date", "Time", "Price", "Premium", "VIP", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        showsTable = new JTable(tableModel);
        ThemeManager.styleTable(showsTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        showsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
        showsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Screen
        showsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Date
        showsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Time
        showsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // Price
        showsTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer); // Premium
        showsTable.getColumnModel().getColumn(8).setCellRenderer(centerRenderer); // VIP
        showsTable.getColumnModel().getColumn(9).setCellRenderer(centerRenderer); // Status

        JScrollPane scrollPane = new JScrollPane(showsTable);
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

        // Assemble Top Content
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);
        mainContent.add(content, BorderLayout.CENTER);

        // Timeline Card at the bottom
        GlassCardPanel timelineCard = new GlassCardPanel(new BorderLayout());
        timelineCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);
        
        JLabel timelineHeader = new JLabel("Daily Show Timeline (Selected Screen)");
        timelineHeader.setFont(ThemeManager.getCardTitleFont());
        timelineHeader.setForeground(CineBookTheme.TEXT_PRIMARY);
        timelineHeader.setBorder(new EmptyBorder(5, 5, 5, 5));
        timelineCard.add(timelineHeader, BorderLayout.NORTH);
        
        timelinePanel = new DailyTimelinePanel();
        timelineCard.add(timelinePanel, BorderLayout.CENTER);
        timelineCard.setPreferredSize(new Dimension(800, 120));
        
        mainContent.add(timelineCard, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getLabelFont());
        l.setForeground(CineBookTheme.TEXT_SECONDARY);
        return l;
    }

    // Getters for form fields and controls
    public JTextField getShowIdField() { return showIdField; }
    public JComboBox<String> getMovieCombo() { return movieCombo; }
    public JComboBox<String> getTheatreCombo() { return theatreCombo; }
    public JComboBox<com.movieticket.model.Screen> getScreenCombo() { return screenCombo; }
    public JTextField getDateField() { return dateField; }
    public JTextField getStartTimeField() { return startTimeField; }
    public JTextField getTimeField() { return startTimeField; }
    public JTextField getEndTimeField() { return endTimeField; }
    public JTextField getPriceField() { return priceField; }
    public JTextField getPremiumPriceField() { return premiumPriceField; }
    public JTextField getVipPriceField() { return vipPriceField; }
    public JComboBox<String> getStatusCombo() { return statusCombo; }
    public JButton getAddButton() { return addButton; }
    public JButton getUpdateButton() { return updateButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getClearButton() { return clearButton; }
    public JButton getSearchButton() { return searchButton; }
    public JButton getRefreshButton() { return refreshButton; }
    public JTextField getSearchField() { return searchField; }
    public JTable getShowsTable() { return showsTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public DailyTimelinePanel getTimelinePanel() { return timelinePanel; }
}
