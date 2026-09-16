package com.movieticket.view;

import com.movieticket.controller.OmdbController;
import com.movieticket.model.OmdbMovie;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * OmdbSearchDialog.java — Modal dialog for online OMDb movie search and import.
 *
 * Launched from the Admin Movie Management screen via the "Search Online Movies" button.
 * Uses the existing CineBook theme (CineBookTheme, ThemeManager, GlassCardPanel).
 * All network calls are executed on a background thread (SwingWorker / executor).
 * The EDT is never blocked.
 *
 * Layout:
 *  [TOP]    Search field  |  "Search" button  |  Status label
 *  [CENTER] Results table  (Poster | Title | Year | IMDb ID | Rating)
 *  [BOTTOM] "Import Selected Movie" button  |  "Close" button
 */
public class OmdbSearchDialog extends JDialog {

    // -------------------------------------------------------------------------
    // UI Components
    // -------------------------------------------------------------------------
    private JTextField          omdbSearchField;
    private JButton             omdbSearchButton;
    private JLabel              statusLabel;
    private JTable              resultsTable;
    private DefaultTableModel   resultsTableModel;
    private JButton             importButton;
    private JButton             closeButton;

    // Poster image cache: row index → ImageIcon
    private final List<ImageIcon> posterCache = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Controller & Callback
    // -------------------------------------------------------------------------
    private final OmdbController omdbController;
    private List<OmdbMovie>      currentResults = new ArrayList<>();

    /** Called when a movie is successfully imported, so the parent view can refresh. */
    private final Runnable onImportSuccess;

