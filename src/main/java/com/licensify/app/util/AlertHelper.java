package com.licensify.app.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import java.util.Optional;

/**
 * Utility class to display beautiful styled popups and alert boxes.
 */
public class AlertHelper {

    /**
     * Shows a message alert with styled CSS.
     */
    public static void showAlert(AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog with YES/NO options.
     * 
     * @return true if YES is clicked, false otherwise.
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        ButtonType btnYes = new ButtonType("Yes");
        ButtonType btnNo = new ButtonType("No");
        alert.getButtonTypes().setAll(btnYes, btnNo);

        styleAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnYes;
    }

    /**
     * Applies styling to the alert's dialog pane.
     */
    private static void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        try {
            // Apply custom CSS
            String cssPath = AlertHelper.class.getResource("/com/licensify/app/css/styles.css").toExternalForm();
            dialogPane.getStylesheets().add(cssPath);
            dialogPane.getStyleClass().add("custom-alert");
        } catch (Exception e) {
            // Fallback if resource is not loaded yet (e.g. tests or build issues)
            System.err.println("Could not load styles.css for alert: " + e.getMessage());
        }
    }
}
