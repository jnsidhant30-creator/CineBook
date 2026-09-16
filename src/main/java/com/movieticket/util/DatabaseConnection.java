package com.movieticket.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String PROPERTIES_FILE = "db.properties";
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                System.err.println("Sorry, unable to find " + PROPERTIES_FILE);
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            System.err.println("Error reading " + PROPERTIES_FILE);
            ex.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        if (url == null || user == null || password == null) {
            throw new SQLException("Database credentials are not properly configured in " + PROPERTIES_FILE);
        }

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.err.println("Unable to connect to the MySQL database. Please check the database server and configuration.");
            throw e;
        }
    }

    public static void testConnection() {
        System.out.println("Testing JDBC Connection...");
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Connection established successfully!");
                
                // Read basic metadata
                java.sql.DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("Database Product Name: " + metaData.getDatabaseProductName());
                System.out.println("Database Product Version: " + metaData.getDatabaseProductVersion());
                System.out.println("JDBC Driver Name: " + metaData.getDriverName());
                System.out.println("JDBC Driver Version: " + metaData.getDriverVersion());
                
                // Simple query check
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT 1")) {
                    if (rs.next()) {
                        System.out.println("SELECT 1 query executed successfully: " + rs.getInt(1));
                    }
                }
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.err.println("Connection test failed!");
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        testConnection();
    }
}
