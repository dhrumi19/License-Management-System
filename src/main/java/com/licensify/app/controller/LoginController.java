package com.licensify.app.controller;

import com.licensify.app.MainApp;
import com.licensify.app.dao.LicenseDao;
import com.licensify.app.dao.UserDao;
import com.licensify.app.model.License;
import com.licensify.app.model.User;
import com.licensify.app.util.AlertHelper;
import com.licensify.app.util.PasswordHasher;
import com.licensify.app.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

/**
 * Controller class for the Login view.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final UserDao userDao = new UserDao();
    private final LicenseDao licenseDao = new LicenseDao();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password cannot be empty.");
            return;
        }

        // Fetch user
        User user = userDao.getUserByUsername(username);
        if (user == null) {
            showError("Invalid username or password.");
            return;
        }

        // Verify password
        if (!PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
            showError("Invalid username or password.");
            return;
        }

        // Set up session
        SessionManager.setCurrentUser(user);

        try {
            if (user.isAdmin()) {
                // Route to Admin Dashboard
                MainApp.setRoot("/com/licensify/app/view/admin_dashboard.fxml");
            } else {
                // Route to User Dashboard after loading their license
                License license = licenseDao.getLicenseByUserId(user.getId());
                SessionManager.setActiveLicense(license);
                
                MainApp.setRoot("/com/licensify/app/view/user_dashboard.fxml");
                
                // Show warning popup if license is expired
                if (license == null) {
                    AlertHelper.showAlert(AlertType.WARNING, "No License", "No License Assigned", 
                            "You do not have any assigned license. Please contact the administrator.");
                } else if (license.isExpired()) {
                    AlertHelper.showAlert(AlertType.ERROR, "License Expired", "License Expired", 
                            "Your license has expired. Please renew or apply for a new license.");
                } else if (license.isSuspended()) {
                    AlertHelper.showAlert(AlertType.ERROR, "License Suspended", "License Suspended", 
                            "Your license has been suspended. Please contact the administrator.");
                }
            }
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
            e.printStackTrace();
            showError("An error occurred during navigation: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
