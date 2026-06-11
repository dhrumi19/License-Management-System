package com.licensify.app.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Model class representing a License.
 */
public class License {
    private int id;
    private String licenseKey;
    private int userId;
    private String name;
    private String type; // "Expiry-Based", "Permanent", "Cloud"
    private String dimension; // "—", "3D Modeling", "2D Drafting"
    private double cost;
    private LocalDate issueDate;
    private LocalDate expiryDate; // Can be null for permanent licenses
    private String status; // "ACTIVE", "EXPIRED", "SUSPENDED"
    private String features; // Comma-separated feature keys, e.g. "EXPORT,API,CHARTS"

    // Transient field for display convenience (e.g. TableView)
    private String username;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    // Constructors
    public License() {}

    public License(int id, String licenseKey, int userId, String name, String type, String dimension, double cost, 
                   LocalDate issueDate, LocalDate expiryDate, String status, String features) {
        this.id = id;
        this.licenseKey = licenseKey;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.dimension = dimension;
        this.cost = cost;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.features = features;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Formatted Dates for UI Display
    public String getFormattedCreatedDate() {
        if (issueDate == null) return "—";
        return issueDate.format(DATE_FORMATTER);
    }

    public String getFormattedExpiryDate() {
        if ("Permanent".equalsIgnoreCase(type)) {
            return "Permanent";
        }
        if (expiryDate == null) return "—";
        return expiryDate.format(DATE_FORMATTER);
    }

    // Helper business logic
    public boolean isExpired() {
        if ("Permanent".equalsIgnoreCase(type)) {
            return false;
        }
        return "EXPIRED".equalsIgnoreCase(status) || (expiryDate != null && expiryDate.isBefore(LocalDate.now()));
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status) && !isExpired();
    }

    public boolean isSuspended() {
        return "SUSPENDED".equalsIgnoreCase(status);
    }

    public boolean hasFeature(String featureName) {
        if (features == null || features.isEmpty()) {
            return false;
        }
        String[] list = features.split(",");
        for (String f : list) {
            if (f.trim().equalsIgnoreCase(featureName)) {
                return true;
            }
        }
        return false;
    }
}
