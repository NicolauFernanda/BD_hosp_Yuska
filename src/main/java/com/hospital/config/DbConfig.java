package com.hospital.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConfig {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/hospitaldb";
    private static final String USER = "postgres";
    private static final String PASSWORD = "a";

    // Get Raw JDBC Connection
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    // No-op method for compatibility
    public static synchronized void shutdown() {
        // No session factory to close in JDBC mode
    }
}
