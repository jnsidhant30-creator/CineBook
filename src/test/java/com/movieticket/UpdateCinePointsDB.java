package com.movieticket;

import com.movieticket.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;

public class UpdateCinePointsDB {
    public static void main(String[] args) {
        System.out.println("Starting Database Migration for CinePoints...");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Add show_id
            try {
                stmt.executeUpdate("ALTER TABLE cinepoints_transactions ADD COLUMN show_id INT NULL AFTER booking_id");
                System.out.println("Added show_id column.");
            } catch (Exception e) { System.out.println("show_id already exists or error: " + e.getMessage()); }

            // Add status
            try {
                stmt.executeUpdate("ALTER TABLE cinepoints_transactions ADD COLUMN status VARCHAR(20) DEFAULT 'AVAILABLE' AFTER points");
                System.out.println("Added status column.");
            } catch (Exception e) { System.out.println("status already exists or error: " + e.getMessage()); }

            // Add show_start_time
            try {
                stmt.executeUpdate("ALTER TABLE cinepoints_transactions ADD COLUMN show_start_time DATETIME NULL AFTER status");
                System.out.println("Added show_start_time column.");
            } catch (Exception e) { System.out.println("show_start_time already exists or error: " + e.getMessage()); }

            // Add foreign key for show_id
            try {
                stmt.executeUpdate("ALTER TABLE cinepoints_transactions ADD CONSTRAINT fk_cinepoints_show FOREIGN KEY (show_id) REFERENCES shows(show_id) ON DELETE SET NULL");
                System.out.println("Added foreign key fk_cinepoints_show.");
            } catch (Exception e) { System.out.println("Foreign key already exists or error: " + e.getMessage()); }
            
            System.out.println("Database migration complete.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
