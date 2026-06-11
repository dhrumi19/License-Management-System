package com.licensify.app.controller;

import com.licensify.app.dao.FeatureLicenseDao;
import com.licensify.app.model.FeatureLicense;
import com.licensify.app.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Controller for the Feature creation/modification form.
 */
public class FeatureFormController {

    @FXML
    private Label formTitleLabel;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private Label lblExpiryDateTitle;
    @FXML
    private DatePicker expiryDatePicker;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private Button saveBtn;

    private final FeatureLicenseDao featureLicenseDao = new FeatureLicenseDao();
    private FeatureLicense existingFeature;
    private String licenseKey;
    private boolean isEditMode = false;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    @FXML
    public void initialize() {
        // Populate type combo box
        typeComboBox.setItems(FXCollections.observableArrayList("Permanent", "Expiry", "Trial"));
        typeComboBox.setValue("Permanent");

        // Populate status combo box
        statusComboBox.setItems(FXCollections.observableArrayList("Enabled", "Disabled"));
        statusComboBox.setValue("Enabled");

        // Listen for type changes to toggle expiry date picker
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Permanent".equalsIgnoreCase(newVal)) {
                if (expiryDatePicker.getValue() == null || expiryDatePicker.getValue().isBefore(LocalDate.of(2099, 1, 1))) {
                    expiryDatePicker.setValue(LocalDate.of(2099, 12, 31));
                }
                expiryDatePicker.setDisable(false);
                lblExpiryDateTitle.setOpacity(1.0);
            } else {
                expiryDatePicker.setDisable(false);
                lblExpiryDateTitle.setOpacity(1.0);
                if (expiryDatePicker.getValue() == null || expiryDatePicker.getValue().equals(LocalDate.of(2099, 12, 31))) {
                    expiryDatePicker.setValue(LocalDate.now().plusMonths(6));
                }
            }
        });

        // Default: Permanent has expiry date picker enabled and defaulted
        expiryDatePicker.setValue(LocalDate.of(2099, 12, 31));
        expiryDatePicker.setDisable(false);
        lblExpiryDateTitle.setOpacity(1.0);
    }

    /**
     * Pre-fills fields if editing an existing feature.
     */
    public void setFeature(FeatureLicense feature, String licenseKey) {
        this.existingFeature = feature;
        this.licenseKey = licenseKey;

        if (feature != null) {
            this.isEditMode = true;
            formTitleLabel.setText("Edit Feature");
            saveBtn.setText("Save Feature");

            nameField.setText(feature.getName());
            typeComboBox.setValue(feature.getType());
            statusComboBox.setValue(feature.getStatus());

            if ("Permanent".equalsIgnoreCase(feature.getType())) {
                expiryDatePicker.setDisable(false);
                lblExpiryDateTitle.setOpacity(1.0);
                if ("Never".equalsIgnoreCase(feature.getExpiry()) || feature.getExpiry() == null || "—".equals(feature.getExpiry())) {
                    expiryDatePicker.setValue(LocalDate.of(2099, 12, 31));
                } else {
                    try {
                        LocalDate parsedDate = LocalDate.parse(feature.getExpiry(), DATE_FORMATTER);
                        expiryDatePicker.setValue(parsedDate);
                    } catch (Exception e) {
                        expiryDatePicker.setValue(LocalDate.of(2099, 12, 31));
                    }
                }
            } else {
                expiryDatePicker.setDisable(false);
                lblExpiryDateTitle.setOpacity(1.0);
                try {
                    // Try parsing the expiry date
                    LocalDate parsedDate = LocalDate.parse(feature.getExpiry(), DATE_FORMATTER);
                    expiryDatePicker.setValue(parsedDate);
                } catch (Exception e) {
                    expiryDatePicker.setValue(LocalDate.now().plusMonths(6));
                }
            }
        } else {
            this.isEditMode = false;
            formTitleLabel.setText("Add New Feature");
            saveBtn.setText("Save Feature");
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String name = nameField.getText().trim();
        String type = typeComboBox.getValue();
        String status = statusComboBox.getValue();
        LocalDate expiryDate = expiryDatePicker.getValue();

        // Validations
        if (name.isEmpty()) {
            AlertHelper.showAlert(AlertType.ERROR, "Validation Error", "Name Required", "Please enter a feature name.");
            return;
        }

        String expiryStr = "Never";
        if (!"Permanent".equalsIgnoreCase(type)) {
            if (expiryDate == null) {
                AlertHelper.showAlert(AlertType.ERROR, "Validation Error", "Expiry Date Required", "Expiry date is required for Expiry and Trial feature types.");
                return;
            }
            expiryStr = expiryDate.format(DATE_FORMATTER);
        } else {
            if (expiryDate != null) {
                expiryStr = expiryDate.format(DATE_FORMATTER);
            }
        }

        boolean success;

        if (isEditMode && existingFeature != null) {
            existingFeature.setName(name);
            existingFeature.setType(type);
            existingFeature.setStatus(status);
            existingFeature.setExpiry(expiryStr);
            success = featureLicenseDao.updateFeature(existingFeature);
        } else {
            // Add new feature
            String randId = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            FeatureLicense newFeature = new FeatureLicense();
            newFeature.setId(randId);
            newFeature.setLicenseKey(licenseKey);
            newFeature.setName(name);
            newFeature.setType(type);
            newFeature.setStatus(status);
            newFeature.setExpiry(expiryStr);
            success = featureLicenseDao.createFeature(newFeature);
        }

        if (success) {
            AlertHelper.showAlert(AlertType.INFORMATION, "Success", "Feature Saved", "Feature details saved successfully.");
            closeWindow();
        } else {
            AlertHelper.showAlert(AlertType.ERROR, "Database Error", "Failed to Save", "Could not save feature to database.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }
}
