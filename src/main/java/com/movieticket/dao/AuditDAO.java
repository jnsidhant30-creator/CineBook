package com.movieticket.dao;

import com.movieticket.model.AuditLog;
import com.movieticket.util.DatabaseConnection;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AuditDAO — Persistent security audit log storage with paginated retrieval.
 */
public class AuditDAO {

    public void log(AuditLog entry) {
        String sql = "INSERT INTO audit_logs (user_id, role, action, entity_type, entity_id, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, entry.getUserId() > 0 ? entry.getUserId() : null, Types.INTEGER);
            stmt.setString(2, entry.getRole());
            stmt.setString(3, entry.getAction());
            stmt.setString(4, entry.getEntityType());
            stmt.setObject(5, entry.getEntityId() > 0 ? entry.getEntityId() : null, Types.INTEGER);
            stmt.setString(6, entry.getDescription());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AuditDAO] log error: " + e.getMessage());
        }
    }

    /**
     * Paginated log retrieval with optional filters.
     */
    public List<AuditLog> getFilteredLogs(String dateFilter, String userFilter, String roleFilter,
                                           String actionFilter, String entityFilter,
                                           String searchText, int limit, int offset) {
        List<AuditLog> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM audit_logs WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (dateFilter != null && !dateFilter.isBlank()) {
            sql.append("AND DATE(created_at) = ? ");
            params.add(dateFilter);
        }
        if (userFilter != null && !userFilter.isBlank()) {
            try { params.add(Integer.parseInt(userFilter)); sql.append("AND user_id = ? "); }
            catch (NumberFormatException ignored) {}
        }
        if (roleFilter != null && !roleFilter.isBlank() && !"All".equals(roleFilter)) {
            sql.append("AND role = ? "); params.add(roleFilter);
        }
        if (actionFilter != null && !actionFilter.isBlank() && !"All".equals(actionFilter)) {
            sql.append("AND action = ? "); params.add(actionFilter);
        }
        if (entityFilter != null && !entityFilter.isBlank() && !"All".equals(entityFilter)) {
            sql.append("AND entity_type = ? "); params.add(entityFilter);
        }
        if (searchText != null && !searchText.isBlank()) {
            sql.append("AND (description LIKE ? OR action LIKE ?) ");
            params.add("%" + searchText + "%");
            params.add("%" + searchText + "%");
        }
        sql.append("ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(map(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AuditDAO] getFilteredLogs error: " + e.getMessage());
        }
        return results;
    }

    public int countFilteredLogs(String dateFilter, String userFilter, String roleFilter,
                                  String actionFilter, String entityFilter, String searchText) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_logs WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (dateFilter != null && !dateFilter.isBlank()) {
            sql.append("AND DATE(created_at) = ? "); params.add(dateFilter);
        }
        if (userFilter != null && !userFilter.isBlank()) {
            try { params.add(Integer.parseInt(userFilter)); sql.append("AND user_id = ? "); }
            catch (NumberFormatException ignored) {}
        }
        if (roleFilter != null && !roleFilter.isBlank() && !"All".equals(roleFilter)) {
            sql.append("AND role = ? "); params.add(roleFilter);
        }
        if (actionFilter != null && !actionFilter.isBlank() && !"All".equals(actionFilter)) {
            sql.append("AND action = ? "); params.add(actionFilter);
        }
        if (entityFilter != null && !entityFilter.isBlank() && !"All".equals(entityFilter)) {
            sql.append("AND entity_type = ? "); params.add(entityFilter);
        }
        if (searchText != null && !searchText.isBlank()) {
            sql.append("AND (description LIKE ? OR action LIKE ?) ");
            params.add("%" + searchText + "%");
            params.add("%" + searchText + "%");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[AuditDAO] countFilteredLogs error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Security summary KPIs.
     */
    public Map<String, Integer> getSecuritySummary() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        String sql = "SELECT " +
            "SUM(CASE WHEN action = 'LOGIN_FAILED' AND DATE(created_at) = CURDATE() THEN 1 ELSE 0 END) AS failed_today, " +
            "SUM(CASE WHEN action = 'LOGIN_SUCCESS' AND DATE(created_at) = CURDATE() THEN 1 ELSE 0 END) AS success_today, " +
            "SUM(CASE WHEN action = 'USER_BLOCKED' THEN 1 ELSE 0 END) AS users_blocked " +
            "FROM audit_logs";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                summary.put("Failed Logins Today", rs.getInt("failed_today"));
                summary.put("Successful Logins Today", rs.getInt("success_today"));
                summary.put("Blocked Users", rs.getInt("users_blocked"));
            }
        } catch (SQLException e) {
            System.err.println("[AuditDAO] getSecuritySummary error: " + e.getMessage());
        }
        return summary;
    }

    /**
     * Export filtered logs to a CSV file. Returns the file path or null on failure.
     */
    public String exportToCsv(List<AuditLog> logs) {
        String path = System.getProperty("java.io.tmpdir") + "cinebook_audit_"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        try (FileWriter fw = new FileWriter(path)) {
            fw.write("ID,User ID,Role,Action,Entity Type,Entity ID,Description,Created At\n");
            for (AuditLog log : logs) {
                fw.write(String.format("%d,%d,%s,%s,%s,%d,\"%s\",%s\n",
                    log.getId(), log.getUserId(),
                    safe(log.getRole()), safe(log.getAction()),
                    safe(log.getEntityType()), log.getEntityId(),
                    safe(log.getDescription()).replace("\"", "'"),
                    log.getCreatedAt() != null ? log.getCreatedAt().toString() : ""));
            }
            return path;
        } catch (IOException e) {
            System.err.println("[AuditDAO] exportToCsv error: " + e.getMessage());
            return null;
        }
    }

    private String safe(String s) { return s != null ? s : ""; }

    private AuditLog map(ResultSet rs) throws SQLException {
        AuditLog a = new AuditLog();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setRole(rs.getString("role"));
        a.setAction(rs.getString("action"));
        a.setEntityType(rs.getString("entity_type"));
        a.setEntityId(rs.getInt("entity_id"));
        a.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        return a;
    }
}
