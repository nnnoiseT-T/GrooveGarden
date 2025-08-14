package com.groovegarden.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized error handling utility for user-friendly error messages
 */
public class ErrorHandler {
    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());
    
    /**
     * Show a user-friendly error dialog
     */
    public static void showError(String title, String header, String content) {
        LOGGER.log(Level.SEVERE, "Error: {0} - {1} - {2}", new Object[]{title, header, content});
        
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Show a warning dialog
     */
    public static void showWarning(String title, String header, String content) {
        LOGGER.log(Level.WARNING, "Warning: {0} - {1} - {2}", new Object[]{title, header, content});
        
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Show an info dialog
     */
    public static void showInfo(String title, String header, String content) {
        LOGGER.log(Level.INFO, "Info: {0} - {1} - {2}", new Object[]{title, header, content});
        
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Show a confirmation dialog
     */
    public static boolean showConfirmation(String title, String header, String content) {
        LOGGER.log(Level.INFO, "Confirmation: {0} - {1} - {2}", new Object[]{title, header, content});
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    /**
     * Handle MIDI device errors
     */
    public static void handleMidiError(String operation, Exception e) {
        LOGGER.log(Level.SEVERE, "MIDI error during " + operation, e);
        
        String content = "Unable to access MIDI devices. Please check:\n" +
                        "• MIDI drivers are installed\n" +
                        "• No other application is using MIDI\n" +
                        "• System audio is working properly";
        
        showError("MIDI Error", "MIDI Device Unavailable", content);
    }
    
    /**
     * Handle file operation errors
     */
    public static void handleFileError(String operation, String filePath, Exception e) {
        LOGGER.log(Level.SEVERE, "File error during " + operation + " on " + filePath, e);
        
        String content = "Unable to " + operation + " file:\n" +
                        filePath + "\n\n" +
                        "Please check:\n" +
                        "• File path is valid\n" +
                        "• You have write permissions\n" +
                        "• Disk space is available";
        
        showError("File Error", "File Operation Failed", content);
    }
} 