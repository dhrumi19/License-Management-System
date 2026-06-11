package com.licensify.app.controller;

import com.licensify.app.dao.LicenseDao;
import com.licensify.app.model.License;
import com.licensify.app.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the Download License popup dialog.
 */
public class DownloadFormController {

    @FXML private ComboBox<License> licenseComboBox;
    @FXML private CheckBox chkDat;
    @FXML private CheckBox chkJson;
    @FXML private Button downloadBtn;

    private final LicenseDao licenseDao = new LicenseDao();

    @FXML
    public void initialize() {
        // Load all licenses
        List<License> list = licenseDao.getAllLicenses();
        licenseComboBox.setItems(FXCollections.observableArrayList(list));

        // Format ComboBox cell rendering matching screenshot
        licenseComboBox.setCellFactory(lv -> new ListCell<License>() {
            @Override
            protected void updateItem(License item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String cleanKey = item.getLicenseKey().replace("-", "");
                    String shortKey = cleanKey.length() > 8 ? cleanKey.substring(3, 11).toUpperCase() : item.getLicenseKey();
                    setText("[" + shortKey + "] " + item.getName() + " — " + (item.isActive() ? "Active" : "Expired"));
                }
            }
        });
        licenseComboBox.setButtonCell(licenseComboBox.getCellFactory().call(null));
    }

    public void setSelectedLicense(License license) {
        if (license != null) {
            for (License l : licenseComboBox.getItems()) {
                if (l.getId() == license.getId()) {
                    licenseComboBox.setValue(l);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleDownload(ActionEvent event) {
        License selected = licenseComboBox.getValue();
        if (selected == null) {
            AlertHelper.showAlert(AlertType.ERROR, "Download Error", "No Licence Selected", "Please select a license to download.");
            return;
        }

        if (!chkDat.isSelected() && !chkJson.isSelected()) {
            AlertHelper.showAlert(AlertType.ERROR, "Download Error", "No Formats Selected", "Please check at least one format (.dat or .json) to download.");
            return;
        }

        // Choose save directory
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Save Location");
        Stage stage = (Stage) downloadBtn.getScene().getWindow();
        File selectedDir = dirChooser.showDialog(stage);

        if (selectedDir != null) {
            String cleanKey = selected.getLicenseKey().replace("-", "");
            String fileId = cleanKey.length() > 8 ? cleanKey.substring(3, 11).toUpperCase() : selected.getLicenseKey();
            
            boolean datSuccess = true;
            boolean jsonSuccess = true;
            StringBuilder successMessage = new StringBuilder("Successfully exported files:\n");

            // 1. Save DAT
            if (chkDat.isSelected()) {
                File datFile = new File(selectedDir, fileId + ".dat");
                String datContent = "# Licensify License Data File\n"
                                  + "license_id=" + fileId + "\n"
                                  + "license_key=" + selected.getLicenseKey() + "\n"
                                  + "name=" + selected.getName() + "\n"
                                  + "type=" + selected.getType() + "\n"
                                  + "dimension=" + selected.getDimension() + "\n"
                                  + "cost=" + String.format("%.2f", selected.getCost()) + "\n"
                                  + "issue_date=" + (selected.getIssueDate() != null ? selected.getIssueDate().toString() : LocalDate.now().toString()) + "\n"
                                  + "expiry_date=" + (selected.getExpiryDate() != null ? selected.getExpiryDate().toString() : "2099-12-31") + "\n"
                                  + "status=" + selected.getStatus() + "\n"
                                  + "features=" + (selected.getFeatures() != null ? selected.getFeatures() : "") + "\n"
                                  + "checksum=" + Integer.toHexString(selected.getLicenseKey().hashCode()).toUpperCase();
                
                try (FileWriter writer = new FileWriter(datFile)) {
                    writer.write(datContent);
                    successMessage.append("- ").append(datFile.getName()).append("\n");
                } catch (IOException e) {
                    datSuccess = false;
                    System.err.println("Error saving DAT file: " + e.getMessage());
                }
            }

            // 2. Save JSON
            if (chkJson.isSelected()) {
                File jsonFile = new File(selectedDir, fileId + ".json");
                String jsonContent = "{\n"
                                   + "  \"licenseId\": \"" + fileId + "\",\n"
                                   + "  \"licenseKey\": \"" + selected.getLicenseKey() + "\",\n"
                                   + "  \"name\": \"" + selected.getName() + "\",\n"
                                   + "  \"type\": \"" + selected.getType() + "\",\n"
                                   + "  \"dimension\": \"" + selected.getDimension() + "\",\n"
                                   + "  \"cost\": " + String.format("%.2f", selected.getCost()) + ",\n"
                                   + "  \"issueDate\": \"" + (selected.getIssueDate() != null ? selected.getIssueDate().toString() : LocalDate.now().toString()) + "\",\n"
                                   + "  \"expiryDate\": \"" + (selected.getExpiryDate() != null ? selected.getExpiryDate().toString() : "2099-12-31") + "\",\n"
                                   + "  \"status\": \"" + selected.getStatus() + "\",\n"
                                   + "  \"features\": \"" + (selected.getFeatures() != null ? selected.getFeatures() : "") + "\"\n"
                                   + "}";
                
                try (FileWriter writer = new FileWriter(jsonFile)) {
                    writer.write(jsonContent);
                    successMessage.append("- ").append(jsonFile.getName()).append("\n");
                } catch (IOException e) {
                    jsonSuccess = false;
                    System.err.println("Error saving JSON file: " + e.getMessage());
                }
            }

            if (datSuccess && jsonSuccess) {
                AlertHelper.showAlert(AlertType.INFORMATION, "Export Successful", "Files Saved", successMessage.toString());
                closeWindow();
            } else {
                AlertHelper.showAlert(AlertType.ERROR, "Export Error", "Write Failure", "Failed to write files to selected location. Check folder permissions.");
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) downloadBtn.getScene().getWindow();
        stage.close();
    }
}
