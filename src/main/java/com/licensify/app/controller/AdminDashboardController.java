package com.licensify.app.controller;

import com.licensify.app.MainApp;
import com.licensify.app.dao.LicenseDao;
import com.licensify.app.dao.TransactionDao;
import com.licensify.app.dao.FeatureLicenseDao;
import com.licensify.app.dao.LicenseRequestDao;
import com.licensify.app.dao.UserDao;
import com.licensify.app.model.License;
import com.licensify.app.model.Transaction;
import com.licensify.app.model.FeatureLicense;
import com.licensify.app.model.LicenseRequest;
import com.licensify.app.model.User;
import com.licensify.app.util.AlertHelper;
import com.licensify.app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.Optional;
import com.licensify.app.util.PasswordHasher;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AdminDashboardController {

    // Navigation
    @FXML private Button btnNavOverview;
    @FXML private Button btnNavUsers;
    @FXML private Button btnNavMainLicense;
    @FXML private Button btnNavFeatureLicense;
    @FXML private Button btnNavHardware;
    @FXML private Button btnNavLicenseRequests;
    @FXML private Button btnNavAuditLogs;

    // Panels
    @FXML private ScrollPane panelOverview;
    @FXML private VBox panelUsers;
    @FXML private VBox panelMainLicense;
    @FXML private VBox panelFeatureLicense;
    @FXML private VBox panelHardware;
    @FXML private VBox panelLicenseRequests;
    @FXML private VBox panelAuditLogs;

    // Overview / Analytics
    @FXML private Label adminWelcomeLabel;
    @FXML private Label lblStatRevenue;
    @FXML private Label totalLicensesLabel;
    @FXML private Label activeLicensesLabel;
    @FXML private Label inactiveLicensesLabel;
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private PieChart licensePieChart;

    // User Management
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TableColumn<User, String> colUserStatus;
    @FXML private TableColumn<User, String> colUserCreated;
    @FXML private TableColumn<User, Void> colUserActions;

    // License Registry
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private Label lblRecordCount;
    @FXML private TableView<License> licenseTable;
    @FXML private TableColumn<License, String> colKey;
    @FXML private TableColumn<License, String> colLicUsername;
    @FXML private TableColumn<License, String> colName;
    @FXML private TableColumn<License, String> colType;
    @FXML private TableColumn<License, String> colDimension;
    @FXML private TableColumn<License, Double> colCost;
    @FXML private TableColumn<License, String> colStatus;
    @FXML private TableColumn<License, String> colFeatures;
    @FXML private TableColumn<License, String> colIssueDate;
    @FXML private TableColumn<License, String> colExpiryDate;
    @FXML private TableColumn<License, Void> colLicActions;

    // Feature License
    @FXML private ComboBox<License> featureLicenseComboBox;
    @FXML private TableView<FeatureLicense> featureTable;
    @FXML private TableColumn<FeatureLicense, String> colFeatId;
    @FXML private TableColumn<FeatureLicense, String> colFeatName;
    @FXML private TableColumn<FeatureLicense, String> colFeatStatus;
    @FXML private TableColumn<FeatureLicense, String> colFeatType;
    @FXML private TableColumn<FeatureLicense, String> colFeatExpiry;
    @FXML private TableColumn<FeatureLicense, Void> colFeatActions;

    // License Requests
    @FXML private TableView<LicenseRequest> licenseRequestsTable;
    @FXML private TableColumn<LicenseRequest, String> colReqId;
    @FXML private TableColumn<LicenseRequest, String> colReqUserId;
    @FXML private TableColumn<LicenseRequest, String> colReqType;
    @FXML private TableColumn<LicenseRequest, String> colReqDuration;
    @FXML private TableColumn<LicenseRequest, String> colReqDate;
    @FXML private TableColumn<LicenseRequest, String> colReqStatus;
    @FXML private TableColumn<LicenseRequest, Void> colReqActions;

    // Audit Logs
    @FXML private TableView<Transaction> auditTable;
    @FXML private TableColumn<Transaction, Integer> colTxId;
    @FXML private TableColumn<Transaction, Timestamp> colTxDate;
    @FXML private TableColumn<Transaction, String> colTxKey;
    @FXML private TableColumn<Transaction, String> colTxAction;
    @FXML private TableColumn<Transaction, String> colTxDetails;

    private final LicenseDao licenseDao = new LicenseDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private final FeatureLicenseDao featureLicenseDao = new FeatureLicenseDao();
    private final LicenseRequestDao requestDao = new LicenseRequestDao();
    private final UserDao userDao = new UserDao();
    
    private ObservableList<License> masterData = FXCollections.observableArrayList();
    private FilteredList<License> filteredData;

    private ObservableList<FeatureLicense> featureMasterData = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            adminWelcomeLabel.setText("System Admin: " + SessionManager.getCurrentUser().getUsername());
        }

        setupAnalyticsData();
        setupUserManagementTable();
        setupLicenseRegistryTable();
        setupFeatureLicenseTable();
        setupLicenseRequestsTable();
        setupAuditLogsTable();

        showOverviewPanel(null);
    }

    private void setupAnalyticsData() {
        List<License> list = licenseDao.getAllLicenses();
        int total = list.size();
        int active = 0;
        int inactive = 0;
        int expiryCount = 0;
        int permanentCount = 0;
        int cloudCount = 0;

        for (License l : list) {
            if (l.isActive()) active++; else inactive++;
            
            if ("Expiry-Based".equalsIgnoreCase(l.getType())) expiryCount++;
            else if ("Permanent".equalsIgnoreCase(l.getType())) permanentCount++;
            else if ("Cloud".equalsIgnoreCase(l.getType())) cloudCount++;
        }

        totalLicensesLabel.setText(String.valueOf(total));
        activeLicensesLabel.setText(String.valueOf(active));
        inactiveLicensesLabel.setText(String.valueOf(inactive));
        lblStatRevenue.setText(String.format("₹%,.0f", active * 850.50)); // Simulated MRR based on active keys

        // Pie Chart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data("Expiry-Based", expiryCount),
            new PieChart.Data("Permanent", permanentCount),
            new PieChart.Data("Cloud (SaaS)", cloudCount)
        );
        licensePieChart.setData(pieData);

        // Simulated Line Chart for past 6 months
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Jan", 32000));
        series.getData().add(new XYChart.Data<>("Feb", 34500));
        series.getData().add(new XYChart.Data<>("Mar", 33000));
        series.getData().add(new XYChart.Data<>("Apr", 38000));
        series.getData().add(new XYChart.Data<>("May", 41200));
        series.getData().add(new XYChart.Data<>("Jun", active * 850.50));
        revenueChart.getData().clear();
        revenueChart.getData().add(series);
    }

    private void setupUserManagementTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        colUserStatus.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item.toUpperCase());
                    label.getStyleClass().add("badge");
                    if ("ACTIVE".equalsIgnoreCase(item)) label.getStyleClass().add("badge-active");
                    else label.getStyleClass().add("badge-expired");
                    setGraphic(label);
                }
            }
        });

        colUserCreated.setCellValueFactory(cellData -> {
            java.sql.Timestamp ts = cellData.getValue().getCreatedAt();
            return new javafx.beans.property.SimpleStringProperty(ts != null ? ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "N/A");
        });

        colUserActions.setCellFactory(column -> new TableCell<User, Void>() {
            private final Button btnSuspend = new Button();
            {
                btnSuspend.getStyleClass().add("btn-action-small");
                btnSuspend.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
                        userDao.updateUserStatus(user.getId(), "SUSPENDED");
                        user.setStatus("SUSPENDED");
                    } else {
                        userDao.updateUserStatus(user.getId(), "ACTIVE");
                        user.setStatus("ACTIVE");
                    }
                    usersTable.refresh();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if ("SUSPENDED".equalsIgnoreCase(user.getStatus())) {
                        btnSuspend.setText("Activate");
                        btnSuspend.setStyle("-fx-text-fill: #10b981; -fx-background-color: rgba(16, 185, 129, 0.1);");
                    } else {
                        btnSuspend.setText("Suspend");
                        btnSuspend.setStyle("-fx-text-fill: #ef4444; -fx-background-color: rgba(239, 68, 68, 0.1);");
                    }
                    setGraphic(btnSuspend);
                }
            }
        });

        usersTable.setItems(FXCollections.observableArrayList(userDao.getAllStandardUsers()));
    }

    private void setupLicenseRegistryTable() {
        colKey.setCellValueFactory(new PropertyValueFactory<>("licenseKey"));
        colLicUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDimension.setCellValueFactory(new PropertyValueFactory<>("dimension"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colFeatures.setCellValueFactory(new PropertyValueFactory<>("features"));
        colIssueDate.setCellValueFactory(new PropertyValueFactory<>("formattedCreatedDate"));
        colExpiryDate.setCellValueFactory(new PropertyValueFactory<>("formattedExpiryDate"));

        colCost.setCellFactory(column -> new TableCell<License, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("₹%,.2f", item));
                }
            }
        });

        colKey.setCellFactory(column -> new TableCell<License, String>() {
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

        colStatus.setCellFactory(column -> new TableCell<License, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item.toUpperCase());
                    label.getStyleClass().add("badge");
                    if ("ACTIVE".equalsIgnoreCase(item)) label.getStyleClass().add("badge-active");
                    else if ("EXPIRED".equalsIgnoreCase(item)) label.getStyleClass().add("badge-expired");
                    else label.getStyleClass().add("badge-expiring");
                    setGraphic(label);
                }
            }
        });

        colLicActions.setCellFactory(column -> new TableCell<License, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDel = new Button("Delete");
            private final HBox pane = new HBox(5, btnEdit, btnDel);
            {
                btnEdit.getStyleClass().add("btn-action-small");
                btnDel.getStyleClass().add("btn-action-small");
                btnDel.setStyle("-fx-text-fill: #ef4444; -fx-background-color: rgba(239, 68, 68, 0.1);");

                btnEdit.setOnAction(e -> {
                    License lic = getTableView().getItems().get(getIndex());
                    openLicenseForm(lic);
                });
                btnDel.setOnAction(e -> {
                    License lic = getTableView().getItems().get(getIndex());
                    if (AlertHelper.showConfirmation("Delete License", "Are you sure?", "Delete license: " + lic.getLicenseKey())) {
                        licenseDao.deleteLicense(lic.getId());
                        loadLicenseRegistryData();
                        setupAnalyticsData();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        statusFilterComboBox.setItems(FXCollections.observableArrayList("All Statuses", "ACTIVE", "EXPIRED", "SUSPENDED"));
        statusFilterComboBox.setValue("All Statuses");

        loadLicenseRegistryData();

        filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldV, newV) -> updateLicenseFilter());
        statusFilterComboBox.valueProperty().addListener((obs, oldV, newV) -> updateLicenseFilter());
        licenseTable.setItems(filteredData);
    }

    private void updateLicenseFilter() {
        String search = searchField.getText().toLowerCase().trim();
        String status = statusFilterComboBox.getValue();
        filteredData.setPredicate(lic -> {
            if (status != null && !"All Statuses".equals(status) && !lic.getStatus().equalsIgnoreCase(status)) return false;
            if (!search.isEmpty() && !lic.getLicenseKey().toLowerCase().contains(search) && (lic.getName() == null || !lic.getName().toLowerCase().contains(search))) return false;
            return true;
        });
    }

    private void loadLicenseRegistryData() {
        List<License> list = licenseDao.getAllLicenses();
        masterData.setAll(list);
        lblRecordCount.setText(list.size() + " records");

        featureLicenseComboBox.setItems(FXCollections.observableArrayList(list));
        featureLicenseComboBox.setCellFactory(lv -> new ListCell<License>() {
            @Override
            protected void updateItem(License item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "[" + item.getLicenseKey().substring(0,8) + "] " + item.getName());
            }
        });
        featureLicenseComboBox.setButtonCell(featureLicenseComboBox.getCellFactory().call(null));
    }

    private void setupFeatureLicenseTable() {
        colFeatId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFeatName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colFeatStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colFeatType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colFeatExpiry.setCellValueFactory(new PropertyValueFactory<>("expiry"));

        colFeatStatus.setCellFactory(column -> new TableCell<FeatureLicense, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().add("badge");
                    if ("Enabled".equalsIgnoreCase(item)) lbl.getStyleClass().add("badge-active");
                    else lbl.getStyleClass().add("badge-expired");
                    setGraphic(lbl);
                }
            }
        });

        colFeatActions.setCellFactory(column -> new TableCell<FeatureLicense, Void>() {
            private final Button btnToggle = new Button("Toggle");
            {
                btnToggle.getStyleClass().add("btn-action-small");
                btnToggle.setOnAction(e -> {
                    FeatureLicense feat = getTableView().getItems().get(getIndex());
                    feat.setStatus("Enabled".equals(feat.getStatus()) ? "Disabled" : "Enabled");
                    featureLicenseDao.updateFeature(feat);
                    featureTable.refresh();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnToggle);
            }
        });

        featureTable.setItems(featureMasterData);

        featureLicenseComboBox.valueProperty().addListener((obs, oldLic, newLic) -> {
            if (newLic != null) {
                featureMasterData.setAll(featureLicenseDao.getFeaturesByLicenseKey(newLic.getLicenseKey()));
            } else {
                featureMasterData.clear();
            }
        });
    }

    private void setupLicenseRequestsTable() {
        colReqId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReqUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colReqType.setCellValueFactory(new PropertyValueFactory<>("requestType"));
        colReqDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colReqDate.setCellValueFactory(cellData -> {
            java.sql.Timestamp ts = cellData.getValue().getRequestDate();
            return new javafx.beans.property.SimpleStringProperty(ts != null ? ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        });
        colReqStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colReqStatus.setCellFactory(column -> new TableCell<LicenseRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item.toUpperCase());
                    label.getStyleClass().add("badge");
                    if ("APPROVED".equalsIgnoreCase(item)) label.getStyleClass().add("badge-active");
                    else if ("REJECTED".equalsIgnoreCase(item)) label.getStyleClass().add("badge-expired");
                    else label.getStyleClass().add("badge-expiring");
                    setGraphic(label);
                }
            }
        });

        colReqActions.setCellFactory(column -> new TableCell<LicenseRequest, Void>() {
            private final Button btnApprove = new Button("Approve");
            private final Button btnReject = new Button("Reject");
            private final HBox pane = new HBox(5, btnApprove, btnReject);
            {
                btnApprove.getStyleClass().add("btn-action-small");
                btnReject.getStyleClass().add("btn-action-small");
                btnReject.setStyle("-fx-text-fill: #ef4444; -fx-background-color: rgba(239, 68, 68, 0.1);");

                btnApprove.setOnAction(e -> {
                    LicenseRequest req = getTableView().getItems().get(getIndex());
                    req.setStatus("APPROVED");
                    requestDao.updateRequestStatus(req.getId(), "APPROVED");
                    
                    if ("RENEW".equalsIgnoreCase(req.getRequestType())) {
                        License lic = masterData.stream().filter(l -> l.getId() == req.getLicenseId()).findFirst().orElse(null);
                        if (lic != null) {
                            lic.setStatus("ACTIVE");
                            LocalDate baseDate = lic.getExpiryDate() != null && lic.getExpiryDate().isAfter(LocalDate.now()) ? lic.getExpiryDate() : LocalDate.now();
                            
                            String dur = req.getDuration();
                            if ("1 Month".equalsIgnoreCase(dur)) {
                                lic.setExpiryDate(baseDate.plus(1, java.time.temporal.ChronoUnit.MONTHS));
                            } else if ("2 Months".equalsIgnoreCase(dur)) {
                                lic.setExpiryDate(baseDate.plus(2, java.time.temporal.ChronoUnit.MONTHS));
                            } else if ("4 Months".equalsIgnoreCase(dur)) {
                                lic.setExpiryDate(baseDate.plus(4, java.time.temporal.ChronoUnit.MONTHS));
                            } else if ("1 Year".equalsIgnoreCase(dur)) {
                                lic.setExpiryDate(baseDate.plus(1, java.time.temporal.ChronoUnit.YEARS));
                            } else if ("2 Years".equalsIgnoreCase(dur)) {
                                lic.setExpiryDate(baseDate.plus(2, java.time.temporal.ChronoUnit.YEARS));
                            } else if ("4 Years".equalsIgnoreCase(dur)) {
                                lic.setExpiryDate(baseDate.plus(4, java.time.temporal.ChronoUnit.YEARS));
                            } else if ("Permanent".equalsIgnoreCase(dur)) {
                                lic.setExpiryDate(null);
                            } else {
                                lic.setExpiryDate(baseDate.plus(1, java.time.temporal.ChronoUnit.YEARS));
                            }
                            
                            licenseDao.updateLicense(lic);
                            loadLicenseRegistryData();
                            setupAnalyticsData();
                        }
                    } else if ("NEW".equalsIgnoreCase(req.getRequestType())) {
                        // Extract product name from features or provide a default
                        String features = req.getFeatures();
                        String productName = "Requested Product";
                        if (features != null && !features.isEmpty()) {
                            // simple fallback: use first line or part of features as product name
                            productName = features.split("\n")[0];
                            if (productName.length() > 30) {
                                productName = productName.substring(0, 30) + "...";
                            }
                        }
                        
                        License newLic = new License();
                        newLic.setUserId(req.getUserId());
                        newLic.setName(productName);
                        newLic.setType("STANDARD");
                        newLic.setDimension("Unlimited");
                        newLic.setCost(0.0);
                        newLic.setStatus("ACTIVE");
                        newLic.setFeatures(req.getFeatures());
                        // Generate a simple key
                        newLic.setLicenseKey("LIC-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                        newLic.setIssueDate(LocalDate.now());
                        
                        String dur = req.getDuration();
                        LocalDate baseDate = LocalDate.now();
                        if ("1 Month".equalsIgnoreCase(dur)) {
                            newLic.setExpiryDate(baseDate.plus(1, java.time.temporal.ChronoUnit.MONTHS));
                        } else if ("2 Months".equalsIgnoreCase(dur)) {
                            newLic.setExpiryDate(baseDate.plus(2, java.time.temporal.ChronoUnit.MONTHS));
                        } else if ("4 Months".equalsIgnoreCase(dur)) {
                            newLic.setExpiryDate(baseDate.plus(4, java.time.temporal.ChronoUnit.MONTHS));
                        } else if ("1 Year".equalsIgnoreCase(dur)) {
                            newLic.setExpiryDate(baseDate.plus(1, java.time.temporal.ChronoUnit.YEARS));
                        } else if ("2 Years".equalsIgnoreCase(dur)) {
                            newLic.setExpiryDate(baseDate.plus(2, java.time.temporal.ChronoUnit.YEARS));
                        } else if ("4 Years".equalsIgnoreCase(dur)) {
                            newLic.setExpiryDate(baseDate.plus(4, java.time.temporal.ChronoUnit.YEARS));
                        } else if ("Permanent".equalsIgnoreCase(dur)) {
                            newLic.setExpiryDate(null);
                        } else {
                            newLic.setExpiryDate(baseDate.plus(1, java.time.temporal.ChronoUnit.YEARS));
                        }
                        
                        licenseDao.createLicense(newLic);
                        loadLicenseRegistryData();
                        setupAnalyticsData();
                    }
                    
                    licenseRequestsTable.refresh();
                });
                btnReject.setOnAction(e -> {
                    LicenseRequest req = getTableView().getItems().get(getIndex());
                    req.setStatus("REJECTED");
                    requestDao.updateRequestStatus(req.getId(), "REJECTED");
                    licenseRequestsTable.refresh();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        licenseRequestsTable.setItems(FXCollections.observableArrayList(requestDao.getAllRequests()));
    }

    private void setupAuditLogsTable() {
        colTxId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTxDate.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        colTxKey.setCellValueFactory(new PropertyValueFactory<>("licenseKey"));
        colTxAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colTxDetails.setCellValueFactory(new PropertyValueFactory<>("details"));

        colTxDate.setCellFactory(column -> new TableCell<Transaction, Timestamp>() {
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        });

        auditTable.setItems(FXCollections.observableArrayList(transactionDao.getAllTransactions()));
    }

    private void hideAllPanels() {
        panelOverview.setVisible(false);
        panelUsers.setVisible(false);
        panelMainLicense.setVisible(false);
        panelFeatureLicense.setVisible(false);
        panelHardware.setVisible(false);
        panelLicenseRequests.setVisible(false);
        panelAuditLogs.setVisible(false);

        btnNavOverview.getStyleClass().removeAll("nav-active");
        btnNavUsers.getStyleClass().removeAll("nav-active");
        btnNavMainLicense.getStyleClass().removeAll("nav-active");
        btnNavFeatureLicense.getStyleClass().removeAll("nav-active");
        btnNavHardware.getStyleClass().removeAll("nav-active");
        btnNavLicenseRequests.getStyleClass().removeAll("nav-active");
        btnNavAuditLogs.getStyleClass().removeAll("nav-active");
    }

    @FXML private void showOverviewPanel(ActionEvent event) {
        hideAllPanels();
        panelOverview.setVisible(true);
        btnNavOverview.getStyleClass().add("nav-active");
        setupAnalyticsData();
    }

    @FXML private void showUsersPanel(ActionEvent event) {
        hideAllPanels();
        panelUsers.setVisible(true);
        btnNavUsers.getStyleClass().add("nav-active");
        usersTable.setItems(FXCollections.observableArrayList(userDao.getAllStandardUsers()));
    }

    @FXML private void showMainLicensePanel(ActionEvent event) {
        hideAllPanels();
        panelMainLicense.setVisible(true);
        btnNavMainLicense.getStyleClass().add("nav-active");
        loadLicenseRegistryData();
    }

    @FXML private void showFeaturePanel(ActionEvent event) {
        hideAllPanels();
        panelFeatureLicense.setVisible(true);
        btnNavFeatureLicense.getStyleClass().add("nav-active");
    }

    @FXML private void showHardwarePanel(ActionEvent event) {
        hideAllPanels();
        panelHardware.setVisible(true);
        btnNavHardware.getStyleClass().add("nav-active");
    }

    @FXML private void showLicenseRequestsPanel(ActionEvent event) {
        hideAllPanels();
        panelLicenseRequests.setVisible(true);
        btnNavLicenseRequests.getStyleClass().add("nav-active");
        licenseRequestsTable.setItems(FXCollections.observableArrayList(requestDao.getAllRequests()));
    }

    @FXML private void showAuditLogsPanel(ActionEvent event) {
        hideAllPanels();
        panelAuditLogs.setVisible(true);
        btnNavAuditLogs.getStyleClass().add("nav-active");
        auditTable.setItems(FXCollections.observableArrayList(transactionDao.getAllTransactions()));
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.cleanSession();
        try {
            MainApp.setRoot("/com/licensify/app/view/login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateLicense(ActionEvent event) {
        openLicenseForm(null);
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Enter new user details:");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                User user = new User();
                user.setUsername(usernameField.getText());
                user.setEmail(emailField.getText());
                user.setPasswordHash(PasswordHasher.hashPassword(passwordField.getText()));
                user.setRole("USER");
                user.setStatus("ACTIVE");
                return user;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            if (userDao.createUser(user)) {
                usersTable.setItems(FXCollections.observableArrayList(userDao.getAllStandardUsers()));
                AlertHelper.showAlert(AlertType.INFORMATION, "Success", "User Created", "New user has been created successfully.");
            } else {
                AlertHelper.showAlert(AlertType.ERROR, "Error", "Failed to create user", "Username or email might already exist.");
            }
        });
    }

    @FXML
    private void handleAddFeature(ActionEvent event) {
        License selectedLic = featureLicenseComboBox.getValue();
        if (selectedLic == null) {
            AlertHelper.showAlert(AlertType.WARNING, "No License", "No License Selected", "Please select a license to add a feature to.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/licensify/app/view/feature_form.fxml"));
            Parent root = loader.load();
            FeatureFormController controller = loader.getController();
            controller.setFeature(null, selectedLic.getLicenseKey());
            Stage stage = new Stage();
            stage.setTitle("Add Feature");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            featureMasterData.setAll(featureLicenseDao.getFeaturesByLicenseKey(selectedLic.getLicenseKey()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openLicenseForm(License license) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/licensify/app/view/license_form.fxml"));
            Parent root = loader.load();
            LicenseFormController controller = loader.getController();
            if (license != null) controller.setLicense(license);
            Stage stage = new Stage();
            stage.setTitle(license == null ? "Create License" : "Edit License");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadLicenseRegistryData();
            setupAnalyticsData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMockDownload(ActionEvent event) {
        AlertHelper.showAlert(AlertType.INFORMATION, "Export CSV", "Exporting Data", "Downloading registry data to CSV.");
    }
}
