package com.licensify.app.database;

import com.licensify.app.util.LicenseGenerator;
import com.licensify.app.util.PasswordHasher;
import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * Handles database connection lifecycle and schema initialization/seeding for SQLite.
 */
public class DatabaseConnection {

    private static final String DB_NAME = "licensify.db";
    private static final String CONNECTION_URL = "jdbc:sqlite:" + DB_NAME;

    static {
        try {
            // Load the SQLite JDBC driver class
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load SQLite JDBC driver: " + e.getMessage());
        }
    }

    /**
     * Obtains a connection to the SQLite database.
     * 
     * @return Connection object.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }

    /**
     * Initializes the database schema (creates tables and seeds default data if empty).
     */
    public static void initializeDatabase() {
        // Create tables
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username VARCHAR(50) UNIQUE NOT NULL,"
                + "email VARCHAR(100) UNIQUE NOT NULL,"
                + "password_hash VARCHAR(255) NOT NULL,"
                + "role VARCHAR(20) NOT NULL CHECK(role IN ('ADMIN', 'USER')),"
                + "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'SUSPENDED')),"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        String createLicensesTable = "CREATE TABLE IF NOT EXISTS licenses ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "license_key VARCHAR(100) UNIQUE NOT NULL,"
                + "user_id INTEGER NOT NULL,"
                + "name VARCHAR(100),"
                + "type VARCHAR(50),"
                + "dimension VARCHAR(50),"
                + "cost DOUBLE,"
                + "issue_date DATE NOT NULL,"
                + "expiry_date DATE,"
                + "status VARCHAR(20) NOT NULL CHECK(status IN ('ACTIVE', 'EXPIRED', 'SUSPENDED')),"
                + "features TEXT,"
                + "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE"
                + ");";

        String createFeatureLicensesTable = "CREATE TABLE IF NOT EXISTS feature_licenses ("
                + "id VARCHAR(50) PRIMARY KEY,"
                + "license_key VARCHAR(100) NOT NULL,"
                + "name VARCHAR(100) NOT NULL,"
                + "status VARCHAR(20) NOT NULL,"
                + "type VARCHAR(20) NOT NULL,"
                + "expiry VARCHAR(50),"
                + "FOREIGN KEY(license_key) REFERENCES licenses(license_key) ON DELETE CASCADE"
                + ");";

        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "license_id INTEGER,"
                + "action_type VARCHAR(50) NOT NULL,"
                + "action_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "details TEXT,"
                + "FOREIGN KEY(license_id) REFERENCES licenses(id) ON DELETE SET NULL"
                + ");";

        String createLicenseRequestsTable = "CREATE TABLE IF NOT EXISTS license_requests ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL,"
                + "license_id INTEGER,"
                + "request_type VARCHAR(20) NOT NULL CHECK(request_type IN ('RENEW', 'NEW')),"
                + "status VARCHAR(20) NOT NULL CHECK(status IN ('PENDING', 'APPROVED', 'REJECTED')),"
                + "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "action_date TIMESTAMP,"
                + "duration VARCHAR(50),"
                + "features TEXT,"
                + "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE"
                + ");";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable foreign key support in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Schema Migration: Check if license_requests table exists
            boolean needsRecreation = false;
            try (ResultSet rsTable = conn.getMetaData().getTables(null, null, "license_requests", null)) {
                if (!rsTable.next()) {
                    needsRecreation = true;
                }
            } catch (SQLException e) {
                needsRecreation = true;
            }

            if (needsRecreation) {
                System.out.println("Upgrading database schema: dropping old tables...");
                stmt.execute("DROP TABLE IF EXISTS license_requests;");
                stmt.execute("DROP TABLE IF EXISTS transactions;");
                stmt.execute("DROP TABLE IF EXISTS feature_licenses;");
                stmt.execute("DROP TABLE IF EXISTS licenses;");
            }

            // Execute table creations
            stmt.execute(createUsersTable);
            stmt.execute(createLicensesTable);
            stmt.execute(createFeatureLicensesTable);
            stmt.execute(createTransactionsTable);
            stmt.execute(createLicenseRequestsTable);
            
            // Alter table if license_id doesn't exist
            try {
                stmt.execute("ALTER TABLE license_requests ADD COLUMN license_id INTEGER;");
            } catch (SQLException e) {
                // Column likely already exists
            }

            // Alter users table to add status column if it doesn't exist
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'SUSPENDED'));");
            } catch (SQLException e) {
                // Column likely already exists
            }

            // Seed default users if users table is empty
            seedDefaultUsers(conn);

            // Seed default active license for standard user if missing
            seedDefaultLicenseIfMissing(conn);

        } catch (SQLException e) {
            System.err.println("Error initializing database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Seeds default admin and user accounts if the database has no users.
     */
    private static void seedDefaultUsers(Connection conn) throws SQLException {
        String checkUsers = "SELECT COUNT(*) AS count FROM users;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkUsers)) {
            
            if (rs.next() && rs.getInt("count") == 0) {
                System.out.println("Seeding default Admin and User accounts...");

                String insertUser = "INSERT INTO users (username, email, password_hash, role, status) VALUES (?, ?, ?, ?, ?);";
                try (PreparedStatement pstmt = conn.prepareStatement(insertUser)) {
                    // Seed Admin
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, "admin@licensify.com");
                    pstmt.setString(3, PasswordHasher.hashPassword("admin123"));
                    pstmt.setString(4, "ADMIN");
                    pstmt.setString(5, "ACTIVE");
                    pstmt.executeUpdate();

                    // Seed standard User
                    pstmt.setString(1, "user");
                    pstmt.setString(2, "user@licensify.com");
                    pstmt.setString(3, PasswordHasher.hashPassword("user123"));
                    pstmt.setString(4, "USER");
                    pstmt.setString(5, "ACTIVE");
                    pstmt.executeUpdate();

                    System.out.println("Seeding complete. Accounts: admin/admin123, user/user123");
                }
            }
        }
    }

    /**
     * Seeds a default active license for the standard 'user' account if they don't have one.
     */
    private static void seedDefaultLicenseIfMissing(Connection conn) throws SQLException {
        int userId = -1;
        String queryUser = "SELECT id FROM users WHERE username = 'user';";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryUser)) {
            if (rs.next()) {
                userId = rs.getInt("id");
            }
        }

        if (userId != -1) {
            String checkLicense = "SELECT COUNT(*) AS count FROM licenses WHERE user_id = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(checkLicense)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt("count") == 0) {
                        System.out.println("Seeding default Active License for user...");
                        
                        String key = LicenseGenerator.generateLicenseKey();
                        String insertLic = "INSERT INTO licenses (license_key, user_id, name, type, dimension, cost, issue_date, expiry_date, status, features) "
                                         + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                        try (PreparedStatement pInsert = conn.prepareStatement(insertLic, Statement.RETURN_GENERATED_KEYS)) {
                            pInsert.setString(1, key);
                            pInsert.setInt(2, userId);
                            pInsert.setString(3, "Primary License");
                            pInsert.setString(4, "Permanent");
                            pInsert.setString(5, "3D Advanced");
                            pInsert.setDouble(6, 499.00);
                            pInsert.setDate(7, Date.valueOf(LocalDate.now()));
                            pInsert.setDate(8, Date.valueOf("2099-12-31"));
                            pInsert.setString(9, "ACTIVE");
                            pInsert.setString(10, "Dual Energy");
                            pInsert.executeUpdate();
                            
                            int licenseId = -1;
                            try (ResultSet generatedKeys = pInsert.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    licenseId = generatedKeys.getInt(1);
                                }
                            }
                                String insertTx = "INSERT INTO transactions (license_id, action_type, details) VALUES (?, ?, ?);";
                                try (PreparedStatement pTx = conn.prepareStatement(insertTx)) {
                                    pTx.setInt(1, licenseId);
                                    pTx.setString(2, "ISSUE");
                                    pTx.setString(3, "Primary active test license seeded automatically.");
                                    pTx.executeUpdate();
                                }

                                // Seed exactly 1 dummy feature record for tests (as requested)
                                String insertFeat = "INSERT INTO feature_licenses (id, license_key, name, status, type, expiry) VALUES (?, ?, ?, ?, ?, ?);";
                                try (PreparedStatement pFeat = conn.prepareStatement(insertFeat)) {
                                    pFeat.setString(1, "C7AC4104");
                                    pFeat.setString(2, key);
                                    pFeat.setString(3, "Dual Energy");
                                    pFeat.setString(4, "Enabled");
                                    pFeat.setString(5, "Permanent");
                                    pFeat.setString(6, "Never");
                                    pFeat.executeUpdate();
                                }
                        }
                    }
                }
            }
        }
    }
}
