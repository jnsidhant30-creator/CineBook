package com.movieticket.view;

import com.movieticket.dao.AuditDAO;
import com.movieticket.model.AuditLog;
import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AuditLogView — Admin Security & Audit Log panel.
 * Paginated table with filters by date, user, role, action, entity type.
 * Supports CSV export. Never displays passwords or credentials.
 */
public class AuditLogView extends JPanel {

    private static final int PAGE_SIZE = 50;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuditDAO auditDAO = new AuditDAO();

    private final DefaultTableModel tableModel;
    private final JTable auditTable;

    // Filters
    private JTextField txtDate;
    private JTextField txtUser;
    private JComboBox<String> cmbRole;
    private JComboBox<String> cmbAction;
    private JComboBox<String> cmbEntity;
    private JTextField txtSearch;

    // Controls
    private JButton btnFilter;
    private JButton btnReset;
    private JButton btnLoadMore;
    private JButton btnExport;
    private JLabel lblStatus;

    private int currentOffset = 0;
    private String lastDate = "", lastUser = "", lastRole = "All", lastAction = "All", lastEntity = "All", lastSearch = "";

    public AuditLogView() {
        super(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildFilters(), BorderLayout.WEST);

        // Table
        String[] cols = {"Date / Time", "User ID", "Role", "Action", "Entity", "Description"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        auditTable = new JTable(tableModel);
        ThemeManager.styleTable(auditTable);
        auditTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        auditTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        auditTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        auditTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        auditTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        auditTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane sp = new JScrollPane(auditTable);
        sp.getViewport().setBackground(CineBookTheme.BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(CineBookTheme.BORDER_COLOR));
        add(sp, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);

        // Initial load
        loadLogs(true);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("🛡 Security Audit Log");
        title.setFont(ThemeManager.getPageTitleFont());
        title.setForeground(CineBookTheme.TEXT_PRIMARY);
        p.add(title, BorderLayout.WEST);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(ThemeManager.getSmallFont());
        lblStatus.setForeground(CineBookTheme.TEXT_MUTED);
        p.add(lblStatus, BorderLayout.EAST);
        return p;
    }

    private JPanel buildFilters() {
        GlassCardPanel card = new GlassCardPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setPreferredSize(new Dimension(220, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0); g.weightx = 1;

        // Section header
        g.gridy = 0;
        JLabel lbl = new JLabel("🔍 Filters");
        lbl.setFont(ThemeManager.getSectionHeaderFont());
        lbl.setForeground(CineBookTheme.TEXT_PRIMARY);
        card.add(lbl, g);

        g.gridy++;
        card.add(filterLabel("Date (YYYY-MM-DD):"), g);
        g.gridy++;
        txtDate = new JTextField();
        ThemeManager.styleTextField(txtDate);
        card.add(txtDate, g);

        g.gridy++;
        card.add(filterLabel("User ID:"), g);
        g.gridy++;
        txtUser = new JTextField();
        ThemeManager.styleTextField(txtUser);
        card.add(txtUser, g);

        g.gridy++;
        card.add(filterLabel("Role:"), g);
        g.gridy++;
        cmbRole = new JComboBox<>(new String[]{"All", "ADMIN", "USER"});
        ThemeManager.styleComboBox(cmbRole);
        card.add(cmbRole, g);

        g.gridy++;
        card.add(filterLabel("Action:"), g);
        g.gridy++;
        String[] actions = {"All", "LOGIN_SUCCESS", "LOGIN_FAILED", "LOGOUT",
            "USER_CREATED", "USER_UPDATED", "USER_BLOCKED", "ROLE_CHANGED",
            "MOVIE_ADDED", "MOVIE_UPDATED", "MOVIE_DELETED",
            "THEATRE_ADDED", "SHOW_CREATED", "BOOKING_CANCELLED",
            "REFUND_CREATED", "REVIEW_MODERATED", "SEAT_HELD", "SEAT_RELEASED"};
        cmbAction = new JComboBox<>(actions);
        ThemeManager.styleComboBox(cmbAction);
        card.add(cmbAction, g);

        g.gridy++;
        card.add(filterLabel("Entity Type:"), g);
        g.gridy++;
        cmbEntity = new JComboBox<>(new String[]{"All", "USER", "MOVIE", "SHOW", "BOOKING", "SEAT", "THEATRE"});
        ThemeManager.styleComboBox(cmbEntity);
        card.add(cmbEntity, g);

        g.gridy++;
        card.add(filterLabel("Search:"), g);
        g.gridy++;
        txtSearch = new JTextField();
        ThemeManager.styleTextField(txtSearch);
        card.add(txtSearch, g);

        g.gridy++;
        g.insets = new Insets(12, 0, 4, 0);
        btnFilter = new JButton("Apply Filters");
        ThemeManager.stylePrimaryButton(btnFilter);
        btnFilter.addActionListener(e -> applyFilters());
        card.add(btnFilter, g);

        g.gridy++;
        g.insets = new Insets(4, 0, 4, 0);
        btnReset = new JButton("Reset");
        ThemeManager.styleSecondaryButton(btnReset);
        btnReset.addActionListener(e -> resetFilters());
        card.add(btnReset, g);

        // Spacer
        g.gridy++;
        g.weighty = 1;
        card.add(new JPanel() {{ setOpaque(false); }}, g);

        return card;
    }

    private JPanel buildFooter() {
        JPanel fp = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fp.setOpaque(false);

        btnLoadMore = new JButton("Load More ↓");
        ThemeManager.styleSecondaryButton(btnLoadMore);
        btnLoadMore.addActionListener(e -> loadLogs(false));

        btnExport = new JButton("📥 Export CSV");
        ThemeManager.styleSecondaryButton(btnExport);
        btnExport.addActionListener(e -> exportCsv());

        fp.add(btnLoadMore);
        fp.add(btnExport);
        return fp;
    }

    private JLabel filterLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getSmallFont());
        l.setForeground(CineBookTheme.TEXT_SECONDARY);
        return l;
    }

