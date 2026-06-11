package com.licensify.app.controller;

import com.licensify.app.dao.LicenseDao;
import com.licensify.app.dao.UserDao;
import com.licensify.app.model.License;
import com.licensify.app.model.User;
import com.licensify.app.util.AlertHelper;
import com.licensify.app.util.LicenseGenerator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the License creation/modification form.
 */
public class LicenseFormController {

    @FXML
    private Label formTitleLabel;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<User> userComboBox;
    @FXML
    private TextField keyField;
    @FXML
    private Button generateBtn;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private ComboBox<String> dimensionComboBox;
    @FXML
    private ComboBox<String> featureComboBox;
    @FXML
    private ComboBox<String> BindHardwareComboBox;  
    @FXML
    private TextField costField;
    @FXML
    private DatePicker issueDatePicker;
    @FXML
    private Label lblExpiryDateTitle;
    @FXML
    private DatePicker expiryDatePicker;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private CheckBox chkExport;
    @FXML
    private CheckBox chkCharts;
    @FXML
    private CheckBox chkApi;
    @FXML
    private Button saveBtn;

    private final UserDao userDao = new UserDao();
    private final LicenseDao licenseDao = new LicenseDao();
    private License existingLicense;
    private AdminDashboardController parentController;

    @FXML
    public void initialize() {
        // Populate status combo box
        statusComboBox.setItems(FXCollections.observableArrayList("ACTIVE", "EXPIRED", "SUSPENDED"));
        statusComboBox.setValue("ACTIVE");

        // Populate type combo box
        typeComboBox.setItems(FXCollections.observableArrayList("Expiry-Based", "Permanent", "Cloud"));
        typeComboBox.setValue("Expiry-Based");

        // Populate dimension combo box
        dimensionComboBox.setItems(FXCollections.observableArrayList("2D License", "3D License", "3D Advanced", "All License"));
        dimensionComboBox.setValue("—");

        // Populate feature combo box
        featureComboBox.setItems(FXCollections.observableArrayList("DUALENERGY", "DICOMEXPORT"));
        featureComboBox.setValue("—");  

        // Populate Bind Hardware combo box
        BindHardwareComboBox.setItems(FXCollections.observableArrayList("CPU ID", "Motherboard", "MAC Address", "Disk Serial"));
        BindHardwareComboBox.setValue("—");
        

        // Listen for type changes to toggle expiry date picker
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Permanent".equalsIgnoreCase(newVal)) {
                if (expiryDatePicker.getValue() == null || expiryDatePicker.getValue().isBefore(LocalDate.of(2099, 1, 1))) {
                    expiryDatePicker.setValue(LocalDate.of(2084, 12, 31));
                }
                expiryDatePicker.setDisable(false);
                lblExpiryDateTitle.setOpacity(1.0);
            } else {
                expiryDatePicker.setDisable(false);
                lblExpiryDateTitle.setOpacity(1.0);
                if (expiryDatePicker.getValue() == null || expiryDatePicker.getValue().equals(LocalDate.of(2099, 12, 31))) {
                    expiryDatePicker.setValue(LocalDate.now().plusYears(1));
                }
            }
        });

        // Load users
        List<User> standardUsers = userDao.getAllStandardUsers();
        userComboBox.setItems(FXCollections.observableArrayList(standardUsers));

        // Default dates
        issueDatePicker.setValue(LocalDate.now());
        expiryDatePicker.setValue(LocalDate.now().plusYears(1));
        
        // Allow editing/typing the license key manually
        keyField.setEditable(true);
    }

    public void setParentController(AdminDashboardController parentController) {
        this.parentController = parentController;
    }

    /**
     * Pre-fills fields if editing an existing license.
     */
    public void setLicense(License license) {
        this.existingLicense = license;
        if (license != null) {
            formTitleLabel.setText("Edit License Details");
            
            // In Edit Mode, keep all fields editable/changeable
            userComboBox.setDisable(false);
            keyField.setText(license.getLicenseKey());
            generateBtn.setDisable(false);

            // Populate user combo box with the single existing user for display
            User dummyUser = userDao.getUserById(license.getUserId());
            if (dummyUser != null) {
                userComboBox.setValue(dummyUser);
            }

            // Fill new attributes
            nameField.setText(license.getName());
            typeComboBox.setValue(license.getType());
            dimensionComboBox.setValue(license.getDimension());
            costField.setText(String.format("%.2f", license.getCost()));

            issueDatePicker.setValue(license.getIssueDate());
            issueDatePicker.setDisable(false); // Enable issue date editing
            
            if ("Permanent".equalsIgnoreCase(license.getType())) {
                if (license.getExpiryDate() != null) {
                    expiryDatePicker.setValue(license.getExpiryDate());
                } else {
                    expiryDatePicker.setValue(LocalDate.of(2084, 12, 31));
                }
                expiryDatePicker.setDisable(false);
            } else {
                expiryDatePicker.setValue(license.getExpiryDate());
                expiryDatePicker.setDisable(false);
            }

            statusComboBox.setValue(license.getStatus());

            // Checkboxes
            chkExport.setSelected(license.hasFeature("EXPORT"));
            chkCharts.setSelected(license.hasFeature("CHARTS"));
            chkApi.setSelected(license.hasFeature("API"));
            
            if (license.hasFeature("DUALENERGY")) {
                featureComboBox.setValue("DUALENERGY");
            } else if (license.hasFeature("DICOMEXPORT")) {
                featureComboBox.setValue("DICOMEXPORT");
            } else {
                featureComboBox.setValue("—");
            }
            
            if (license.hasFeature("CPU ID")) {
                BindHardwareComboBox.setValue("CPU ID");
            } else if (license.hasFeature("Motherboard")) {
                BindHardwareComboBox.setValue("Motherboard");
            } else if (license.hasFeature("MAC Address")) {
                BindHardwareComboBox.setValue("MAC Address");
            } else if (license.hasFeature("Disk Serial")) {
                BindHardwareComboBox.setValue("Disk Serial");
            } else {
                BindHardwareComboBox.setValue("—");
            }
            
            saveBtn.setText("Update License");
        } else {
            formTitleLabel.setText("Create New License");
            saveBtn.setText("Save License");
        }
    }

    @FXML
    private void handleGenerateKey(ActionEvent event) {
        keyField.setText(LicenseGenerator.generateLicenseKey());
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String name = nameField.getText().trim();
        User selectedUser = userComboBox.getValue();
        String key = keyField.getText().trim();
        String type = typeComboBox.getValue();
        String dimension = dimensionComboBox.getValue();
        LocalDate issueDate = issueDatePicker.getValue();
        LocalDate expiryDate = expiryDatePicker.getValue();
        String status = statusComboBox.getValue();

        // Validations
        if (name.isEmpty()) {
            AlertHelper.showAlert(AlertType.ERROR, "Validation Error", "Name Required", "Please enter a license name (e.g. Primary License).");
            return;
        }

        if (selectedUser == null) {
            AlertHelper.showAlert(AlertType.ERROR, "Validation Error", "User Required", "Please select a user to assign the license to.");
            return;
        }

        if (key.isEmpty()) {
            AlertHelper.showAlert(AlertType.ERROR, "Validation Error", "Key Required", "Please click 'Generate' to create a unique license key.");
            return;
        }

        double cost = 0.0;
        try {
            cost = Double.parseDouble(costField.getText().trim());
            if (cost < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            AlertHelper.showAlert(AlertType.ERROR, "Validation Error", "Invalid Cost", "Please enter a valid positive decimal value for Cost.");
            return;
        }

        if (issueDate == null) {
            AlertHelper.showAlert(AlertType.ERROR, "Validation Error", "Issue Date Required", "Please select a valid issue date.");
            return;
        }

        if (!"Permanent".equalsIgnoreCase(type)) {
            if (expiryDate == null) {
                AlertHelper.showAlert(AlertType.ERROR, "Validation Error", "Expiry Date Required", "Expiry date is required for Expiry-Based and Cloud license types.");
                return;
            }
            if (expiryDate.isBefore(issueDate)) {
                AlertHelper.showAlert(AlertType.ERROR, "Validation Error", "Invalid Dates", "Expiry date must be after the issue date.");
                return;
            }
        } else {
            if (expiryDate != null && expiryDate.isBefore(issueDate)) {
                AlertHelper.showAlert(AlertType.ERROR, "Validation Error", "Invalid Dates", "Expiry date must be after the issue date.");
                return;
            }
        }

        // Build feature list string
        List<String> featuresList = new ArrayList<>();
        if (chkExport.isSelected()) featuresList.add("EXPORT");
        if (chkCharts.isSelected()) featuresList.add("CHARTS");
        if (chkApi.isSelected()) featuresList.add("API");
        
        String featureComboVal = featureComboBox.getValue();
        if (featureComboVal != null && !"—".equals(featureComboVal)) {
            featuresList.add(featureComboVal);
        }
        
        String hardwareComboVal = BindHardwareComboBox.getValue();
        if (hardwareComboVal != null && !"—".equals(hardwareComboVal)) {
            featuresList.add(hardwareComboVal);
        }
        String features = String.join(",", featuresList);

        boolean success;

        if (existingLicense != null) {
            // Update
            existingLicense.setLicenseKey(key);
            existingLicense.setUserId(selectedUser.getId());
            existingLicense.setName(name);
            existingLicense.setType(type);
            existingLicense.setDimension(dimension);
            existingLicense.setCost(cost);
            existingLicense.setIssueDate(issueDate);
            existingLicense.setExpiryDate(expiryDate);
            existingLicense.setStatus(status);
            existingLicense.setFeatures(features);
            success = licenseDao.updateLicense(existingLicense);
        } else {
            // Check if user already has a license
            License oldLicense = licenseDao.getLicenseByUserId(selectedUser.getId());
            if (oldLicense != null) {
                boolean proceed = AlertHelper.showConfirmation("License Already Exists", "Override Existing License?", 
                        "This user already has a license assigned. Do you want to issue a new one?");
                if (!proceed) {
                    return;
                }
            }

            // Create new
            License newLicense = new License();
            newLicense.setLicenseKey(key);
            newLicense.setUserId(selectedUser.getId());
            newLicense.setName(name);
            newLicense.setType(type);
            newLicense.setDimension(dimension);
            newLicense.setCost(cost);
            newLicense.setIssueDate(issueDate);
            newLicense.setExpiryDate(expiryDate);
            newLicense.setStatus(status);
            newLicense.setFeatures(features);
            success = licenseDao.createLicense(newLicense);
        }

        if (success) {
            AlertHelper.showAlert(AlertType.INFORMATION, "Success", "License Saved", "License details have been saved successfully.");
            closeWindow();
        } else {
            AlertHelper.showAlert(AlertType.ERROR, "Database Error", "Failed to Save", "Could not save license to database. Check database locks.");
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
