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
 * TheatreScreenManagementView — Multi-Theatre & Screen Management.
 * Provides a tabbed interface: [Theatres] [Screens].
 * Part of Phase 5 Admin Dashboard upgrade.
 */
public class TheatreScreenManagementView extends JPanel {

    // ─── Tab 1: Theatres ─────────────────────────────────────────────────────
    private JTextField theatreIdField;
    private JTextField theatreNameField;
    private JTextField theatreLocationField;
    private JTextField theatreCityField;
    private JTextField theatreCapacityField;
    private JTextField theatreContactField;
    private JComboBox<String> theatreStatusCombo;
    private JButton addTheatreBtn;
    private JButton updateTheatreBtn;
    private JButton deleteTheatreBtn;
    private JButton clearTheatreBtn;
    private JTextField theatreSearchField;
    private JButton theatreSearchBtn;
    private JButton theatreRefreshBtn;
    private JTable theatresTable;
    private DefaultTableModel theatresTableModel;

    // ─── Tab 2: Screens ──────────────────────────────────────────────────────
    private JTextField screenIdField;
    private JTextField screenNameField;
    private JComboBox<String> screenTheatreCombo;
    private JComboBox<String> screenTypeCombo;
    private JTextField screenCapacityField;
    private JComboBox<String> screenStatusCombo;
    private JButton addScreenBtn;
    private JButton updateScreenBtn;
    private JButton deleteScreenBtn;
    private JButton clearScreenBtn;
    private JButton screenRefreshBtn;
    private JTable screensTable;
    private DefaultTableModel screensTableModel;

    private JTabbedPane tabbedPane;