    private void applyFilters() {
        lastDate   = txtDate.getText().trim();
        lastUser   = txtUser.getText().trim();
        lastRole   = (String) cmbRole.getSelectedItem();
        lastAction = (String) cmbAction.getSelectedItem();
        lastEntity = (String) cmbEntity.getSelectedItem();
        lastSearch = txtSearch.getText().trim();
        currentOffset = 0;
        loadLogs(true);
    }

    private void resetFilters() {
        txtDate.setText(""); txtUser.setText(""); txtSearch.setText("");
        cmbRole.setSelectedIndex(0); cmbAction.setSelectedIndex(0); cmbEntity.setSelectedIndex(0);
        lastDate = ""; lastUser = ""; lastRole = "All"; lastAction = "All"; lastEntity = "All"; lastSearch = "";
        currentOffset = 0;
        loadLogs(true);
    }

    private void loadLogs(boolean clear) {
        lblStatus.setText("Loading...");
        new SwingWorker<List<AuditLog>, Void>() {
            @Override
            protected List<AuditLog> doInBackground() {
                return auditDAO.getFilteredLogs(lastDate, lastUser, lastRole, lastAction, lastEntity, lastSearch, PAGE_SIZE, currentOffset);
            }
            @Override
            protected void done() {
                try {
                    List<AuditLog> logs = get();
                    if (clear) tableModel.setRowCount(0);
                    for (AuditLog log : logs) {
                        tableModel.addRow(new Object[]{
                            log.getCreatedAt() != null ? log.getCreatedAt().format(DT_FMT) : "",
                            log.getUserId() > 0 ? log.getUserId() : "–",
                            log.getRole() != null ? log.getRole() : "–",
                            log.getAction(),
                            log.getEntityType() != null ? log.getEntityType() : "–",
                            log.getDescription()
                        });
                    }
                    currentOffset += logs.size();
                    lblStatus.setText("Showing " + tableModel.getRowCount() + " entries");
                    btnLoadMore.setEnabled(!logs.isEmpty() && logs.size() == PAGE_SIZE);
                } catch (Exception e) {
                    lblStatus.setText("Error loading logs.");
                }
            }
        }.execute();
    }

    private void exportCsv() {
        lblStatus.setText("Exporting...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                List<AuditLog> all = auditDAO.getFilteredLogs(
                    lastDate, lastUser, lastRole, lastAction, lastEntity, lastSearch, 10000, 0);
                return auditDAO.exportToCsv(all);
            }
            @Override
            protected void done() {
                try {
                    String path = get();
                    if (path != null) {
                        lblStatus.setText("Exported to: " + path);
                        JOptionPane.showMessageDialog(AuditLogView.this, "Audit log exported to:\n" + path, "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        lblStatus.setText("Export failed.");
                    }
                } catch (Exception e) {
                    lblStatus.setText("Export error.");
                }
            }
        }.execute();
    }

    public void refresh() {
        currentOffset = 0;
        loadLogs(true);
    }
}
