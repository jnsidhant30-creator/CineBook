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
 * Movie Management panel for Admin Dashboard — Pure Typographic Metadata Management (NO POSTERS).
 */
public class MovieManagementView extends JPanel {

    // Form fields
    private JTextField movieIdField;
    private JTextField titleField;
    private JTextField genreField;
    private JTextField languageField;
    private JTextField durationField;
    private JTextField ratingField;

    // Action buttons
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton searchButton;
    private JButton refreshButton;
    private JButton searchOnlineButton;  // OMDb online search
    private JButton searchTmdbButton;    // TMDB online search
    private JTextField searchField;

    // Movie table
    private JTable movieTable;
    private DefaultTableModel tableModel;

    public MovieManagementView() {
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

        JLabel titleLabel = new JLabel("Movie Catalog Management");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Add, update, or remove movie entries in the system catalog");
        subtitleLabel.setFont(ThemeManager.getBodyFont());
        subtitleLabel.setForeground(CineBookTheme.TEXT_MUTED);

        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(3));
        titleContainer.add(subtitleLabel);
        topPanel.add(titleContainer, BorderLayout.WEST);

        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchContainer.setOpaque(false);

        JLabel lblSearch = new JLabel("Search Catalog:");
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

        // TMDB: Online movie search button (PRIMARY)
        searchTmdbButton = new JButton("🌐 SEARCH TMDB");
        ThemeManager.styleButton(searchTmdbButton,
                new Color(13, 148, 136),          // accent cyan/teal
                new Color(15, 118, 110),          // hover
                com.movieticket.util.CineBookTheme.TEXT_PRIMARY);
        searchTmdbButton.setPreferredSize(new Dimension(170, 36));
        searchTmdbButton.setToolTipText("Search The Movie Database (TMDB) and import movies");
        searchContainer.add(searchTmdbButton);

        // OMDb: Online movie search button (FALLBACK)
        searchOnlineButton = new JButton("🌐 Search OMDb (Fallback)");
        ThemeManager.styleButton(searchOnlineButton,
                new Color(100, 116, 139),         // muted gray/blue
                new Color(71, 85, 105),           // hover
                com.movieticket.util.CineBookTheme.TEXT_PRIMARY);
        searchOnlineButton.setPreferredSize(new Dimension(210, 36));
        searchOnlineButton.setToolTipText("Search the OMDb database and import movies (Fallback)");
        searchContainer.add(searchOnlineButton);

        topPanel.add(searchContainer, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. Split Panel: Form (Left) & Table (Right)
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // Left: Form Card Panel
        GlassCardPanel formCard = new GlassCardPanel(new GridBagLayout());
        formCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.insets = new Insets(6, 6, 6, 6);
        formGbc.gridx = 0;

        int row = 0;
        formGbc.gridy = row++;
        formGbc.gridwidth = 2;
        JLabel formHeader = new JLabel("🎬  Movie Metadata Form");
        formHeader.setFont(ThemeManager.getCardTitleFont());
        formHeader.setForeground(CineBookTheme.TEXT_PRIMARY);
        formCard.add(formHeader, formGbc);

        formGbc.gridwidth = 1;

        // Movie ID
        formGbc.gridy = row++;
        formGbc.gridx = 0;
        formCard.add(createLabel("Movie ID:"), formGbc);
        formGbc.gridx = 1;
        movieIdField = new JTextField();
        movieIdField.setEditable(false);
        ThemeManager.styleTextField(movieIdField);
        movieIdField.setBackground(CineBookTheme.BG_HEADER);
        formCard.add(movieIdField, formGbc);

        // Title
        formGbc.gridy = row++;
        formGbc.gridx = 0;
        formCard.add(createLabel("Movie Title: *"), formGbc);
        formGbc.gridx = 1;
        titleField = new JTextField();
        ThemeManager.styleTextField(titleField);
        formCard.add(titleField, formGbc);

        // Genre
        formGbc.gridy = row++;
        formGbc.gridx = 0;
        formCard.add(createLabel("Genre:"), formGbc);
        formGbc.gridx = 1;
        genreField = new JTextField();
        ThemeManager.styleTextField(genreField);
        formCard.add(genreField, formGbc);

        // Language
        formGbc.gridy = row++;
        formGbc.gridx = 0;
        formCard.add(createLabel("Language:"), formGbc);
        formGbc.gridx = 1;
        languageField = new JTextField();
        ThemeManager.styleTextField(languageField);
        formCard.add(languageField, formGbc);

        // Duration
        formGbc.gridy = row++;
        formGbc.gridx = 0;
        formCard.add(createLabel("Duration (mins): *"), formGbc);
        formGbc.gridx = 1;
        durationField = new JTextField();
        ThemeManager.styleTextField(durationField);
        formCard.add(durationField, formGbc);

        // Rating
        formGbc.gridy = row++;
        formGbc.gridx = 0;
        formCard.add(createLabel("Rating (0.0-10.0):"), formGbc);
        formGbc.gridx = 1;
        ratingField = new JTextField();
        ThemeManager.styleTextField(ratingField);
        formCard.add(ratingField, formGbc);

        // Action Buttons Panel inside Form
        formGbc.gridy = row++;
        formGbc.gridx = 0;
        formGbc.gridwidth = 2;
        formGbc.insets = new Insets(18, 6, 6, 6);
        JPanel formActions = new JPanel(new GridLayout(2, 2, 10, 10));
        formActions.setOpaque(false);

        addButton = new JButton("Add Movie");
        ThemeManager.styleSuccessButton(addButton);

        updateButton = new JButton("Update Movie");
        ThemeManager.stylePrimaryButton(updateButton);

        deleteButton = new JButton("Delete Movie");
        ThemeManager.styleDangerButton(deleteButton);

        clearButton = new JButton("Clear Form");
        ThemeManager.styleSecondaryButton(clearButton);

        formActions.add(addButton);
        formActions.add(updateButton);
        formActions.add(deleteButton);
        formActions.add(clearButton);
        formCard.add(formActions, formGbc);

        // Right: Table panel
        GlassCardPanel tableCard = new GlassCardPanel(new BorderLayout());
        tableCard.setTopAccent(CineBookTheme.ACCENT_BLUE, 3);

        String[] columns = {"ID", "Title", "Genre", "Duration (mins)", "Language", "Rating"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        movieTable = new JTable(tableModel);
        ThemeManager.styleTable(movieTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        movieTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        movieTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        movieTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        movieTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        movieTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        movieTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        movieTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        movieTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        movieTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(movieTable);
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

    // Getters for form validation and controllers
    public JTextField getMovieIdField() { return movieIdField; }
    public JTextField getTitleField() { return titleField; }
    public JTextField getGenreField() { return genreField; }
    public JTextField getLanguageField() { return languageField; }
    public JTextField getDurationField() { return durationField; }
    public JTextField getRatingField() { return ratingField; }
    public JButton getAddButton() { return addButton; }
    public JButton getUpdateButton() { return updateButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getClearButton() { return clearButton; }
    public JButton getSearchButton() { return searchButton; }
    public JButton getRefreshButton() { return refreshButton; }
    public JButton getSearchOnlineButton() { return searchOnlineButton; }  // OMDb
    public JButton getSearchTmdbButton() { return searchTmdbButton; }      // TMDB
    public JTextField getSearchField() { return searchField; }
    public JTable getMovieTable() { return movieTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
}
