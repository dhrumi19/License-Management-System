package com.licensify.app.dao;

import com.licensify.app.database.DatabaseConnection;
import com.licensify.app.model.FeatureLicense;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'feature_licenses' table.
 */
public class FeatureLicenseDao {

    /**
     * Gets all features assigned to a specific license key.
     */
    public List<FeatureLicense> getFeaturesByLicenseKey(String licenseKey) {
        List<FeatureLicense> list = new ArrayList<>();
        String sql = "SELECT * FROM feature_licenses WHERE license_key = ? ORDER BY name ASC;";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, licenseKey);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToFeature(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching feature licenses: " + e.getMessage());
        }
        return list;
    }

    /**
     * Creates a new feature license record.
     */
    public boolean createFeature(FeatureLicense feat) {
        String sql = "INSERT INTO feature_licenses (id, license_key, name, status, type, expiry) VALUES (?, ?, ?, ?, ?, ?);";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, feat.getId());
            pstmt.setString(2, feat.getLicenseKey());
            pstmt.setString(3, feat.getName());
            pstmt.setString(4, feat.getStatus());
            pstmt.setString(5, feat.getType());
            pstmt.setString(6, feat.getExpiry());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating feature license: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing feature license record.
     */
    public boolean updateFeature(FeatureLicense feat) {
        String sql = "UPDATE feature_licenses SET name = ?, status = ?, type = ?, expiry = ? WHERE id = ?;";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, feat.getName());
            pstmt.setString(2, feat.getStatus());
            pstmt.setString(3, feat.getType());
            pstmt.setString(4, feat.getExpiry());
            pstmt.setString(5, feat.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating feature license: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a feature license record.
     */
    public boolean deleteFeature(String id) {
        String sql = "DELETE FROM feature_licenses WHERE id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting feature license: " + e.getMessage());
            return false;
        }
    }

    private FeatureLicense mapResultSetToFeature(ResultSet rs) throws SQLException {
        FeatureLicense feat = new FeatureLicense();
        feat.setId(rs.getString("id"));
        feat.setLicenseKey(rs.getString("license_key"));
        feat.setName(rs.getString("name"));
        feat.setStatus(rs.getString("status"));
        feat.setType(rs.getString("type"));
        feat.setExpiry(rs.getString("expiry"));
        return feat;
    }
}
