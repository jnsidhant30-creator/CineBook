package com.movieticket.view;

import com.movieticket.controller.TmdbController;
import com.movieticket.model.TmdbMovie;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ImageLoader;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TmdbSearchAdminDialog.java — Admin-facing modal dialog for TMDB search and import.
 */
public class TmdbSearchAdminDialog extends JDialog {

    private JTextField tmdbSearchField;
    private JButton tmdbSearchButton;
    private JLabel statusLabel;
    private JTable resultsTable;
    private DefaultTableModel resultsTableModel;
    private JButton importButton;
    private JButton closeButton;

    private final List<ImageIcon> posterCache = new ArrayList<>();
    private final TmdbController tmdbController;
    private List<TmdbMovie> currentResults = new ArrayList<>();
    private final Runnable onImportSuccess;

    private final ExecutorService posterExecutor = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "tmdb-poster-loader");
        t.setDaemon(true);
        return t;
    });

    public TmdbSearchAdminDialog(Frame owner, TmdbController tmdbController, Runnable onImportSuccess) {
        super(owner, "🎬 Search Movies (TMDB)", true);
        this.tmdbController = tmdbController;
        this.onImportSuccess = onImportSuccess;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int width = Math.min(900, maxBounds.width - 40);
        int height = Math.min(650, maxBounds.height - 40);
        setSize(width, height);
        setMinimumSize(new Dimension(700, 480));
        setLocationRelativeTo(owner);

        initComponents();
        getContentPane().setBackground(CineBookTheme.BG_PRIMARY);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(CineBookTheme.BG_PRIMARY);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        root.add(buildTopPanel(), BorderLayout.NORTH);
        root.add(buildCenterPanel(), BorderLayout.CENTER);
        root.add(buildBottomPanel(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 8));
        topPanel.setOpaque(false);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel titleLabel = new JLabel("Import from TMDB");
        titleLabel.setFont(ThemeManager.getSectionHeaderFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        JLabel subLabel = new JLabel("Search The Movie Database (TMDB) to add or update movies in CineBook.");
        subLabel.setFont(ThemeManager.getBodyFont());
        subLabel.setForeground(CineBookTheme.TEXT_MUTED);
        
        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        titleStack.add(titleLabel);
        titleStack.add(Box.createVerticalStrut(2));
        titleStack.add(subLabel);
        headerRow.add(titleStack, BorderLayout.WEST);
        topPanel.add(headerRow, BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        searchRow.setOpaque(false);

        JLabel searchLbl = new JLabel("Movie Title:");
        searchLbl.setFont(ThemeManager.getLabelFont());
        searchLbl.setForeground(CineBookTheme.TEXT_SECONDARY);
        searchRow.add(searchLbl);

        tmdbSearchField = new JTextField();
        ThemeManager.styleTextField(tmdbSearchField);
        tmdbSearchField.setPreferredSize(new Dimension(280, 36));
        searchRow.add(tmdbSearchField);

        tmdbSearchButton = new JButton("🔍  Search TMDB");
        ThemeManager.stylePrimaryButton(tmdbSearchButton);
        tmdbSearchButton.setPreferredSize(new Dimension(160, 36));
        searchRow.add(tmdbSearchButton);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(ThemeManager.getSmallFont());
        statusLabel.setForeground(CineBookTheme.ACCENT_CYAN);
        searchRow.add(statusLabel);

        topPanel.add(searchRow, BorderLayout.SOUTH);

        tmdbSearchField.addActionListener(e -> doSearch());
        tmdbSearchButton.addActionListener(e -> doSearch());

        return topPanel;
    }

    private GlassCardPanel buildCenterPanel() {
        GlassCardPanel card = new GlassCardPanel(new BorderLayout(0, 0));
        card.setTopAccent(CineBookTheme.ACCENT_CYAN, 3);

        String[] cols = {"Poster", "Title", "Year", "TMDB ID", "TMDB Rating", "Genres"};
        resultsTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 0 ? ImageIcon.class : String.class;
            }
        };

        resultsTable = new JTable(resultsTableModel);
        ThemeManager.styleTable(resultsTable);
        resultsTable.setRowHeight(80);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        resultsTable.getColumnModel().getColumn(0).setMaxWidth(80);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(150);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        resultsTable.getColumnModel().getColumn(2).setCellRenderer(center);
        resultsTable.getColumnModel().getColumn(3).setCellRenderer(center);
        resultsTable.getColumnModel().getColumn(4).setCellRenderer(center);

        resultsTable.getColumnModel().getColumn(0).setCellRenderer(new PosterCellRenderer());

        JScrollPane sp = new JScrollPane(resultsTable);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CineBookTheme.BG_CARD);
        card.add(sp, BorderLayout.CENTER);

        resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) doImport();
            }
        });

        return card;
    }

    private JPanel buildBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        
        JLabel attribution = new JLabel(" This product uses the TMDB API but is not endorsed or certified by TMDB.");
        attribution.setFont(ThemeManager.getSmallFont());
        attribution.setForeground(CineBookTheme.TEXT_MUTED);
        bottomPanel.add(attribution, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        buttonPanel.setOpaque(false);

        importButton = new JButton("⬇  Import Selected Movie");
        ThemeManager.styleSuccessButton(importButton);
        importButton.setPreferredSize(new Dimension(200, 38));
        importButton.setEnabled(false);
        importButton.addActionListener(e -> doImport());

        closeButton = new JButton("Close");
        ThemeManager.styleSecondaryButton(closeButton);
        closeButton.setPreferredSize(new Dimension(100, 38));
        closeButton.addActionListener(e -> {
            posterExecutor.shutdownNow();
            dispose();
        });

        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                importButton.setEnabled(resultsTable.getSelectedRow() >= 0);
            }
        });

        buttonPanel.add(importButton);
        buttonPanel.add(closeButton);
        
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        return bottomPanel;
    }

    private void doSearch() {
        String query = tmdbSearchField.getText().trim();
        if (query.isEmpty()) {
            showStatus("⚠ Please enter a movie title.", CineBookTheme.WARNING_COLOR);
            return;
        }

        setSearching(true);
        clearResults();
        showStatus("Searching TMDB for \"" + query + "\"…", CineBookTheme.ACCENT_CYAN);

        tmdbController.searchMovies(query,
                results -> {
                    currentResults = results;
                    populateResults(results);
                    if (results.isEmpty()) {
                        showStatus("No movies found for \"" + query + "\".", CineBookTheme.ACCENT_GOLD);
                    } else {
                        showStatus("Found " + results.size() + " result(s). Select a movie to import.", CineBookTheme.SUCCESS_COLOR);
                    }
                },
                error -> showStatus("⚠ " + error, CineBookTheme.DANGER_COLOR),
                () -> setSearching(false)
        );
    }

    private void doImport() {
        int row = resultsTable.getSelectedRow();
        if (row < 0 || row >= currentResults.size()) return;

        TmdbMovie selected = currentResults.get(row);
        setImporting(true);
        showStatus("Fetching full details and saving to database…", CineBookTheme.ACCENT_CYAN);

        tmdbController.importMovie(selected,
                successMsg -> {
                    showStatus("Success: " + successMsg, CineBookTheme.SUCCESS_COLOR);
                    JOptionPane.showMessageDialog(this, successMsg, "Import Successful", JOptionPane.INFORMATION_MESSAGE);
                    if (onImportSuccess != null) onImportSuccess.run();
                },
                errorMsg -> {
                    showStatus("Error: " + errorMsg, CineBookTheme.DANGER_COLOR);
                    JOptionPane.showMessageDialog(this, errorMsg, "Import Failed", JOptionPane.ERROR_MESSAGE);
                },
                () -> setImporting(false)
        );
    }

    private void populateResults(List<TmdbMovie> movies) {
        resultsTableModel.setRowCount(0);
        posterCache.clear();

        for (int i = 0; i < movies.size(); i++) {
            TmdbMovie m = movies.get(i);
            posterCache.add(null);
            
            String rating = m.getVoteAverage() > 0 ? String.format("%.1f", m.getVoteAverage()) : "N/A";
            String genres = m.getGenres() != null && !m.getGenres().isEmpty() 
                            ? String.join(", ", m.getGenres()) 
                            : "N/A";
                            
            resultsTableModel.addRow(new Object[]{
                    null,
                    m.getTitle() != null ? m.getTitle() : "Unknown",
                    m.getYear(),
                    String.valueOf(m.getTmdbId()),
                    rating,
                    genres
            });
        }

        for (int i = 0; i < movies.size(); i++) {
            final int rowIdx = i;
            final String posterPath = movies.get(i).getPosterPath();
            if (posterPath != null && !posterPath.isBlank()) {
                posterExecutor.submit(() -> loadPosterAsync(rowIdx, posterPath));
            }
        }
    }

    private void clearResults() {
        resultsTableModel.setRowCount(0);
        posterCache.clear();
        currentResults.clear();
        importButton.setEnabled(false);
    }

    private void loadPosterAsync(int rowIdx, String posterPath) {
        try {
            Image img = ImageLoader.loadTmdbImage(posterPath, "w92");
            if (img != null) {
                Image scaled = img.getScaledInstance(55, 75, Image.SCALE_SMOOTH);
                ImageIcon thumb = new ImageIcon(scaled);

                SwingUtilities.invokeLater(() -> {
                    if (rowIdx < posterCache.size()) {
                        posterCache.set(rowIdx, thumb);
                        if (rowIdx < resultsTableModel.getRowCount()) {
                            resultsTableModel.setValueAt(thumb, rowIdx, 0);
                        }
                    }
                });
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void setSearching(boolean active) {
        tmdbSearchButton.setEnabled(!active);
        tmdbSearchButton.setText(active ? "Searching…" : "🔍  Search TMDB");
        tmdbSearchField.setEnabled(!active);
    }

    private void setImporting(boolean active) {
        importButton.setEnabled(!active);
        importButton.setText(active ? "Importing…" : "⬇  Import Selected Movie");
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private static class PosterCellRenderer extends DefaultTableCellRenderer {
        private static final ImageIcon PLACEHOLDER = createPlaceholder();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel lbl = new JLabel();
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setVerticalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBackground(isSelected ? CineBookTheme.ACCENT_PURPLE : CineBookTheme.BG_CARD);

            if (value instanceof ImageIcon icon) {
                lbl.setIcon(icon);
            } else {
                lbl.setIcon(PLACEHOLDER);
            }
            return lbl;
        }

        private static ImageIcon createPlaceholder() {
            BufferedImage img = new BufferedImage(55, 75, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(30, 45, 70));
            g.fillRoundRect(0, 0, 54, 74, 8, 8);
            g.setColor(new Color(80, 100, 140));
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(0, 0, 54, 74, 8, 8);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            g.setColor(new Color(100, 120, 160));
            FontMetrics fm = g.getFontMetrics();
            String emoji = "🎬";
            g.drawString(emoji, (54 - fm.stringWidth(emoji)) / 2, 44);
            g.dispose();
            return new ImageIcon(img);
        }
    }
}
