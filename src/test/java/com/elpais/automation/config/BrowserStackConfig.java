package com.elpais.automation.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;

// BrowserStack configuration manager
public class BrowserStackConfig {
    private static final Logger logger = LogManager.getLogger(BrowserStackConfig.class);

    private String username;
    private String accessKey;
    private String url;
    private Map<String, Object> capabilities;

    public BrowserStackConfig() {
        // Load credentials from .env file via ConfigManager
        this.username = ConfigManager.get("BROWSERSTACK_USERNAME");
        this.accessKey = ConfigManager.get("BROWSERSTACK_ACCESSKEY");

        // Validate credentials are loaded
        if (this.username == null || this.username.isEmpty()) {
            logger.warn("BROWSERSTACK_USERNAME not found in .env file");
            this.username = ""; // Will use environment variable if available
        }
        if (this.accessKey == null || this.accessKey.isEmpty()) {
            logger.warn("BROWSERSTACK_ACCESSKEY not found in .env file");
            this.accessKey = ""; // Will use environment variable if available
        }

        this.url = "https://hub.browserstack.com/wd/hub";
        this.capabilities = new HashMap<>();

        logger.info("BrowserStack configuration initialized with username: {}",
                this.username.substring(0, Math.min(5, this.username.length())) + "***");
    }

    // Build capabilities for BrowserStack execution
    public DesiredCapabilities buildCapabilities(String browserName, String browserVersion,
                                                 String os, String osVersion, String resolution) {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("browserName", browserName);

        // BrowserStack specific capabilities (W3C format)
        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("userName", username);
        bstackOptions.put("accessKey", accessKey);
        bstackOptions.put("os", os);
        bstackOptions.put("osVersion", osVersion);
        bstackOptions.put("browserVersion", browserVersion);
        bstackOptions.put("resolution", resolution);
        bstackOptions.put("projectName", "El País BrowserStack");
        bstackOptions.put("buildName", "Build-" + System.currentTimeMillis());
        bstackOptions.put("sessionName", "Article Scraping Test - " + browserName);
        bstackOptions.put("debug", "true");
        bstackOptions.put("networkLogs", "true");
        bstackOptions.put("consoleLogs", "warnings");

        caps.setCapability("bstack:options", bstackOptions);

        logger.info("Built capabilities for: {} {} on {} {}", browserName, browserVersion, os, osVersion);
        return caps;
    }

    // Build mobile device capabilities
    public DesiredCapabilities buildMobileCapabilities(String device, String osVersion, String browserName) {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("browserName", browserName);

        // BrowserStack specific capabilities for mobile (W3C format)
        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("userName", username);
        bstackOptions.put("accessKey", accessKey);
        bstackOptions.put("deviceName", device);
        bstackOptions.put("osVersion", osVersion);
        bstackOptions.put("projectName", "El País BrowserStack");
        bstackOptions.put("buildName", "Build-" + System.currentTimeMillis());
        bstackOptions.put("sessionName", "Mobile Test - " + device);
        bstackOptions.put("debug", "true");

        caps.setCapability("bstack:options", bstackOptions);

        logger.info("Built mobile capabilities for: {} - {}", device, browserName);
        return caps;
    }

    // ...existing code...
    public String getUsername() {
        return username;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Object> capabilities) {
        this.capabilities = capabilities;
    }
}
