package com.licensify.app.model;

import java.sql.Timestamp;

/**
 * Model class representing a License Transaction Log.
 */
public class Transaction {
    private int id;
    private int licenseId;
    private String actionType; // 'ISSUE', 'RENEW', 'SUSPEND', 'DELETE', etc.
    private Timestamp actionDate;
    private String details;

    // Transient fields for display in logs
    private String licenseKey;
    private String username;

    // Constructors
    public Transaction() {}

    public Transaction(int id, int licenseId, String actionType, Timestamp actionDate, String details) {
        this.id = id;
        this.licenseId = licenseId;
        this.actionType = actionType;
        this.actionDate = actionDate;
        this.details = details;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(int licenseId) {
        this.licenseId = licenseId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Timestamp getActionDate() {
        return actionDate;
    }

    public void setActionDate(Timestamp actionDate) {
        this.actionDate = actionDate;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
