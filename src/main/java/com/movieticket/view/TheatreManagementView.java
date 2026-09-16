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
 * Theatre Management panel for Admin Dashboard — Complex & Hall Operations.
 * Features translucent glass card containers over the cinematic background.
 */
public class TheatreManagementView extends JPanel {

    // Form fields
    private JTextField theatreIdField;
    private JTextField nameField;
    private JTextField locationField;
    private JTextField capacityField;

    // Action buttons
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton searchButton;
    private JButton refreshButton;
    private JTextField searchField;

    // Table
    private JTable theatresTable;
    private DefaultTableModel tableModel;

    public TheatreManagementView() {
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

        JLabel titleLabel = new JLabel("Theatre & Complex Management");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Manage cinema locations, screening halls, and seat capacities");
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
        fGbc.insets = new Insets(8, 8, 8, 8);
        fGbc.gridx = 0;

        int row = 0;
        fGbc.gridy = row++;
        fGbc.gridwidth = 2;
        JLabel formHeader = new JLabel("Theatre Hall Details");
        formHeader.setFont(ThemeManager.getCardTitleFont());
        formHeader.setForeground(CineBookTheme.TEXT_PRIMARY);
        formCard.add(formHeader, fGbc);

        fGbc.gridwidth = 1;

        // ID
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Theatre ID:"), fGbc);
        fGbc.gridx = 1;
        theatreIdField = new JTextField();
        theatreIdField.setEditable(false);
        ThemeManager.styleTextField(theatreIdField);
        theatreIdField.setBackground(CineBookTheme.BG_HEADER);
        formCard.add(theatreIdField, fGbc);

        // Name
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Theatre Name: *"), fGbc);
        fGbc.gridx = 1;
        nameField = new JTextField();
        ThemeManager.styleTextField(nameField);
        formCard.add(nameField, fGbc);

        // Location
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Location: *"), fGbc);
        fGbc.gridx = 1;
        locationField = new JTextField();
        ThemeManager.styleTextField(locationField);
        formCard.add(locationField, fGbc);

        // Capacity
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        formCard.add(createLabel("Total Seats: *"), fGbc);
        fGbc.gridx = 1;
        capacityField = new JTextField();
        ThemeManager.styleTextField(capacityField);
        formCard.add(capacityField, fGbc);

        // Action Buttons inside Form Card
        fGbc.gridy = row++;
        fGbc.gridx = 0;
        fGbc.gridwidth = 2;
        fGbc.insets = new Insets(20, 8, 8, 8);
        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonGrid.setOpaque(false);

        addButton = new JButton("Add Theatre");
        ThemeManager.styleSuccessButton(addButton);

        updateButton = new JButton("Update Theatre");
        ThemeManager.stylePrimaryButton(updateButton);

        deleteButton = new JButton("Delete Theatre");
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

        String[] columns = {"ID", "Theatre Name", "Location", "Total Seats"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        theatresTable = new JTable(tableModel);
        ThemeManager.styleTable(theatresTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        theatresTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        theatresTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(theatresTable);
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

    // Getters for form validation and controllers
    public JTextField getTheatreIdField() { return theatreIdField; }
    public JTextField getNameField() { return nameField; }
    public JTextField getLocationField() { return locationField; }
    public JTextField getCapacityField() { return capacityField; }
    public JButton getAddButton() { return addButton; }
    public JButton getUpdateButton() { return updateButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getClearButton() { return clearButton; }
    public JButton getSearchButton() { return searchButton; }
    public JButton getRefreshButton() { return refreshButton; }
    public JTextField getSearchField() { return searchField; }
    public JTable getTheatresTable() { return theatresTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
}
