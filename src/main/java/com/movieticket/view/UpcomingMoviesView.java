package com.movieticket.view;

import com.movieticket.model.UpcomingMovie;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;
import com.movieticket.view.components.RoundedButton;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UpcomingMoviesView extends JPanel {

    private JButton checkNewMoviesBtn;
    private JLabel lastCheckLabel;
    private JLabel nextCheckLabel;

    private JTable upcomingTable;
    private DefaultTableModel upcomingTableModel;
    private JButton viewDetailsBtn;
    private JButton addToCineBookBtn;
    private JButton ignoreBtn;

    private JTable recentTable;
    private DefaultTableModel recentTableModel;

    public UpcomingMoviesView() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel titleLbl = new JLabel("Movie Discovery & Monitoring");
        titleLbl.setFont(ThemeManager.getHeadingFont(Font.BOLD, 24));
        titleLbl.setForeground(CineBookTheme.TEXT_PRIMARY);

        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        checkPanel.setOpaque(false);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setOpaque(false);
        lastCheckLabel = new JLabel("Last Check: Never");
        lastCheckLabel.setForeground(CineBookTheme.TEXT_MUTED);
        nextCheckLabel = new JLabel("Next Auto Check: N/A");
        nextCheckLabel.setForeground(CineBookTheme.TEXT_MUTED);
        statusPanel.add(lastCheckLabel);
        statusPanel.add(nextCheckLabel);

        checkNewMoviesBtn = new RoundedButton("Check for New Movies");
        checkNewMoviesBtn.setIcon(FontIcon.of(FontAwesomeSolid.SYNC_ALT, 14, CineBookTheme.TEXT_PRIMARY));
        ThemeManager.stylePrimaryButton(checkNewMoviesBtn);

        checkPanel.add(statusPanel);
        checkPanel.add(checkNewMoviesBtn);

        topBar.add(titleLbl, BorderLayout.WEST);
        topBar.add(checkPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Center Tabs for Upcoming / Recent
        JTabbedPane tabbedPane = new JTabbedPane();

        // Upcoming Tab
        JPanel upcomingPanel = createUpcomingPanel();
        tabbedPane.addTab("Upcoming Discovered", upcomingPanel);

        // Recent Tab
        JPanel recentPanel = createRecentPanel();
        tabbedPane.addTab("Recent Releases (Last 2 Months)", recentPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createUpcomingPanel() {
        GlassCardPanel panel = new GlassCardPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"ID", "Title", "Release Date", "IMDb ID", "Genre", "Status", "Source"};
        upcomingTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        upcomingTable = new JTable(upcomingTableModel);
        ThemeManager.styleTable(upcomingTable);
        
        // Hide ID column
        upcomingTable.getColumnModel().getColumn(0).setMinWidth(0);
        upcomingTable.getColumnModel().getColumn(0).setMaxWidth(0);
        upcomingTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scroll = new JScrollPane(upcomingTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CineBookTheme.BG_PRIMARY);
        panel.add(scroll, BorderLayout.CENTER);

        // Action Buttons Bottom
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        viewDetailsBtn = new RoundedButton("View Details");
        ThemeManager.styleSecondaryButton(viewDetailsBtn);

        ignoreBtn = new RoundedButton("Ignore");
        ThemeManager.styleSecondaryButton(ignoreBtn);

        addToCineBookBtn = new RoundedButton("Add to CineBook");
        ThemeManager.stylePrimaryButton(addToCineBookBtn);

        actionPanel.add(viewDetailsBtn);
        actionPanel.add(ignoreBtn);
        actionPanel.add(addToCineBookBtn);

        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRecentPanel() {
        GlassCardPanel panel = new GlassCardPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"ID", "Title", "Release Date", "Genre", "Language", "Rating"};
        recentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recentTable = new JTable(recentTableModel);
        ThemeManager.styleTable(recentTable);
        
        // Hide ID column
        recentTable.getColumnModel().getColumn(0).setMinWidth(0);
        recentTable.getColumnModel().getColumn(0).setMaxWidth(0);
        recentTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scroll = new JScrollPane(recentTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CineBookTheme.BG_PRIMARY);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    public void populateUpcomingTable(List<UpcomingMovie> movies) {
        upcomingTableModel.setRowCount(0);
        if (movies != null) {
            for (UpcomingMovie m : movies) {
                upcomingTableModel.addRow(new Object[]{
                        m.getId(),
                        m.getTitle(),
                        m.getReleaseDate() != null ? m.getReleaseDate().toString() : "N/A",
                        m.getImdbId() != null ? m.getImdbId() : "N/A",
                        m.getGenre() != null ? m.getGenre() : "N/A",
                        m.getStatus(),
                        m.getSource()
                });
            }
        }
    }
    
    public void populateRecentTable(List<com.movieticket.model.Movie> movies) {
        recentTableModel.setRowCount(0);
        if (movies != null) {
            for (com.movieticket.model.Movie m : movies) {
                recentTableModel.addRow(new Object[]{
                        m.getMovieId(),
                        m.getTitle(),
                        m.getReleaseDate() != null ? m.getReleaseDate().toString() : "N/A",
                        m.getGenre() != null ? m.getGenre() : "N/A",
                        m.getLanguage() != null ? m.getLanguage() : "N/A",
                        m.getRating()
                });
            }
        }
    }

    public UpcomingMovie getSelectedUpcomingMovie(List<UpcomingMovie> currentList) {
        int row = upcomingTable.getSelectedRow();
        if (row >= 0 && row < currentList.size()) {
            // Because table might be sorted if RowSorter is added, map index
            int modelRow = upcomingTable.convertRowIndexToModel(row);
            int id = (int) upcomingTableModel.getValueAt(modelRow, 0);
            return currentList.stream().filter(m -> m.getId() == id).findFirst().orElse(null);
        }
        return null;
    }

    public JButton getCheckNewMoviesBtn() { return checkNewMoviesBtn; }
    public JButton getViewDetailsBtn() { return viewDetailsBtn; }
    public JButton getAddToCineBookBtn() { return addToCineBookBtn; }
    public JButton getIgnoreBtn() { return ignoreBtn; }
    public JLabel getLastCheckLabel() { return lastCheckLabel; }
    public JLabel getNextCheckLabel() { return nextCheckLabel; }
    public JTable getUpcomingTable() { return upcomingTable; }
}
