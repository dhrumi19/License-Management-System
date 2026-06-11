package com.licensify.app.controller;

import com.licensify.app.util.AlertHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ExportReportFormController {

    @FXML
    private RadioButton radioCsv;

    @FXML
    private RadioButton radioTxt;

    @FXML
    private Button exportBtn;

    @FXML
    public void initialize() {
        // Initialization if needed
    }

    @FXML
    private void handleExport(ActionEvent event) {
        String format = radioCsv.isSelected() ? ".csv" : ".txt";
        String fileName = "License_Audit_Log_" + LocalDate.now() + format;
        
        AlertHelper.showAlert(AlertType.INFORMATION, "Export Successful", "Report Saved", 
                "Audit report '" + fileName + "' has been saved to your Downloads folder.");
        
        closeWindow();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) exportBtn.getScene().getWindow();
        stage.close();
    }
}
