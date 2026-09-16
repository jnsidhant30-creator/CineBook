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
 * User Management panel for Admin Dashboard — Accounts & Privileges.
 * Features translucent glass card containers over the dark cinematic background.
 */
public class UserManagementView extends JPanel {

    private JTextField userIdField;
    private JTextField usernameField;
    private JComboBox<String> roleCombo;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton searchButton;
    private JButton refreshButton;
    private JTextField searchField;

    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserManagementView() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
    }

    private void initComponents() {
        // 1. Top Header & Search Bar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);

        JLabel titleLabel = new JLabel("User Account Management");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Manage application accounts, privileges, and system roles");
        subtitleLabel.setFont(ThemeManager.getBodyFont());
        subtitleLabel.setForeground(CineBookTheme.TEXT_MUTED);

        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(4));
        titleContainer.add(subtitleLabel);
        topPanel.add(titleContainer, BorderLayout.WEST);

        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchContainer.setOpaque(false);

        JLabel lblSearch = new JLabel("Search User:");
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

        // 2. Split Content Area: Form Card (Left) & User Table Card (Right)
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // Left Form Card
        GlassCardPanel formCard = new GlassCardPanel(new GridBagLayout());
        formCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.insets = new Insets(8, 8, 8, 8);
        formGbc.gridx = 0;

        int row = 0;
        formGbc.gridy = row++;
        formGbc.gridwidth = 2;
        JLabel formHeader = new JLabel("Account Role Details");
        formHeader.setFont(ThemeManager.getCardTitleFont());
        formHeader.setForeground(CineBookTheme.TEXT_PRIMARY);
        formCard.add(formHeader, formGbc);

        formGbc.gridwidth = 1;

        // User ID (Disabled)
        formGbc.gridy = row++;
        formGbc.gridx = 0;
        formCard.add(createLabel("User ID:"), formGbc);
        formGbc.gridx = 1;
        userIdField = new JTextField();
        userIdField.setEditable(false);
        ThemeManager.styleTextField(userIdField);
        userIdField.setBackground(CineBookTheme.BG_HEADER);
        formCard.add(userIdField, formGbc);

        // Username
        formGbc.gridy = row++;
        formGbc.gridx = 0;
        formCard.add(createLabel("Username: *"), formGbc);
        formGbc.gridx = 1;
        usernameField = new JTextField();
        ThemeManager.styleTextField(usernameField);
        formCard.add(usernameField, formGbc);

        // Role Dropdown
        formGbc.gridy = row++;
        formGbc.gridx = 0;
        formCard.add(createLabel("System Role: *"), formGbc);
        formGbc.gridx = 1;
        roleCombo = new JComboBox<>(new String[]{"CUSTOMER", "ADMIN"});
        ThemeManager.styleComboBox(roleCombo);
        roleCombo.setPreferredSize(new Dimension(180, 34));
        formCard.add(roleCombo, formGbc);

        // Note about passwords
        formGbc.gridy = row++;
        formGbc.gridx = 0;
        formGbc.gridwidth = 2;
        JLabel securityNote = new JLabel("<html><font color='#94A3B8'><i>* Passwords are securely hashed and omitted from the management view for security compliance.</i></font></html>");
        securityNote.setFont(ThemeManager.getSmallFont());
        formCard.add(securityNote, formGbc);

        // Action Buttons inside Form
        formGbc.gridy = row++;
        formGbc.gridx = 0;
        formGbc.gridwidth = 2;
        formGbc.insets = new Insets(18, 8, 8, 8);
        JPanel formActions = new JPanel(new GridLayout(2, 2, 10, 10));
        formActions.setOpaque(false);

        addButton = new JButton("Add User");
        ThemeManager.styleSuccessButton(addButton);

        updateButton = new JButton("Update Role");
        ThemeManager.stylePrimaryButton(updateButton);

        deleteButton = new JButton("Delete User");
        ThemeManager.styleDangerButton(deleteButton);

        clearButton = new JButton("Clear Form");
        ThemeManager.styleSecondaryButton(clearButton);

        formActions.add(addButton);
        formActions.add(updateButton);
        formActions.add(deleteButton);
        formActions.add(clearButton);
        formCard.add(formActions, formGbc);

        // Right Table Card
        GlassCardPanel tableCard = new GlassCardPanel(new BorderLayout());
        tableCard.setTopAccent(CineBookTheme.ACCENT_BLUE, 3);

        String[] columns = {"User ID", "Username", "System Role"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        userTable = new JTable(tableModel);
        ThemeManager.styleTable(userTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        userTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        userTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CineBookTheme.BG_CARD);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Grid positions
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 10);
        contentPanel.add(formCard, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 10, 0, 0);
        contentPanel.add(tableCard, gbc);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getLabelFont());
        l.setForeground(CineBookTheme.TEXT_SECONDARY);
        return l;
    }

    // Getters
    public JTextField getUserIdField() { return userIdField; }
    public JTextField getUsernameField() { return usernameField; }
    public JComboBox<String> getRoleCombo() { return roleCombo; }
    public JButton getAddButton() { return addButton; }
    public JButton getUpdateButton() { return updateButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getClearButton() { return clearButton; }
    public JButton getSearchButton() { return searchButton; }
    public JButton getRefreshButton() { return refreshButton; }
    public JTextField getSearchField() { return searchField; }
    public JTable getUserTable() { return userTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
}
