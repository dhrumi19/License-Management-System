package com.licensify.app.dao;

import com.licensify.app.database.DatabaseConnection;
import com.licensify.app.model.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'transactions' table (logs of license activities).
 */
public class TransactionDao {

    /**
     * Inserts an audit log entry for license actions.
     */
    public boolean logTransaction(int licenseId, String actionType, String details) {
        String sql = "INSERT INTO transactions (license_id, action_type, details) VALUES (?, ?, ?);";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (licenseId > 0) {
                pstmt.setInt(1, licenseId);
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            pstmt.setString(2, actionType);
            pstmt.setString(3, details);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error logging transaction: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all transactions joined with license keys and username.
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, l.license_key, u.username FROM transactions t "
                   + "LEFT JOIN licenses l ON t.license_id = l.id "
                   + "LEFT JOIN users u ON l.user_id = u.id "
                   + "ORDER BY t.action_date DESC LIMIT 100;";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Transaction tx = new Transaction();
                tx.setId(rs.getInt("id"));
                tx.setLicenseId(rs.getInt("license_id"));
                tx.setActionType(rs.getString("action_type"));
                tx.setActionDate(rs.getTimestamp("action_date"));
                tx.setDetails(rs.getString("details"));
                tx.setLicenseKey(rs.getString("license_key"));
                tx.setUsername(rs.getString("username"));
                transactions.add(tx);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return transactions;
    }
}
