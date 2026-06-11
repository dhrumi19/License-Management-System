package com.licensify.app.dao;

import com.licensify.app.database.DatabaseConnection;
import com.licensify.app.model.License;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'licenses' table.
 */
public class LicenseDao {

    private final TransactionDao transactionDao = new TransactionDao();

    /**
     * Retrieves all licenses joined with the username of the user.
     */
    public List<License> getAllLicenses() {
        List<License> licenses = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM licenses l "
                   + "JOIN users u ON l.user_id = u.id "
                   + "ORDER BY l.expiry_date DESC;";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                License lic = mapResultSetToLicense(rs);
                lic.setUsername(rs.getString("username"));
                licenses.add(lic);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all licenses: " + e.getMessage());
        }
        return licenses;
    }

    /**
     * Retrieves a license by its key.
     */
    public License getLicenseByKey(String key) {
        String sql = "SELECT l.*, u.username FROM licenses l "
                   + "JOIN users u ON l.user_id = u.id "
                   + "WHERE l.license_key = ?;";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    License lic = mapResultSetToLicense(rs);
                    lic.setUsername(rs.getString("username"));
                    return lic;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching license by key: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves the license for a specific user.
     */
    public License getLicenseByUserId(int userId) {
        String sql = "SELECT l.*, u.username FROM licenses l "
                   + "JOIN users u ON l.user_id = u.id "
                   + "WHERE l.user_id = ? ORDER BY l.id DESC LIMIT 1;";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    License lic = mapResultSetToLicense(rs);
                    lic.setUsername(rs.getString("username"));
                    return lic;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching license by user ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all licenses for a specific user ID.
     */
    public List<License> getAllLicensesByUserId(int userId) {
        List<License> licenses = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM licenses l "
                   + "JOIN users u ON l.user_id = u.id "
                   + "WHERE l.user_id = ? "
                   + "ORDER BY l.expiry_date DESC;";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    License lic = mapResultSetToLicense(rs);
                    lic.setUsername(rs.getString("username"));
                    licenses.add(lic);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user licenses by user ID: " + e.getMessage());
        }
        return licenses;
    }

    /**
     * Creates a new license and logs the transaction.
     */
    public boolean createLicense(License license) {
        String sql = "INSERT INTO licenses (license_key, user_id, name, type, dimension, cost, issue_date, expiry_date, status, features) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, license.getLicenseKey());
            pstmt.setInt(2, license.getUserId());
            pstmt.setString(3, license.getName());
            pstmt.setString(4, license.getType());
            pstmt.setString(5, license.getDimension());
            pstmt.setDouble(6, license.getCost());
            
            pstmt.setDate(7, license.getIssueDate() != null ? Date.valueOf(license.getIssueDate()) : Date.valueOf(LocalDate.now()));
            if (license.getExpiryDate() != null) {
                pstmt.setDate(8, Date.valueOf(license.getExpiryDate()));
            } else {
                pstmt.setNull(8, java.sql.Types.DATE);
            }
            pstmt.setString(9, license.getStatus());
            pstmt.setString(10, license.getFeatures());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        license.setId(generatedKeys.getInt(1));
                    }
                }
                // Log transaction
                transactionDao.logTransaction(license.getId(), "ISSUE", 
                    "License '" + license.getName() + "' issued to user " + license.getUserId() + " with key: " + license.getLicenseKey());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating license: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates an existing license and logs the transaction.
     */
    public boolean updateLicense(License license) {
        String sql = "UPDATE licenses SET license_key = ?, user_id = ?, name = ?, type = ?, dimension = ?, cost = ?, issue_date = ?, expiry_date = ?, status = ?, features = ? WHERE id = ?;";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, license.getLicenseKey());
            pstmt.setInt(2, license.getUserId());
            pstmt.setString(3, license.getName());
            pstmt.setString(4, license.getType());
            pstmt.setString(5, license.getDimension());
            pstmt.setDouble(6, license.getCost());
            
            pstmt.setDate(7, license.getIssueDate() != null ? Date.valueOf(license.getIssueDate()) : Date.valueOf(LocalDate.now()));
            if (license.getExpiryDate() != null) {
                pstmt.setDate(8, Date.valueOf(license.getExpiryDate()));
            } else {
                pstmt.setNull(8, java.sql.Types.DATE);
            }
            pstmt.setString(9, license.getStatus());
            pstmt.setString(10, license.getFeatures());
            pstmt.setInt(11, license.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Log transaction
                transactionDao.logTransaction(license.getId(), "UPDATE", 
                    "License updated. Key: " + license.getLicenseKey() + ", User ID: " + license.getUserId() + ", New Status: " + license.getStatus() + ", Cost: " + license.getCost());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating license: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a license.
     */
    public boolean deleteLicense(int id) {
        // Log transaction first (before cascade delete)
        transactionDao.logTransaction(id, "DELETE", "License ID " + id + " was deleted.");
        
        String sql = "DELETE FROM licenses WHERE id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting license: " + e.getMessage());
        }
        return false;
    }

    /**
     * Map current row of ResultSet to a License object.
     */
    private License mapResultSetToLicense(ResultSet rs) throws SQLException {
        License lic = new License();
        lic.setId(rs.getInt("id"));
        lic.setLicenseKey(rs.getString("license_key"));
        lic.setUserId(rs.getInt("user_id"));
        lic.setName(rs.getString("name"));
        lic.setType(rs.getString("type"));
        lic.setDimension(rs.getString("dimension"));
        lic.setCost(rs.getDouble("cost"));
        
        Date issueD = rs.getDate("issue_date");
        lic.setIssueDate(issueD != null ? issueD.toLocalDate() : null);
        
        Date expiryD = rs.getDate("expiry_date");
        lic.setExpiryDate(expiryD != null ? expiryD.toLocalDate() : null);
        
        lic.setStatus(rs.getString("status"));
        lic.setFeatures(rs.getString("features"));
        return lic;
    }
}
