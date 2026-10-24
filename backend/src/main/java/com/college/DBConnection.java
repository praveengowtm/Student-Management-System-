package com.college;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection Utility Class
 * 
 * Manages database connectivity to MySQL using JDBC (Java Database Connectivity).
 * Uses DriverManager.getConnection() with connection parameters.
 * Supports environment variables (DB_URL, DB_USER, DB_PASSWORD) with default fallbacks.
 */
public class DBConnection {

    // =========================================================================
    // CONFIGURATION SECTION
    // Modify these default values if your MySQL credentials differ during lab assessment:
    // =========================================================================
    private static final String DEFAULT_URL = 
            "jdbc:mysql://localhost:3306/college_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root"; // Set to your MySQL password, e.g., "root", "admin", or ""

    // Driver class name for MySQL Connector/J 8.x
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            // Explicitly load the MySQL JDBC driver class into memory
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: MySQL JDBC Driver class not found in classpath!");
            e.printStackTrace();
        }
    }

    // Private constructor to prevent instantiation of utility class
    private DBConnection() {
    }

    /**
     * Establishes and returns a new connection to the MySQL database.
     * 
     * @return Connection active database connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        // Read environment variables if set; otherwise use default configuration values
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");

        String url = (envUrl != null && !envUrl.trim().isEmpty()) ? envUrl : DEFAULT_URL;
        String user = (envUser != null && !envUser.trim().isEmpty()) ? envUser : DEFAULT_USER;
        String password = (envPass != null) ? envPass : DEFAULT_PASSWORD;

        return DriverManager.getConnection(url, user, password);
    }
}
