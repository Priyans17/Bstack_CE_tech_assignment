package com.elpais.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration manager for loading application properties from .env file and config.properties
 */
public class ConfigManager {
    private static final Logger logger = LogManager.getLogger(ConfigManager.class);
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "config.properties";
    private static final String DOTENV_FILE = ".env";

    static {
        try {
            // Load config.properties as defaults
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(CONFIG_FILE);
            if (inputStream != null) {
                properties.load(inputStream);
                inputStream.close();
                logger.info("Configuration loaded successfully from: {}", CONFIG_FILE);
            } else {
                logger.warn("Configuration file not found: {}", CONFIG_FILE);
            }

            // Load .env file from project root (if present) as overrides
            Path dotenvPath = Path.of(System.getProperty("user.dir")).resolve(DOTENV_FILE);
            if (Files.exists(dotenvPath)) {
                logger.info("Loading local .env file: {}", dotenvPath.toString());
                for (String line : Files.readAllLines(dotenvPath, StandardCharsets.UTF_8)) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eq = line.indexOf('=');
                    if (eq <= 0) continue;
                    String key = line.substring(0, eq).trim();
                    String value = line.substring(eq + 1).trim();
                    // Remove optional surrounding quotes
                    if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    if (!key.isEmpty()) {
                        properties.setProperty(key, value);
                    }
                }
            } else {
                logger.info("No local .env file found at {}", dotenvPath.toString());
            }

        } catch (IOException e) {
            logger.error("Error loading configuration files", e);
        }
    }

    /**
     * Get configuration value: prefer OS environment variables, then .env/properties file
     */
    public static String get(String key) {
        // First prefer system environment variables
        String sys = System.getenv(key);
        if (sys != null && !sys.isEmpty()) return sys;

        // Then properties (includes values loaded from config.properties and .env)
        return properties.getProperty(key, "");
    }

    /**
     * Get configuration value with default fallback
     */
    public static String get(String key, String defaultValue) {
        String val = get(key);
        if (val == null || val.isEmpty()) return defaultValue;
        return val;
    }

    /**
     * Get integer configuration value
     */
    public static int getInt(String key, int defaultValue) {
        try {
            String value = get(key);
            if (value != null && !value.isEmpty()) {
                return Integer.parseInt(value);
            }
            return defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for key: {}", key);
            return defaultValue;
        }
    }

    /**
     * Get boolean configuration value
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    /**
     * Get long configuration value
     */
    public static long getLong(String key, long defaultValue) {
        try {
            String value = get(key);
            if (value != null && !value.isEmpty()) {
                return Long.parseLong(value);
            }
            return defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Invalid long value for key: {}", key);
            return defaultValue;
        }
    }

    /**
     * Get all configuration values as map (returns property value first, then system env if set)
     */
    public static String getEnv(String key) {
        String sys = System.getenv(key);
        if (sys != null && !sys.isEmpty()) return sys;
        return properties.getProperty(key);
    }
}
