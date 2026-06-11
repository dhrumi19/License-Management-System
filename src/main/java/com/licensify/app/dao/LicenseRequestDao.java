package com.licensify.app.dao;

import com.licensify.app.database.DatabaseConnection;
import com.licensify.app.model.LicenseRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LicenseRequestDao {

    public LicenseRequest getUserPendingRequest(int userId) {
        String sql = "SELECT * FROM license_requests WHERE user_id = ? AND status = 'PENDING' LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRequest(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending request: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean createRequest(LicenseRequest request) {
        if (getUserPendingRequest(request.getUserId()) != null) {
            System.err.println("Backend restriction: User already has a pending request.");
            return false;
        }

        String sql = "INSERT INTO license_requests (user_id, license_id, request_type, status, duration, features) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, request.getUserId());
            pstmt.setInt(2, request.getLicenseId());
            pstmt.setString(3, request.getRequestType());
            pstmt.setString(4, request.getStatus());
            pstmt.setString(5, request.getDuration());
            pstmt.setString(6, request.getFeatures());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating license request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<LicenseRequest> getAllRequests() {
        List<LicenseRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM license_requests ORDER BY request_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                requests.add(mapResultSetToRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all requests: " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }

    public List<LicenseRequest> getRequestsByUserId(int userId) {
        List<LicenseRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM license_requests WHERE user_id = ? ORDER BY request_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching requests by user: " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }

    public boolean updateRequestStatus(int requestId, String status) {
        String sql = "UPDATE license_requests SET status = ?, action_date = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, status);
            pstmt.setInt(2, requestId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating request status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private LicenseRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        return new LicenseRequest(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getInt("license_id"),
            rs.getString("request_type"),
            rs.getString("status"),
            rs.getTimestamp("request_date"),
            rs.getTimestamp("action_date"),
            rs.getString("duration"),
            rs.getString("features")
        );
    }
}
