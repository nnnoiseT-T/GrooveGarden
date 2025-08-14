package com.groovegarden;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.groovegarden.config.AppConfig;
import com.groovegarden.util.ErrorHandler;
import com.groovegarden.util.MemoryMonitor;

import java.util.logging.Logger;

public class MainApp extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Initialize configuration
            AppConfig.initialize();
            
            // Start memory monitoring
            MemoryMonitor.startMonitoring();
            
            // Load FXML
            Parent root = FXMLLoader.load(getClass().getResource("/ui/main.fxml"));
            
            // Set up stage with configuration values
            primaryStage.setTitle("Groove Garden - Algorithmic Composition");
            primaryStage.setScene(new Scene(root, 
                AppConfig.getInt("ui.window.width"), 
                AppConfig.getInt("ui.window.height")));
            primaryStage.setMinWidth(AppConfig.getInt("ui.window.width"));
            primaryStage.setMinHeight(AppConfig.getInt("ui.window.height"));
            
            // Set up close handler
            primaryStage.setOnCloseRequest(event -> {
                MemoryMonitor.stopMonitoring();
                LOGGER.info("Application shutting down");
            });
            
            primaryStage.show();
            LOGGER.info("Application started successfully");
            
        } catch (Exception e) {
            LOGGER.severe("Failed to start application: " + e.getMessage());
            ErrorHandler.showError("Startup Error", "Application Failed to Start", 
                "Unable to initialize the application. Please check the logs for details.");
            throw e;
        }
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            LOGGER.severe("Application launch failed: " + e.getMessage());
            System.err.println("Application launch failed: " + e.getMessage());
            System.exit(1);
        }
    }
} 