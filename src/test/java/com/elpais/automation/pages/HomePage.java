package com.elpais.automation.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object for El País home page
 */
public class HomePage extends BasePage {
    private static final Logger logger = LogManager.getLogger(HomePage.class);

    // Locators
    private static final By LANGUAGE_SELECTOR = By.xpath("//button[@data-language='es']");
    private static final By OPINION_SECTION = By.xpath("//a[contains(@href, '/opinion/')] | //a[text()='Opinión']");
    private static final By CLOSE_COOKIE_BANNER = By.id("didomi-notice-agree-button");
    private static final By HAMBURGER_MENU = By.id("btn_hamb");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigate to El País website
     */
    public void navigate(String url) {
        logger.info("Navigating to: {}", url);
        driver.get(url);
        waitForPageLoad();
        closeCookieBanner();
    }

    /**
     * Set language to Spanish
     */
    public void setLanguageToSpanish() {
        logger.info("Setting language to Spanish");
        try {
            if (isElementPresent(LANGUAGE_SELECTOR)) {
                clickElement(LANGUAGE_SELECTOR);
                Thread.sleep(2000); // Wait for language switch
                logger.info("Language set to Spanish");
            }
        } catch (Exception e) {
            logger.warn("Error setting language, continuing anyway", e);
        }
    }

    /**
     * Close cookie banner if present
     */
    public void closeCookieBanner() {
        logger.info("Attempting to close cookie banner");
        try {
            // Wait a bit for banner to appear
            Thread.sleep(2000);
            if (isElementPresent(CLOSE_COOKIE_BANNER)) {
                clickElement(CLOSE_COOKIE_BANNER);
                Thread.sleep(1000);
                logger.info("Cookie banner closed");
            }
        } catch (Exception e) {
            logger.debug("Cookie banner not found or already closed");
        }
    }

    /**
     * Navigate to Opinion section
     */
    public OpinionPage navigateToOpinionSection() {
        logger.info("Navigating to Opinion section");
        closeCookieBanner();
        
        try {
            // Try direct click first
            if (isElementPresent(OPINION_SECTION)) {
                clickElement(OPINION_SECTION);
            } else {
                // If not present (common on mobile), open hamburger menu
                logger.info("Opinion link not visible, trying hamburger menu");
                clickElement(HAMBURGER_MENU);
                Thread.sleep(1000);
                clickElement(OPINION_SECTION);
            }
        } catch (Exception e) {
            logger.error("Failed to navigate to Opinion section", e);
            // Fallback: direct URL navigation if click fails
            driver.get("https://elpais.com/opinion/");
        }
        
        waitForPageLoad();
        return new OpinionPage(driver);
    }

    /**
     * Verify home page is loaded
     */
    public boolean isHomePageLoaded() {
        try {
            waitForPageLoad();
            return isElementPresent(OPINION_SECTION) || isElementPresent(HAMBURGER_MENU);
        } catch (Exception e) {
            logger.error("Home page not loaded properly", e);
            return false;
        }
    }
}

