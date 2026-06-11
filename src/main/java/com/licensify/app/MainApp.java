package com.licensify.app;

import com.licensify.app.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Main application bootstrap for the Licensify License Management System.
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        
        // 1. Initialize SQLite Database (creates tables & seeds defaults)
        DatabaseConnection.initializeDatabase();

        // 2. Set up initial Login Scene
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/licensify/app/view/login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 800, 600);
            
            stage.setTitle("Licensify - Secure License Manager");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            System.err.println("Fatal: Could not load login.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigation helper to change root views and dynamically resize the window.
     * 
     * @param fxml Absolute path to the FXML resource file.
     * @throws IOException if loading the FXML fails.
     */
    public static void setRoot(String fxml) throws IOException {
        Parent root = FXMLLoader.load(MainApp.class.getResource(fxml));
        
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        // Adjust dimensions based on the panel loaded
        if (fxml.contains("login.fxml")) {
            primaryStage.setWidth(800);
            primaryStage.setHeight(620);
        } else if (fxml.contains("admin_dashboard.fxml")) {
            primaryStage.setWidth(1020);
            primaryStage.setHeight(740);
        } else if (fxml.contains("user_dashboard.fxml")) {
            primaryStage.setWidth(1070);
            primaryStage.setHeight(740);
        }
        
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
