package com.elpais.automation.config;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Configuration manager for loading application properties from .env file and config.properties
public class ConfigManager {
    private static final Logger logger = LogManager.getLogger(ConfigManager.class);
    private static final Properties properties = new Properties();

    private static final String CONFIG_FILE = "config.properties";
    private static final String DOTENV_FILE = ".env";

    static {
        try {
            loadConfigProperties();
            loadDotEnv();
            logger.info("Configuration initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing configuration", e);
        }
    }

    // Load config.properties from resources
    private static void loadConfigProperties() {
        try (InputStream inputStream =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE)) {

            if (inputStream != null) {
                properties.load(inputStream);
                logger.info("Loaded configuration from {}", CONFIG_FILE);
            } else {
                logger.warn("{} not found in resources", CONFIG_FILE);
            }
        } catch (Exception e) {
            logger.error("Error loading config.properties", e);
        }
    }

    // Load .env from project root
    private static void loadDotEnv() {
        try {
            Path dotenvPath = Path.of(System.getProperty("user.dir")).resolve(DOTENV_FILE);

            if (!Files.exists(dotenvPath)) {
                logger.info(".env file not found at {}", dotenvPath);
                return;
            }

            logger.info("Loading .env from {}", dotenvPath);

            for (String line : Files.readAllLines(dotenvPath, StandardCharsets.UTF_8)) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                int idx = line.indexOf('=');
                if (idx <= 0) continue;

                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();

                if (!key.isEmpty()) {
                    properties.setProperty(key, value);
                }
            }

        } catch (Exception e) {
            logger.error("Error loading .env file", e);
        }
    }

    // Resolve configuration value with env mapping support
    public static String get(String key) {
        String value = properties.getProperty(key);

        if (value == null || value.isEmpty()) return "";

        // If value refers to ENV variable name → resolve it
        String envValue = System.getenv(value);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        // Otherwise return direct value
        return value;
    }

    // Get with default
    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val == null || val.isEmpty()) ? defaultValue : val;
    }

    // Integer
    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // Boolean
    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        if (val == null || val.isEmpty()) return defaultValue;
        return Boolean.parseBoolean(val);
    }

    // Long
    public static long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(get(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
