package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Show Selection screen — Premium Cinema Showtime Portal.
 * Two column layout: Filters on left, Showtimes grouped by Cinema on right.
 */
public class ShowSelectionView extends JPanel {

    private JLabel movieTitleLabel;
    
    // Filters
    private JComboBox<String> cityCombo;
    private JComboBox<String> theatreCombo;
    private JComboBox<String> dateCombo;
    
    // Shows Container
    private JPanel showsContainer;
    
    // Navigation
    private JButton backButton;

    public ShowSelectionView() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBackground(CineBookTheme.BG_PRIMARY);

        initComponents();
    }

    private void initComponents() {
        // --- Top Header ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(20, 30, 10, 30));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        
        movieTitleLabel = new JLabel("Movie Title");
        movieTitleLabel.setFont(ThemeManager.getFont(Font.BOLD, 36));
        movieTitleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        
        JLabel subtitle = new JLabel("Select a cinema and showtime to book tickets");
        subtitle.setFont(ThemeManager.getFont(Font.PLAIN, 16));
        subtitle.setForeground(CineBookTheme.TEXT_MUTED);
        
        titleBox.add(movieTitleLabel);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);
        
        topPanel.add(titleBox, BorderLayout.WEST);
        
        backButton = new JButton(" Back to Movie Details");
        backButton.setIcon(FontIcon.of(FontAwesomeSolid.ARROW_LEFT, 14, CineBookTheme.TEXT_PRIMARY));
        ThemeManager.styleSecondaryButton(backButton);
        topPanel.add(backButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        // --- Main Content (Two Column Layout) ---
        JPanel mainContent = new JPanel(new BorderLayout(30, 0));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(10, 30, 20, 30));

        // LEFT COLUMN: Filters (30% width)
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setOpaque(false);
        filterPanel.setPreferredSize(new Dimension(300, 0));
        
        GlassCardPanel filterCard = new GlassCardPanel();
        filterCard.setLayout(new BoxLayout(filterCard, BoxLayout.Y_AXIS));
        filterCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 3);
        filterCard.setBorder(new EmptyBorder(24, 24, 24, 24));
        
        JLabel filterTitle = new JLabel("Filters");
        filterTitle.setFont(ThemeManager.getFont(Font.BOLD, 20));
        filterTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        filterCard.add(filterTitle);
        filterCard.add(Box.createVerticalStrut(20));
        
        // City Filter
        JLabel lblCity = new JLabel("City");
        lblCity.setFont(ThemeManager.getLabelFont());
        lblCity.setForeground(CineBookTheme.TEXT_MUTED);
        cityCombo = new JComboBox<>();
        ThemeManager.styleComboBox(cityCombo);
        cityCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        filterCard.add(lblCity);
        filterCard.add(Box.createVerticalStrut(6));
        filterCard.add(cityCombo);
        filterCard.add(Box.createVerticalStrut(16));
        
        // Date Filter
        JLabel lblDt = new JLabel("Date");
        lblDt.setFont(ThemeManager.getLabelFont());
        lblDt.setForeground(CineBookTheme.TEXT_MUTED);
        dateCombo = new JComboBox<>();
        ThemeManager.styleComboBox(dateCombo);
        dateCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        filterCard.add(lblDt);
        filterCard.add(Box.createVerticalStrut(6));
        filterCard.add(dateCombo);
        filterCard.add(Box.createVerticalStrut(16));

        // Cinema Filter
        JLabel lblTh = new JLabel("Cinema");
        lblTh.setFont(ThemeManager.getLabelFont());
        lblTh.setForeground(CineBookTheme.TEXT_MUTED);
        theatreCombo = new JComboBox<>();
        ThemeManager.styleComboBox(theatreCombo);
        theatreCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        filterCard.add(lblTh);
        filterCard.add(Box.createVerticalStrut(6));
        filterCard.add(theatreCombo);
        
        filterPanel.add(filterCard, BorderLayout.NORTH);
        mainContent.add(filterPanel, BorderLayout.WEST);

        // RIGHT COLUMN: Cinema Cards + Showtimes
        showsContainer = new JPanel();
        showsContainer.setLayout(new BoxLayout(showsContainer, BoxLayout.Y_AXIS));
        showsContainer.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(showsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        mainContent.add(scrollPane, BorderLayout.CENTER);
        
        add(mainContent, BorderLayout.CENTER);
    }

    // Getters for controller connectivity
    public JLabel getMovieTitleLabel() { return movieTitleLabel; }
    public JComboBox<String> getCityCombo() { return cityCombo; }
    public JComboBox<String> getTheatreCombo() { return theatreCombo; }
    public JComboBox<String> getDateCombo() { return dateCombo; }
    public JPanel getShowsContainer() { return showsContainer; }
    public JButton getBackButton() { return backButton; }
}
