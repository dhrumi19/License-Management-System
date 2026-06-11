package com.licensify.app.controller;

import com.licensify.app.dao.LicenseRequestDao;
import com.licensify.app.model.LicenseRequest;
import com.licensify.app.util.AlertHelper;
import com.licensify.app.util.SessionManager;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RequestFormController {

    @FXML private Label lblTitle;
    @FXML private Label lblDetails;
    @FXML private ComboBox<String> comboDuration;
    @FXML private TextArea txtFeatures;
    @FXML private Button btnSubmit;

    private int licenseId = 0;
    private String requestType = "NEW";
    private final LicenseRequestDao requestDao = new LicenseRequestDao();
    private Runnable onSuccess;

    @FXML
    public void initialize() {
        comboDuration.setItems(FXCollections.observableArrayList(
                "1 Month", "2 Months", "4 Months", "1 Year", "2 Years", "4 Years", "Permanent"
        ));
        comboDuration.getSelectionModel().select("1 Year");
    }

    public void setRenewMode(int existingLicenseId, String features) {
        this.licenseId = existingLicenseId;
        this.requestType = "RENEW";
        lblTitle.setText("Renew License");
        lblDetails.setText("Additional Notes");
        txtFeatures.setPromptText("Any additional requests for this renewal...");
        btnSubmit.setText("Submit Renewal");
        if (features != null) {
            txtFeatures.setText(features);
        }
    }

    public void setNewMode() {
        this.licenseId = 0;
        this.requestType = "NEW";
        lblTitle.setText("Request New License");
        lblDetails.setText("Product & Features");
        txtFeatures.setPromptText("Specify the product you want to license, and any specific features...");
        btnSubmit.setText("Submit Request");
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        String duration = comboDuration.getValue();
        String features = txtFeatures.getText().trim();

        if ("NEW".equals(requestType) && features.isEmpty()) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Details", "Please specify the product and features for the new license.");
            return;
        }

        try {
            int userId = SessionManager.getCurrentUser().getId();
            LicenseRequest req = new LicenseRequest(userId, licenseId, requestType, "PENDING", duration, features);
            boolean success = requestDao.createRequest(req);
            
            if (success) {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Success", "Request Submitted", "Your " + requestType.toLowerCase() + " request has been submitted successfully.");
                if (onSuccess != null) {
                    onSuccess.run();
                }
                closeWindow();
            } else {
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Failed", "Submission Failed", "Could not submit your request. You might already have a pending request.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Error", "System Error", "An error occurred while submitting.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) lblTitle.getScene().getWindow();
        stage.close();
    }
}