    // Thread pool for async poster loading
    private final ExecutorService posterExecutor = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "omdb-poster-loader");
        t.setDaemon(true);
        return t;
    });

    // =========================================================================
    // Constructor
    // =========================================================================

    public OmdbSearchDialog(Frame owner, OmdbController omdbController, Runnable onImportSuccess) {
        super(owner, "🌐 Search Online Movies — OMDb", true);
        this.omdbController  = omdbController;
        this.onImportSuccess = onImportSuccess;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int width = Math.min(900, maxBounds.width - 40);
        int height = Math.min(620, maxBounds.height - 40);
        setSize(width, height);
        setMinimumSize(new Dimension(700, 480));
        setLocationRelativeTo(owner);

        initComponents();
        applyTheme();
    }

    // =========================================================================
    // UI Build
    // =========================================================================

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(CineBookTheme.BG_PRIMARY);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        // ----- TOP: Header + Search bar -----
        root.add(buildTopPanel(), BorderLayout.NORTH);

        // ----- CENTER: Results table -----
        root.add(buildCenterPanel(), BorderLayout.CENTER);

        // ----- BOTTOM: Action buttons -----
        root.add(buildBottomPanel(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 8));
        topPanel.setOpaque(false);

        // Title
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel titleLabel = new JLabel("Search Online Movies");
        titleLabel.setFont(ThemeManager.getSectionHeaderFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        JLabel subLabel = new JLabel("Search OMDb and import movies into CineBook");
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

        // Search Row
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        searchRow.setOpaque(false);

        JLabel searchLbl = new JLabel("Movie Title:");
        searchLbl.setFont(ThemeManager.getLabelFont());
        searchLbl.setForeground(CineBookTheme.TEXT_SECONDARY);
        searchRow.add(searchLbl);

        omdbSearchField = new JTextField();
        ThemeManager.styleTextField(omdbSearchField);
        omdbSearchField.setPreferredSize(new Dimension(280, 36));
        omdbSearchField.setToolTipText("Enter a movie title (e.g. Avatar, Inception)");
        searchRow.add(omdbSearchField);

        omdbSearchButton = new JButton("🔍  Search OMDb");
        ThemeManager.stylePrimaryButton(omdbSearchButton);
        omdbSearchButton.setPreferredSize(new Dimension(150, 36));
        searchRow.add(omdbSearchButton);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(ThemeManager.getSmallFont());
        statusLabel.setForeground(CineBookTheme.ACCENT_CYAN);
        searchRow.add(statusLabel);

        topPanel.add(searchRow, BorderLayout.SOUTH);

        // Enter key triggers search
        omdbSearchField.addActionListener(e -> doSearch());
        omdbSearchButton.addActionListener(e -> doSearch());

        return topPanel;
    }

    private GlassCardPanel buildCenterPanel() {
        GlassCardPanel card = new GlassCardPanel(new BorderLayout(0, 0));
        card.setTopAccent(CineBookTheme.ACCENT_BLUE, 3);

        String[] cols = {"Poster", "Title", "Year", "IMDb ID", "IMDb Rating"};
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
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        // Column widths
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        resultsTable.getColumnModel().getColumn(0).setMaxWidth(80);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(320);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        // Center columns 2-4
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c = 2; c <= 4; c++) {
            resultsTable.getColumnModel().getColumn(c).setCellRenderer(center);
        }

        // Poster column renderer
        resultsTable.getColumnModel().getColumn(0).setCellRenderer(new PosterCellRenderer());

        JScrollPane sp = new JScrollPane(resultsTable);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CineBookTheme.BG_CARD);
        card.add(sp, BorderLayout.CENTER);

        // Double-click to import
        resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) doImport();
            }
        });

        return card;
    }

    private JPanel buildBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        bottomPanel.setOpaque(false);

        importButton = new JButton("⬇  Import Selected Movie");
        ThemeManager.styleSuccessButton(importButton);
        importButton.setPreferredSize(new Dimension(200, 38));
        importButton.setEnabled(false);
        importButton.setToolTipText("Select a movie from the results and click to import it into CineBook");
        importButton.addActionListener(e -> doImport());

        closeButton = new JButton("Close");
        ThemeManager.styleSecondaryButton(closeButton);
        closeButton.setPreferredSize(new Dimension(100, 38));
        closeButton.addActionListener(e -> {
            posterExecutor.shutdownNow();
            dispose();
        });

        // Enable import when a row is selected
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                importButton.setEnabled(resultsTable.getSelectedRow() >= 0);
            }
        });

        bottomPanel.add(importButton);
        bottomPanel.add(closeButton);
        return bottomPanel;
    }

    private void applyTheme() {
        getContentPane().setBackground(CineBookTheme.BG_PRIMARY);
    }

    // =========================================================================
    // Actions
    // =========================================================================

    /** Executes an OMDb search in a background SwingWorker. */
    private void doSearch() {
        String query = omdbSearchField.getText().trim();
        if (query.isEmpty()) {
            showStatus("⚠ Please enter a movie title to search.", CineBookTheme.WARNING_COLOR);
            return;
        }

        setSearching(true);
        clearResults();
        showStatus("Searching OMDb for \"" + query + "\"…", CineBookTheme.ACCENT_CYAN);

        omdbController.searchOnline(query,
                results -> {
                    // onSuccess — on EDT
                    currentResults = results;
                    populateResults(results);
                    if (results.isEmpty()) {
                        showStatus("No movies found for \"" + query + "\". Try a different title.", CineBookTheme.ACCENT_GOLD);
                    } else {
                        showStatus("Found " + results.size() + " result(s). Select a movie and click Import.", CineBookTheme.SUCCESS_COLOR);
                    }
                },
                error -> {
                    // onError — on EDT
                    showStatus("⚠ " + error, CineBookTheme.DANGER_COLOR);
                },
                () -> setSearching(false)   // onDone — on EDT
        );
    }

    /** Imports the selected movie via the controller. */
    private void doImport() {
        int row = resultsTable.getSelectedRow();
        if (row < 0 || row >= currentResults.size()) {
            showStatus("Please select a movie from the results first.", CineBookTheme.WARNING_COLOR);
            return;
        }

        OmdbMovie selected = currentResults.get(row);
        if (selected.getImdbId() == null) {
            showStatus("Selected movie has no IMDb ID — cannot import.", CineBookTheme.DANGER_COLOR);
            return;
        }

        setImporting(true);
        showStatus("Fetching full details and saving to database…", CineBookTheme.ACCENT_CYAN);

        omdbController.importMovie(selected,
                successMsg -> {
                    // onSuccess
                    showStatus("Success: " + successMsg, CineBookTheme.SUCCESS_COLOR);
                    JOptionPane.showMessageDialog(this, successMsg, "Import Successful", JOptionPane.INFORMATION_MESSAGE);
                    if (onImportSuccess != null) onImportSuccess.run();
                },
                errorMsg -> {
                    // onError
                    showStatus("Error: " + errorMsg, CineBookTheme.DANGER_COLOR);
                    JOptionPane.showMessageDialog(this, errorMsg, "Import Failed", JOptionPane.ERROR_MESSAGE);
                },
                () -> setImporting(false)    // onDone
        );
    }

    // =========================================================================
    // Table Helpers
    // =========================================================================

    private void populateResults(List<OmdbMovie> movies) {
        resultsTableModel.setRowCount(0);
        posterCache.clear();

        for (int i = 0; i < movies.size(); i++) {
            OmdbMovie m = movies.get(i);
            String rating = m.getImdbRating() != null ? m.getImdbRating() : "N/A";
            posterCache.add(null); // placeholder
            resultsTableModel.addRow(new Object[]{
                    null,                                            // poster placeholder
                    m.getTitle() != null ? m.getTitle() : "Unknown",
                    m.getYear()  != null ? m.getYear()  : "—",
                    m.getImdbId() != null ? m.getImdbId() : "—",
                    rating
            });
        }

        // Load posters asynchronously
        for (int i = 0; i < movies.size(); i++) {
            final int rowIdx = i;
            final String posterUrl = movies.get(i).getPosterUrl();
            if (posterUrl != null) {
                posterExecutor.submit(() -> loadPosterAsync(rowIdx, posterUrl));
            }
        }
    }

    private void clearResults() {
        resultsTableModel.setRowCount(0);
        posterCache.clear();
        currentResults = new ArrayList<>();
        importButton.setEnabled(false);
    }

    /** Loads a poster image asynchronously and updates the table cell on the EDT. */
    private void loadPosterAsync(int rowIdx, String posterUrl) {
        try {
            URL url = URI.create(posterUrl).toURL();
            ImageIcon icon = new ImageIcon(url);
            // Scale to thumbnail
            Image scaled = icon.getImage().getScaledInstance(55, 75, Image.SCALE_SMOOTH);
            ImageIcon thumb = new ImageIcon(scaled);

            SwingUtilities.invokeLater(() -> {
                if (rowIdx < posterCache.size()) {
                    posterCache.set(rowIdx, thumb);
                    if (rowIdx < resultsTableModel.getRowCount()) {
                        resultsTableModel.setValueAt(thumb, rowIdx, 0);
                    }
                }
            });
        } catch (Exception e) {
            // Poster unavailable — leave placeholder (handled by renderer)
        }
    }

    // =========================================================================
    // State Helpers
    // =========================================================================

    private void setSearching(boolean active) {
        omdbSearchButton.setEnabled(!active);
        omdbSearchButton.setText(active ? "Searching…" : "🔍  Search OMDb");
        omdbSearchField.setEnabled(!active);
    }

    private void setImporting(boolean active) {
        importButton.setEnabled(!active);
        importButton.setText(active ? "Importing…" : "⬇  Import Selected Movie");
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    // =========================================================================
    // Poster Cell Renderer
    // =========================================================================

    /**
     * Renders the poster column: shows the thumbnail if loaded, or a placeholder icon otherwise.
     */
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
            g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g.setColor(new Color(80, 100, 140));
            String msg = "Loading";
            g.drawString(msg, (54 - fm.stringWidth(msg)) / 2 + 2, 62);
            g.dispose();
            return new ImageIcon(img);
        }
    }
}
