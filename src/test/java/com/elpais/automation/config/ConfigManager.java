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
        loadConfigProperties();
        loadDotEnvOverrides();
    }

    // Load config.properties from resources
    private static void loadConfigProperties() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(CONFIG_FILE);

            if (inputStream != null) {
                properties.load(inputStream);
                inputStream.close();
                logger.info("Configuration loaded from {}", CONFIG_FILE);
            } else {
                logger.warn("Configuration file not found: {}", CONFIG_FILE);
            }

        } catch (Exception e) {
            logger.error("Error loading config.properties", e);
        }
    }

    // Load .env from project root (override values)
    private static void loadDotEnvOverrides() {
        try {
            Path dotenvPath = Path.of(System.getProperty("user.dir")).resolve(DOTENV_FILE);

            if (!Files.exists(dotenvPath)) {
                logger.info("No .env file found at {}", dotenvPath);
                return;
            }

            logger.info("Loading .env file: {}", dotenvPath);

            for (String line : Files.readAllLines(dotenvPath, StandardCharsets.UTF_8)) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                int eq = line.indexOf('=');
                if (eq <= 0) continue;

                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();

                // Remove quotes
                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }

                properties.setProperty(key, value);
            }

        } catch (Exception e) {
            logger.error("Error loading .env", e);
        }
    }

    // Resolve ${ENV_VAR} placeholders
    private static String resolveEnvPlaceholder(String value) {
        if (value == null) return "";

        if (value.startsWith("${") && value.endsWith("}")) {
            String envKey = value.substring(2, value.length() - 1);
            String envVal = System.getenv(envKey);
            if (envVal != null && !envVal.isEmpty()) return envVal;
        }

        return value;
    }

    // Get configuration value: prefer OS environment variables, then .env/properties file
    public static String get(String key) {

        // 1. System env
        String sys = System.getenv(key);
        if (sys != null && !sys.isEmpty()) return sys;

        // 2. Properties
        String val = properties.getProperty(key);

        // Resolve ${ENV}
        val = resolveEnvPlaceholder(val);

        return val == null ? "" : val;
    }

    // Get configuration value with default fallback
    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val == null || val.isEmpty()) ? defaultValue : val;
    }

    // Get integer configuration value
    public static int getInt(String key, int defaultValue) {
        try {
            String value = get(key);
            if (!value.isEmpty()) return Integer.parseInt(value);
        } catch (Exception e) {
            logger.warn("Invalid integer for key {}", key);
        }
        return defaultValue;
    }

    // Get boolean configuration value
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value.isEmpty()) return defaultValue;
        return Boolean.parseBoolean(value);
    }

    // Get long configuration value
    public static long getLong(String key, long defaultValue) {
        try {
            String value = get(key);
            if (!value.isEmpty()) return Long.parseLong(value);
        } catch (Exception e) {
            logger.warn("Invalid long for key {}", key);
        }
        return defaultValue;
    }

    // Direct env read helper
    public static String getEnv(String key) {
        String sys = System.getenv(key);
        if (sys != null && !sys.isEmpty()) return sys;
        return properties.getProperty(key);
    }
}