    public TheatreScreenManagementView() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
    }

    private void initComponents() {
        // Page Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel titleLabel = new JLabel("Multi-Theatre & Screen Management");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Manage cinema locations, individual screening rooms, types and capacities");
        subtitleLabel.setFont(ThemeManager.getBodyFont());
        subtitleLabel.setForeground(CineBookTheme.TEXT_MUTED);

        titleBox.add(titleLabel);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(subtitleLabel);
        headerPanel.add(titleBox, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ThemeManager.getLabelFont());
        tabbedPane.setBackground(CineBookTheme.BG_CARD);
        tabbedPane.setForeground(CineBookTheme.TEXT_PRIMARY);
        tabbedPane.setOpaque(false);

        tabbedPane.addTab("🏛  Theatres", buildTheatresTab());
        tabbedPane.addTab("🎬  Screens", buildScreensTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ─── Tab 1: Theatres ─────────────────────────────────────────────────────

    private JPanel buildTheatresTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Search / toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);

        JLabel lblSearch = createLabel("Search:");
        theatreSearchField = new JTextField();
        ThemeManager.styleTextField(theatreSearchField);
        theatreSearchField.setPreferredSize(new Dimension(200, 34));

        theatreSearchBtn = new JButton("Search");
        ThemeManager.stylePrimaryButton(theatreSearchBtn);
        theatreSearchBtn.setPreferredSize(new Dimension(90, 34));

        theatreRefreshBtn = new JButton("Refresh");
        ThemeManager.styleSecondaryButton(theatreRefreshBtn);
        theatreRefreshBtn.setPreferredSize(new Dimension(90, 34));

        toolbar.add(lblSearch);
        toolbar.add(theatreSearchField);
        toolbar.add(theatreSearchBtn);
        toolbar.add(theatreRefreshBtn);
        tab.add(toolbar, BorderLayout.NORTH);

        // Center: form + table
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Form card
        GlassCardPanel formCard = new GlassCardPanel(new GridBagLayout());
        formCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);

        GridBagConstraints fg = new GridBagConstraints();
        fg.fill = GridBagConstraints.HORIZONTAL;
        fg.insets = new Insets(7, 8, 7, 8);
        int r = 0;

        fg.gridwidth = 2; fg.gridx = 0; fg.gridy = r++;
        JLabel formHeader = new JLabel("Theatre Details");
        formHeader.setFont(ThemeManager.getCardTitleFont());
        formHeader.setForeground(CineBookTheme.TEXT_PRIMARY);
        formCard.add(formHeader, fg);
        fg.gridwidth = 1;

        theatreIdField = addFormRow(formCard, fg, r++, "Theatre ID:", true);
        theatreNameField = addFormRow(formCard, fg, r++, "Theatre Name: *", false);
        theatreLocationField = addFormRow(formCard, fg, r++, "Location / Address: *", false);
        theatreCityField = addFormRow(formCard, fg, r++, "City: *", false);
        theatreContactField = addFormRow(formCard, fg, r++, "Contact Number:", false);
        theatreCapacityField = addFormRow(formCard, fg, r++, "Total Seats: *", false);

        // Status combo
        fg.gridx = 0; fg.gridy = r;
        formCard.add(createLabel("Status:"), fg);
        fg.gridx = 1; fg.gridy = r++;
        theatreStatusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        ThemeManager.styleComboBox(theatreStatusCombo);
        formCard.add(theatreStatusCombo, fg);

        // Buttons
        fg.gridx = 0; fg.gridy = r++; fg.gridwidth = 2;
        fg.insets = new Insets(18, 8, 8, 8);
        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);
        addTheatreBtn = new JButton("Add Theatre"); ThemeManager.styleSuccessButton(addTheatreBtn);
        updateTheatreBtn = new JButton("Update Theatre"); ThemeManager.stylePrimaryButton(updateTheatreBtn);
        deleteTheatreBtn = new JButton("Delete Theatre"); ThemeManager.styleDangerButton(deleteTheatreBtn);
        clearTheatreBtn = new JButton("Clear Form"); ThemeManager.styleSecondaryButton(clearTheatreBtn);
        btnGrid.add(addTheatreBtn); btnGrid.add(updateTheatreBtn);
        btnGrid.add(deleteTheatreBtn); btnGrid.add(clearTheatreBtn);
        formCard.add(btnGrid, fg);

        // Table card
        GlassCardPanel tableCard = new GlassCardPanel(new BorderLayout());
        tableCard.setTopAccent(CineBookTheme.ACCENT_BLUE, 3);

        String[] cols = {"ID", "Theatre Name", "Location", "City", "Contact", "Total Seats", "Screens", "Status"};
        theatresTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        theatresTable = new JTable(theatresTableModel);
        ThemeManager.styleTable(theatresTable);
        theatresTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        theatresTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        theatresTable.getColumnModel().getColumn(6).setPreferredWidth(60);
        theatresTable.getColumnModel().getColumn(7).setPreferredWidth(70);

        centerAlignColumns(theatresTable, 0, 5, 6);

        JScrollPane sp = new JScrollPane(theatresTable);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CineBookTheme.BG_CARD);
        tableCard.add(sp, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.weightx = 0.32; gbc.insets = new Insets(0, 0, 0, 8);
        center.add(formCard, gbc);
        gbc.gridx = 1; gbc.weightx = 0.68; gbc.insets = new Insets(0, 8, 0, 0);
        center.add(tableCard, gbc);

        tab.add(center, BorderLayout.CENTER);
        return tab;
    }

    // ─── Tab 2: Screens ──────────────────────────────────────────────────────

    private JPanel buildScreensTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);
        screenRefreshBtn = new JButton("Refresh");
        ThemeManager.styleSecondaryButton(screenRefreshBtn);
        screenRefreshBtn.setPreferredSize(new Dimension(90, 34));
        toolbar.add(screenRefreshBtn);
        tab.add(toolbar, BorderLayout.NORTH);

        // Center: form + table
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Form card
        GlassCardPanel formCard = new GlassCardPanel(new GridBagLayout());
        formCard.setTopAccent(CineBookTheme.ACCENT_CYAN, 3);

        GridBagConstraints fg = new GridBagConstraints();
        fg.fill = GridBagConstraints.HORIZONTAL;
        fg.insets = new Insets(7, 8, 7, 8);
        int r = 0;

        fg.gridwidth = 2; fg.gridx = 0; fg.gridy = r++;
        JLabel formHeader = new JLabel("Screen Details");
        formHeader.setFont(ThemeManager.getCardTitleFont());
        formHeader.setForeground(CineBookTheme.TEXT_PRIMARY);
        formCard.add(formHeader, fg);
        fg.gridwidth = 1;

        // Screen ID
        fg.gridx = 0; fg.gridy = r;
        formCard.add(createLabel("Screen ID:"), fg);
        fg.gridx = 1; fg.gridy = r++;
        screenIdField = new JTextField();
        screenIdField.setEditable(false);
        ThemeManager.styleTextField(screenIdField);
        screenIdField.setBackground(CineBookTheme.BG_HEADER);
        formCard.add(screenIdField, fg);

        // Theatre combo
        fg.gridx = 0; fg.gridy = r;
        formCard.add(createLabel("Theatre: *"), fg);
        fg.gridx = 1; fg.gridy = r++;
        screenTheatreCombo = new JComboBox<>();
        ThemeManager.styleComboBox(screenTheatreCombo);
        formCard.add(screenTheatreCombo, fg);

        // Screen name
        screenNameField = addFormRowAt(formCard, fg, r++, "Screen Name: *", false);

        // Screen type
        fg.gridx = 0; fg.gridy = r;
        formCard.add(createLabel("Screen Type: *"), fg);
        fg.gridx = 1; fg.gridy = r++;
        screenTypeCombo = new JComboBox<>(new String[]{"STANDARD", "IMAX", "4DX", "DOLBY_ATMOS", "GOLD_CLASS", "DRIVE_IN"});
        ThemeManager.styleComboBox(screenTypeCombo);
        formCard.add(screenTypeCombo, fg);

        // Capacity
        screenCapacityField = addFormRowAt(formCard, fg, r++, "Capacity (Seats): *", false);

        // Status
        fg.gridx = 0; fg.gridy = r;
        formCard.add(createLabel("Status:"), fg);
        fg.gridx = 1; fg.gridy = r++;
        screenStatusCombo = new JComboBox<>(new String[]{"ACTIVE", "MAINTENANCE", "CLOSED"});
        ThemeManager.styleComboBox(screenStatusCombo);
        formCard.add(screenStatusCombo, fg);

        // Buttons
        fg.gridx = 0; fg.gridy = r++; fg.gridwidth = 2;
        fg.insets = new Insets(18, 8, 8, 8);
        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);
        addScreenBtn = new JButton("Add Screen"); ThemeManager.styleSuccessButton(addScreenBtn);
        updateScreenBtn = new JButton("Update Screen"); ThemeManager.stylePrimaryButton(updateScreenBtn);
        deleteScreenBtn = new JButton("Delete Screen"); ThemeManager.styleDangerButton(deleteScreenBtn);
        clearScreenBtn = new JButton("Clear Form"); ThemeManager.styleSecondaryButton(clearScreenBtn);
        btnGrid.add(addScreenBtn); btnGrid.add(updateScreenBtn);
        btnGrid.add(deleteScreenBtn); btnGrid.add(clearScreenBtn);
        formCard.add(btnGrid, fg);

        // Table card
        GlassCardPanel tableCard = new GlassCardPanel(new BorderLayout());
        tableCard.setTopAccent(CineBookTheme.ACCENT_GOLD, 3);

        String[] cols = {"Screen ID", "Theatre", "Screen Name", "Type", "Capacity", "Status"};
        screensTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        screensTable = new JTable(screensTableModel);
        ThemeManager.styleTable(screensTable);
        screensTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        screensTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        screensTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        screensTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        centerAlignColumns(screensTable, 0, 4);

        JScrollPane sp = new JScrollPane(screensTable);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CineBookTheme.BG_CARD);
        tableCard.add(sp, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.weightx = 0.35; gbc.insets = new Insets(0, 0, 0, 8);
        center.add(formCard, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; gbc.insets = new Insets(0, 8, 0, 0);
        center.add(tableCard, gbc);

        tab.add(center, BorderLayout.CENTER);
        return tab;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private JTextField addFormRow(JPanel panel, GridBagConstraints fg, int row, String label, boolean readonly) {
        fg.gridx = 0; fg.gridy = row; fg.insets = new Insets(7, 8, 7, 8);
        panel.add(createLabel(label), fg);
        fg.gridx = 1;
        JTextField tf = new JTextField();
        ThemeManager.styleTextField(tf);
        if (readonly) { tf.setEditable(false); tf.setBackground(CineBookTheme.BG_HEADER); }
        panel.add(tf, fg);
        return tf;
    }

    private JTextField addFormRowAt(JPanel panel, GridBagConstraints fg, int row, String label, boolean readonly) {
        fg.gridx = 0; fg.gridy = row; fg.insets = new Insets(7, 8, 7, 8);
        panel.add(createLabel(label), fg);
        fg.gridx = 1;
        JTextField tf = new JTextField();
        ThemeManager.styleTextField(tf);
        if (readonly) { tf.setEditable(false); tf.setBackground(CineBookTheme.BG_HEADER); }
        panel.add(tf, fg);
        return tf;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getLabelFont());
        l.setForeground(CineBookTheme.TEXT_SECONDARY);
        return l;
    }

    private void centerAlignColumns(JTable table, int... columnIndices) {
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int idx : columnIndices) {
            table.getColumnModel().getColumn(idx).setCellRenderer(center);
        }
    }

    // ─── Getters: Theatres Tab ────────────────────────────────────────────────
    public JTextField getTheatreIdField() { return theatreIdField; }
    public JTextField getTheatreNameField() { return theatreNameField; }
    public JTextField getTheatreLocationField() { return theatreLocationField; }
    public JTextField getTheatreCityField() { return theatreCityField; }
    public JTextField getTheatreCapacityField() { return theatreCapacityField; }
    public JTextField getTheatreContactField() { return theatreContactField; }
    public JComboBox<String> getTheatreStatusCombo() { return theatreStatusCombo; }
    public JButton getAddTheatreBtn() { return addTheatreBtn; }
    public JButton getUpdateTheatreBtn() { return updateTheatreBtn; }
    public JButton getDeleteTheatreBtn() { return deleteTheatreBtn; }
    public JButton getClearTheatreBtn() { return clearTheatreBtn; }
    public JTextField getTheatreSearchField() { return theatreSearchField; }
    public JButton getTheatreSearchBtn() { return theatreSearchBtn; }
    public JButton getTheatreRefreshBtn() { return theatreRefreshBtn; }
    public JTable getTheatresTable() { return theatresTable; }
    public DefaultTableModel getTheatresTableModel() { return theatresTableModel; }

    // ─── Getters: Screens Tab ─────────────────────────────────────────────────
    public JTextField getScreenIdField() { return screenIdField; }
    public JTextField getScreenNameField() { return screenNameField; }
    public JComboBox<String> getScreenTheatreCombo() { return screenTheatreCombo; }
    public JComboBox<String> getScreenTypeCombo() { return screenTypeCombo; }
    public JTextField getScreenCapacityField() { return screenCapacityField; }
    public JComboBox<String> getScreenStatusCombo() { return screenStatusCombo; }
    public JButton getAddScreenBtn() { return addScreenBtn; }
    public JButton getUpdateScreenBtn() { return updateScreenBtn; }
    public JButton getDeleteScreenBtn() { return deleteScreenBtn; }
    public JButton getClearScreenBtn() { return clearScreenBtn; }
    public JButton getScreenRefreshBtn() { return screenRefreshBtn; }
    public JTable getScreensTable() { return screensTable; }
    public DefaultTableModel getScreensTableModel() { return screensTableModel; }
    public JTabbedPane getTabbedPane() { return tabbedPane; }
}
