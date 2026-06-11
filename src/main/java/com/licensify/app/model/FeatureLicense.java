package com.licensify.app.model;

/**
 * Model class representing a Feature License.
 */
public class FeatureLicense {
    private String id;
    private String licenseKey;
    private String name;
    private String status; // "Enabled" or "Disabled"
    private String type; // "Permanent", "Expiry", "Trial"
    private String expiry; // "—", a date, or "X days left"

    // Constructors
    public FeatureLicense() {}

    public FeatureLicense(String id, String licenseKey, String name, String status, String type, String expiry) {
        this.id = id;
        this.licenseKey = licenseKey;
        this.name = name;
        this.status = status;
        this.type = type;
        this.expiry = expiry;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }
}
