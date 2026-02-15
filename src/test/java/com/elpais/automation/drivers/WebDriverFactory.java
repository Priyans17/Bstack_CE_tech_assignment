package com.elpais.automation.drivers;

import com.elpais.automation.config.BrowserStackConfig;
import com.elpais.automation.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.MalformedURLException;
import java.net.URL;

// WebDriver factory for creating local and remote WebDriver instances
public class WebDriverFactory {
    private static final Logger logger = LogManager.getLogger(WebDriverFactory.class);

    // Create local WebDriver for the specified browser
    public static WebDriver createLocalDriver(String browserName) {
        logger.info("Creating local WebDriver for browser: {}", browserName);

        switch (browserName.toLowerCase()) {
            case "chrome":
                return createChromeDriver();
            case "firefox":
                return createFirefoxDriver();
            default:
                logger.error("Unsupported browser: {}", browserName);
                throw new IllegalArgumentException("Unsupported browser: " + browserName);
        }
    }

    // Create remote WebDriver for BrowserStack
    public static WebDriver createRemoteDriver(String browserName, String browserVersion,
                                               String os, String osVersion, String resolution)
            throws MalformedURLException {
        logger.info("Creating remote WebDriver for BrowserStack: {} {} on {} {}",
                   browserName, browserVersion, os, osVersion);

        BrowserStackConfig config = new BrowserStackConfig();
        URL url = new URL(config.getUrl());

        return new RemoteWebDriver(url, config.buildCapabilities(browserName, browserVersion, os, osVersion, resolution));
    }

    // Create remote WebDriver for mobile device on BrowserStack
    public static WebDriver createRemoteMobileDriver(String device, String osVersion, String browserName)
            throws MalformedURLException {
        logger.info("Creating remote mobile WebDriver for BrowserStack: {} - {}", device, browserName);

        BrowserStackConfig config = new BrowserStackConfig();
        URL url = new URL(config.getUrl());

        return new RemoteWebDriver(url, config.buildMobileCapabilities(device, osVersion, browserName));
    }

    // Create local Chrome driver
    private static WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        logger.info("Chrome driver initialized");
        return new ChromeDriver(options);
    }

    // Create local Firefox driver
    private static WebDriver createFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--disable-gpu");

        logger.info("Firefox driver initialized");
        return new FirefoxDriver(options);
    }
}
