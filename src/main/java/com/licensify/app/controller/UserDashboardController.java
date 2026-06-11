package com.licensify.app.controller;

import com.licensify.app.MainApp;
import com.licensify.app.dao.LicenseDao;
import com.licensify.app.dao.LicenseRequestDao;
import com.licensify.app.model.License;
import com.licensify.app.model.LicenseRequest;
import com.licensify.app.model.User;
import com.licensify.app.util.AlertHelper;
import com.licensify.app.util.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserDashboardController {

    @FXML private Label lblUserFullName;
    @FXML private Label lblUserRole;

    @FXML private Label lblStatTotal;
    @FXML private Label lblStatActive;
    @FXML private Label lblStatExpiring;
    @FXML private Label lblStatPending;

    @FXML private BarChart<String, Number> usageBarChart;
    @FXML private PieChart statusPieChart;

    @FXML private TableView<License> recentLicensesTable;
    @FXML private TableColumn<License, String> colLicKey;
    @FXML private TableColumn<License, String> colLicProduct;
    @FXML private TableColumn<License, String> colLicType;
    @FXML private TableColumn<License, String> colLicStatus;
    @FXML private TableColumn<License, String> colLicExpiry;
    @FXML private TableColumn<License, Void> colLicAction;

    // Navigation Buttons
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavLicenses;
    @FXML private Button btnNavRequests;
    @FXML private Button btnNavDownloads;

    // Panels
    @FXML private ScrollPane panelDashboard;
    @FXML private VBox panelMyLicenses;
    @FXML private VBox panelRequests;

    // My Licenses Full Table
    @FXML private TableView<License> myLicensesFullTable;
    @FXML private TableColumn<License, String> colMyLicKey;
    @FXML private TableColumn<License, String> colMyLicProduct;
    @FXML private TableColumn<License, String> colMyLicType;
    @FXML private TableColumn<License, String> colMyLicStatus;
    @FXML private TableColumn<License, String> colMyLicExpiry;
    @FXML private TableColumn<License, Void> colMyLicAction;

    // Renewal Requests Full Table
    @FXML private TableView<LicenseRequest> requestsFullTable;
    @FXML private TableColumn<LicenseRequest, String> colReqId;
    @FXML private TableColumn<LicenseRequest, String> colReqType;
    @FXML private TableColumn<LicenseRequest, String> colReqStatus;
    @FXML private TableColumn<LicenseRequest, String> colReqDate;

    @FXML private VBox timelineContainer;

    private final LicenseDao licenseDao = new LicenseDao();
    private final LicenseRequestDao requestDao = new LicenseRequestDao();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            lblUserFullName.setText(user.getUsername());
            lblUserRole.setText(user.getEmail());
        }

        setupTableColumns();
        loadDashboardData();

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> recentLicensesTable.refresh())
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    private void setupTableColumns() {
        colLicKey.setCellValueFactory(new PropertyValueFactory<>("licenseKey"));
        colLicProduct.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLicType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colLicStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        // We will render Expiry dynamically, so we don't need a property value factory for it.

        colLicKey.setCellFactory(column -> new TableCell<License, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String clean = item.replace("-", "");
                    setText(clean.length() > 8 ? clean.substring(0, 8).toUpperCase() : item);
                }
            }
        });

        colLicStatus.setCellFactory(column -> new TableCell<License, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    badge.getStyleClass().add("badge");
                    if ("ACTIVE".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-active");
                    } else if ("EXPIRED".equalsIgnoreCase(item) || "SUSPENDED".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-expired");
                    } else {
                        badge.getStyleClass().add("badge-expiring");
                    }
                    setGraphic(badge);
                }
            }
        });

        colLicExpiry.setCellFactory(column -> new TableCell<License, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    License lic = getTableRow().getItem();
                    if (lic.getExpiryDate() != null) {
                        long totalSecs = java.time.LocalDateTime.now().until(lic.getExpiryDate().atStartOfDay(), java.time.temporal.ChronoUnit.SECONDS);
                        if (totalSecs > 0) {
                            long d = totalSecs / 86400;
                            long h = (totalSecs % 86400) / 3600;
                            long m = ((totalSecs % 86400) % 3600) / 60;
                            long s = totalSecs % 60;
                            setText(String.format("%dd %02dh %02dm %02ds", d, h, m, s));
                        } else {
                            setText("Expired");
                        }
                    } else {
                        setText("Permanent");
                    }
                }
            }
        });

        colLicAction.setCellFactory(column -> new TableCell<License, Void>() {
            private final Button btnDownload = new Button("Download");
            private final Button btnRenew = new Button("Renew");
            private final HBox pane = new HBox(5);
            {
                btnDownload.getStyleClass().add("btn-action-small");
                btnRenew.getStyleClass().add("btn-action-small");
                btnRenew.setStyle("-fx-text-fill: #f59e0b; -fx-background-color: rgba(245, 158, 11, 0.1); -fx-border-color: rgba(245, 158, 11, 0.3);");
                
                btnDownload.setOnAction(e -> {
                    License lic = getTableRow().getItem();
                    if (lic != null) downloadLicense(lic);
                });
                
                btnRenew.setOnAction(e -> {
                    License lic = getTableRow().getItem();
                    if (lic != null) requestRenewal(lic);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    License lic = getTableRow().getItem();
                    pane.getChildren().clear();
                    if ("EXPIRED".equalsIgnoreCase(lic.getStatus())) {
                        pane.getChildren().add(btnRenew);
                    } else {
                        pane.getChildren().add(btnDownload);
                    }
                    setGraphic(pane);
                }
            }
        });

        // Setup My Licenses Table (Reuse identical factories)
        colMyLicKey.setCellValueFactory(new PropertyValueFactory<>("licenseKey"));
        colMyLicProduct.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMyLicType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMyLicStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        colMyLicKey.setCellFactory(colLicKey.getCellFactory());
        colMyLicStatus.setCellFactory(colLicStatus.getCellFactory());
        colMyLicExpiry.setCellFactory(colLicExpiry.getCellFactory());
        colMyLicAction.setCellFactory(colLicAction.getCellFactory());

        // Setup Renewal Requests Table
        colReqId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReqType.setCellValueFactory(new PropertyValueFactory<>("requestType"));
        colReqStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colReqDate.setCellValueFactory(cellData -> {
            java.sql.Timestamp ts = cellData.getValue().getRequestDate();
            return new javafx.beans.property.SimpleStringProperty(ts != null ? ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "");
        });

        colReqStatus.setCellFactory(column -> new TableCell<LicenseRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    badge.getStyleClass().add("badge");
                    if ("APPROVED".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-active");
                    } else if ("REJECTED".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-expired");
                    } else {
                        badge.getStyleClass().add("badge-expiring");
                    }
                    setGraphic(badge);
                }
            }
        });
    }

    private void requestRenewal(License license) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/licensify/app/view/request_form.fxml"));
            javafx.scene.Parent root = loader.load();
            RequestFormController controller = loader.getController();
            controller.setRenewMode(license.getId(), license.getFeatures());
            controller.setOnSuccess(this::loadDashboardData);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Renew License");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Could not load", "Failed to open request form.");
        }
    }

    private void downloadLicense(License license) {
        if (!"ACTIVE".equalsIgnoreCase(license.getStatus())) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Cannot Download", "License Inactive", "Only active licenses can be downloaded.");
            return;
        }
        try {
            String owner = SessionManager.getCurrentUser().getUsername();
            String signature = com.licensify.app.util.PasswordHasher.hashPassword(license.getLicenseKey() + owner + license.getType());
            
            // Generate JSON
            String jsonContent = "{\n" +
                "  \"licenseKey\": \"" + license.getLicenseKey() + "\",\n" +
                "  \"product\": \"" + license.getName() + "\",\n" +
                "  \"type\": \"" + license.getType() + "\",\n" +
                "  \"owner\": \"" + owner + "\",\n" +
                "  \"issueDate\": \"" + license.getIssueDate() + "\",\n" +
                "  \"expiryDate\": " + (license.getExpiryDate() != null ? "\"" + license.getExpiryDate() + "\"" : "null") + ",\n" +
                "  \"signature\": \"" + signature + "\"\n" +
                "}";
            
            // Generate DAT
            String datContent = "LICENSE_KEY=" + license.getLicenseKey() + "\n" +
                "PRODUCT=" + license.getName() + "\n" +
                "TYPE=" + license.getType() + "\n" +
                "OWNER=" + owner + "\n" +
                "ISSUE_DATE=" + license.getIssueDate() + "\n" +
                "EXPIRY_DATE=" + (license.getExpiryDate() != null ? license.getExpiryDate() : "N/A") + "\n" +
                "SIGNATURE=" + signature;

            java.nio.file.Files.write(java.nio.file.Paths.get("license_" + license.getLicenseKey() + ".json"), jsonContent.getBytes());
            java.nio.file.Files.write(java.nio.file.Paths.get("license_" + license.getLicenseKey() + ".dat"), datContent.getBytes());

            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Success", "License Downloaded", "Files saved successfully as .json and .dat in application folder!");
        } catch (Exception ex) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Download Failed", ex.getMessage());
        }
    }

    private void loadDashboardData() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        List<License> licenses = licenseDao.getAllLicensesByUserId(user.getId());
        List<LicenseRequest> requests = requestDao.getRequestsByUserId(user.getId());

        // 1. Calculate Stats
        int total = licenses.size();
        int active = 0;
        int expiring = 0;
        for (License lic : licenses) {
            if (lic.isActive()) active++;
            if (!lic.isExpired() && !"Permanent".equalsIgnoreCase(lic.getType()) && lic.getExpiryDate() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), lic.getExpiryDate());
                if (days > 0 && days <= 30) expiring++;
            }
        }
        
        int pending = 0;
        for (LicenseRequest req : requests) {
            if ("PENDING".equalsIgnoreCase(req.getStatus())) pending++;
        }

        lblStatTotal.setText(String.valueOf(total));
        lblStatActive.setText(String.valueOf(active));
        lblStatExpiring.setText(String.valueOf(expiring));
        lblStatPending.setText(String.valueOf(pending));

        // 2. Populate Pie Chart
        int expired = total - active;
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data("Active", active),
            new PieChart.Data("Inactive", expired)
        );
        statusPieChart.setData(pieData);

        // 3. Populate Bar Chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        long expiryBased = licenses.stream().filter(l -> "Expiry-Based".equalsIgnoreCase(l.getType())).count();
        long permanent = licenses.stream().filter(l -> "Permanent".equalsIgnoreCase(l.getType())).count();
        long cloud = licenses.stream().filter(l -> "Cloud".equalsIgnoreCase(l.getType())).count();
        
        series.getData().add(new XYChart.Data<>("Expiry", expiryBased));
        series.getData().add(new XYChart.Data<>("Permanent", permanent));
        series.getData().add(new XYChart.Data<>("Cloud", cloud));
        
        usageBarChart.getData().clear();
        usageBarChart.getData().add(series);

        // 4. Populate Table
        recentLicensesTable.setItems(FXCollections.observableArrayList(licenses));
        myLicensesFullTable.setItems(FXCollections.observableArrayList(licenses));
        requestsFullTable.setItems(FXCollections.observableArrayList(requests));

        // 5. Populate Timeline
        timelineContainer.getChildren().clear();
        if (requests.isEmpty()) {
            Label noReq = new Label("No recent activity.");
            noReq.setStyle("-fx-text-fill: #64748b;");
            timelineContainer.getChildren().add(noReq);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            int count = 0;
            for (LicenseRequest req : requests) {
                if (count++ > 4) break; // show top 5

                HBox node = new HBox(15);
                node.getStyleClass().add("timeline-node");
                
                Circle dot = new Circle(5);
                dot.getStyleClass().add("timeline-dot");
                
                VBox textNode = new VBox(2);
                Label title = new Label(req.getRequestType() + " Request " + req.getStatus());
                title.setStyle("-fx-font-weight: 600; -fx-text-fill: #e2e8f0; -fx-font-size: 13px;");
                
                String dateStr = req.getRequestDate() != null ? req.getRequestDate().toLocalDateTime().format(formatter) : "Unknown Date";
                Label desc = new Label(req.getDuration() + " • " + dateStr);
                desc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

                if ("APPROVED".equalsIgnoreCase(req.getStatus())) {
                    dot.setStyle("-fx-fill: #10b981;");
                } else if ("REJECTED".equalsIgnoreCase(req.getStatus())) {
                    dot.setStyle("-fx-fill: #ef4444;");
                } else {
                    dot.setStyle("-fx-fill: #f59e0b;");
                }

                // align dot to top of node
                HBox dotContainer = new HBox(dot);
                dotContainer.setTranslateX(-21); // move left to overlap border
                dotContainer.setTranslateY(5);
                
                textNode.getChildren().addAll(title, desc);
                textNode.setTranslateX(-15);
                
                node.getChildren().addAll(dotContainer, textNode);
                timelineContainer.getChildren().add(node);
            }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.cleanSession();
        try {
            MainApp.setRoot("/com/licensify/app/view/login.fxml");
        } catch (IOException e) {
            System.err.println("Logout failed: " + e.getMessage());
        }
    }

    @FXML
    private void showDashboardPanel(ActionEvent event) {
        Button clickedBtn = (Button) event.getSource();
        String text = clickedBtn.getText().trim();
        
        if ("Downloads".equals(text)) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/licensify/app/view/download_form.fxml"));
                javafx.scene.Parent root = loader.load();
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Download License Files");
                stage.setScene(new javafx.scene.Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Could not load", "Failed to open Downloads panel.");
            }
        } else {
            // Hide all panels
            panelDashboard.setVisible(false);
            panelMyLicenses.setVisible(false);
            panelRequests.setVisible(false);

            // Remove active classes
            if (btnNavDashboard != null) btnNavDashboard.getStyleClass().remove("nav-active");
            if (btnNavLicenses != null) btnNavLicenses.getStyleClass().remove("nav-active");
            if (btnNavRequests != null) btnNavRequests.getStyleClass().remove("nav-active");

            // Show selected panel
            if ("Dashboard".equals(text)) {
                panelDashboard.setVisible(true);
                if (btnNavDashboard != null) btnNavDashboard.getStyleClass().add("nav-active");
            } else if ("My Licenses".equals(text) || "View All".equals(text)) {
                panelMyLicenses.setVisible(true);
                if (btnNavLicenses != null) btnNavLicenses.getStyleClass().add("nav-active");
            } else if ("Renewal Requests".equals(text)) {
                panelRequests.setVisible(true);
                if (btnNavRequests != null) btnNavRequests.getStyleClass().add("nav-active");
            } else {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, text, "Navigation", "You clicked on " + text + ". This feature is integrated directly into the dashboard overview.");
                panelDashboard.setVisible(true);
                if (btnNavDashboard != null) btnNavDashboard.getStyleClass().add("nav-active");
            }
        }
    }

    @FXML
    private void handleRequestNewLicense(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/licensify/app/view/request_form.fxml"));
            javafx.scene.Parent root = loader.load();
            RequestFormController controller = loader.getController();
            controller.setNewMode();
            controller.setOnSuccess(this::loadDashboardData);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Request New License");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Could not load", "Failed to open request form.");
        }
    }
}
