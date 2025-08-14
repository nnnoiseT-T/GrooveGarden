package com.groovegarden.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Application configuration management
 */
public class AppConfig {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final String CONFIG_FILE = "groove-garden.properties";
    
    // Default values
    private static final Properties DEFAULT_CONFIG = new Properties();
    static {
        // Grid settings
        DEFAULT_CONFIG.setProperty("grid.size", "8");
        DEFAULT_CONFIG.setProperty("grid.cell.size", "50");
        DEFAULT_CONFIG.setProperty("grid.gap", "2");
        
        // Music settings
        DEFAULT_CONFIG.setProperty("music.default.tempo", "120");
        DEFAULT_CONFIG.setProperty("music.min.tempo", "60");
        DEFAULT_CONFIG.setProperty("music.max.tempo", "180");
        DEFAULT_CONFIG.setProperty("music.default.scale", "C Dorian");
        DEFAULT_CONFIG.setProperty("music.time.signature", "4/4");
        DEFAULT_CONFIG.setProperty("music.step.size", "16");
        
        // UI settings
        DEFAULT_CONFIG.setProperty("ui.window.width", "675");
        DEFAULT_CONFIG.setProperty("ui.window.height", "650");
        DEFAULT_CONFIG.setProperty("ui.panel.width", "180");
        DEFAULT_CONFIG.setProperty("ui.panel.height", "120");
        
        // Performance settings
        DEFAULT_CONFIG.setProperty("performance.update.interval", "16");
        DEFAULT_CONFIG.setProperty("performance.buffer.size", "1024");
        DEFAULT_CONFIG.setProperty("performance.score.update.interval", "1000");
        
        // MIDI settings
        DEFAULT_CONFIG.setProperty("midi.channel.drums", "9");
        DEFAULT_CONFIG.setProperty("midi.channel.bass", "0");
        DEFAULT_CONFIG.setProperty("midi.channel.melody", "1");
        DEFAULT_CONFIG.setProperty("midi.velocity.default", "80");
    }
    
    private static Properties config = new Properties();
    private static boolean initialized = false;
    
    /**
     * Initialize configuration
     */
    public static void initialize() {
        if (initialized) return;
        
        try {
            // Load existing config or create new one
            if (loadConfig()) {
                LOGGER.info("Configuration loaded from file");
            } else {
                // Use defaults and save
                config.putAll(DEFAULT_CONFIG);
                saveConfig();
                LOGGER.info("Default configuration created and saved");
            }
            initialized = true;
        } catch (Exception e) {
            LOGGER.warning("Failed to load configuration, using defaults: " + e.getMessage());
            config.putAll(DEFAULT_CONFIG);
            initialized = true;
        }
    }
    
    /**
     * Get configuration value as string
     */
    public static String getString(String key) {
        if (!initialized) initialize();
        return config.getProperty(key, DEFAULT_CONFIG.getProperty(key, ""));
    }
    
    /**
     * Get configuration value as integer
     */
    public static int getInt(String key) {
        if (!initialized) initialize();
        try {
            return Integer.parseInt(config.getProperty(key, DEFAULT_CONFIG.getProperty(key, "0")));
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid integer value for key: " + key);
            return 0;
        }
    }
    
    /**
     * Get configuration value as double
     */
    public static double getDouble(String key) {
        if (!initialized) initialize();
        try {
            return Double.parseDouble(config.getProperty(key, DEFAULT_CONFIG.getProperty(key, "0.0")));
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid double value for key: " + key);
            return 0.0;
        }
    }
    
    /**
     * Set configuration value
     */
    public static void set(String key, String value) {
        if (!initialized) initialize();
        config.setProperty(key, value);
        try {
            saveConfig();
        } catch (Exception e) {
            LOGGER.warning("Failed to save configuration: " + e.getMessage());
        }
    }
    
    /**
     * Reset configuration to defaults
     */
    public static void resetToDefaults() {
        config.clear();
        config.putAll(DEFAULT_CONFIG);
        try {
            saveConfig();
            LOGGER.info("Configuration reset to defaults");
        } catch (Exception e) {
            LOGGER.warning("Failed to save default configuration: " + e.getMessage());
        }
    }
    
    private static boolean loadConfig() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            config.load(input);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    private static void saveConfig() throws IOException {
        try (FileOutputStream output = new FileOutputStream(CONFIG_FILE)) {
            config.store(output, "Groove Garden Configuration");
        }
    }
} 