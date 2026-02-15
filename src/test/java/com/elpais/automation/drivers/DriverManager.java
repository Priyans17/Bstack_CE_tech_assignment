package com.elpais.automation.drivers;

import com.elpais.automation.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe WebDriver manager using ThreadLocal pattern
 */
public class DriverManager {
    private static final Logger logger = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> threadLocalDriver = new ThreadLocal<>();

    private DriverManager() {
    }

    /**
     * Initialize WebDriver based on environment and browser parameters
     */
    public static void initializeDriver(String env, String browserName, String browserVersion,
                                       String os, String osVersion, String resolution) {
        try {
            // Increase Selenium's internal HTTP client timeout for remote execution
            System.setProperty("webdriver.http.factory", "jdk-http-client");
            System.setProperty("jdk.httpclient.connectionTimeout", "120000");
            System.setProperty("jdk.httpclient.receiveTimeout", "120000");

            WebDriver driver;

            if ("browserstack".equalsIgnoreCase(env)) {
                driver = WebDriverFactory.createRemoteDriver(browserName, browserVersion, os, osVersion, resolution);
            } else {
                driver = WebDriverFactory.createLocalDriver(browserName);
            }

            // Set timeouts
            int implicitTimeout = ConfigManager.getInt("app.timeout.implicit", 10);
            int pageLoadTimeout = ConfigManager.getInt("app.timeout.pageLoad", 20);

            driver.manage().timeouts().implicitlyWait(implicitTimeout, TimeUnit.SECONDS);
            driver.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);

            // Maximize window for desktop browsers
            if (!"mobile".equalsIgnoreCase(browserName)) {
                driver.manage().window().maximize();
            }

            threadLocalDriver.set(driver);
            logger.info("WebDriver initialized successfully for thread: {}", Thread.currentThread().getId());
        } catch (MalformedURLException e) {
            logger.error("Failed to initialize WebDriver", e);
            throw new RuntimeException("WebDriver initialization failed", e);
        }
    }

    /**
     * Initialize WebDriver for mobile testing
     */
    public static void initializeMobileDriver(String device, String osVersion, String browserName) {
        try {
            // Increase Selenium's internal HTTP client timeout for remote execution
            System.setProperty("webdriver.http.factory", "jdk-http-client");
            System.setProperty("jdk.httpclient.connectionTimeout", "120000");
            System.setProperty("jdk.httpclient.receiveTimeout", "120000");

            WebDriver driver = WebDriverFactory.createRemoteMobileDriver(device, osVersion, browserName);

            int implicitTimeout = ConfigManager.getInt("app.timeout.implicit", 10);
            driver.manage().timeouts().implicitlyWait(implicitTimeout, TimeUnit.SECONDS);

            threadLocalDriver.set(driver);
            logger.info("Mobile WebDriver initialized for thread: {}", Thread.currentThread().getId());
        } catch (MalformedURLException e) {
            logger.error("Failed to initialize mobile WebDriver", e);
            throw new RuntimeException("Mobile WebDriver initialization failed", e);
        }
    }

    /**
     * Get WebDriver instance for current thread
     */
    public static WebDriver getDriver() {
        WebDriver driver = threadLocalDriver.get();
        if (driver == null) {
            logger.warn("No WebDriver found for thread: {}", Thread.currentThread().getId());
        }
        return driver;
    }

    /**
     * Close and remove WebDriver for current thread
     */
    public static void quitDriver() {
        WebDriver driver = threadLocalDriver.get();
        if (driver != null) {
            try {
                driver.quit();
                logger.info("WebDriver closed for thread: {}", Thread.currentThread().getId());
            } catch (Exception e) {
                logger.error("Error closing WebDriver", e);
            } finally {
                threadLocalDriver.remove();
            }
        }
    }

    /**
     * Take screenshot and save to file
     */
    public static String takeScreenshot(String testName) {
        WebDriver driver = getDriver();
        if (driver instanceof org.openqa.selenium.TakesScreenshot) {
            try {
                String screenshotPath = "./screenshots/" + testName + "_" + System.currentTimeMillis() + ".png";
                java.io.File screenshotFile = ((org.openqa.selenium.TakesScreenshot) driver)
                        .getScreenshotAs(org.openqa.selenium.OutputType.FILE);
                org.apache.commons.io.FileUtils.copyFile(screenshotFile, new java.io.File(screenshotPath));
                logger.info("Screenshot saved: {}", screenshotPath);
                return screenshotPath;
            } catch (Exception e) {
                logger.error("Failed to take screenshot", e);
            }
        }
        return "";
    }
}

