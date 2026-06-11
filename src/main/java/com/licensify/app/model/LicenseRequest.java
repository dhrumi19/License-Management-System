package com.licensify.app.model;

import java.sql.Timestamp;

public class LicenseRequest {
    private int id;
    private int userId;
    private int licenseId;
    private String requestType;
    private String status;
    private Timestamp requestDate;
    private Timestamp actionDate;
    private String duration;
    private String features;

    // Constructors
    public LicenseRequest() {
    }

    public LicenseRequest(int userId, int licenseId, String requestType, String status, String duration, String features) {
        this.userId = userId;
        this.licenseId = licenseId;
        this.requestType = requestType;
        this.status = status;
        this.duration = duration;
        this.features = features;
    }

    public LicenseRequest(int id, int userId, int licenseId, String requestType, String status, Timestamp requestDate, Timestamp actionDate, String duration, String features) {
        this.id = id;
        this.userId = userId;
        this.licenseId = licenseId;
        this.requestType = requestType;
        this.status = status;
        this.requestDate = requestDate;
        this.actionDate = actionDate;
        this.duration = duration;
        this.features = features;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getLicenseId() { return licenseId; }
    public void setLicenseId(int licenseId) { this.licenseId = licenseId; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getRequestDate() { return requestDate; }
    public void setRequestDate(Timestamp requestDate) { this.requestDate = requestDate; }

    public Timestamp getActionDate() { return actionDate; }
    public void setActionDate(Timestamp actionDate) { this.actionDate = actionDate; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }
}
