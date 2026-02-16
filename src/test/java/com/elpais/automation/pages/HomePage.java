package com.elpais.automation.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

// Page Object for El País home page
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

    // Navigate to El País website
    public void navigate(String url) {

        // Fix: prevent invalid argument if URL empty
        if (url == null || url.trim().isEmpty()) {
            url = "https://elpais.com";
            logger.warn("Empty URL provided. Using default {}", url);
        }

        logger.info("Navigating to {}", url);
        driver.get(url);
        waitForPageLoad();
        closeCookieBanner();
    }

    // Set language to Spanish
    public void setLanguageToSpanish() {
        logger.info("Setting language to Spanish");

        try {
            if (isElementPresent(LANGUAGE_SELECTOR)) {
                clickElement(LANGUAGE_SELECTOR);
                Thread.sleep(2000);
                logger.info("Language set to Spanish");
            }
        } catch (Exception e) {
            logger.warn("Language switch skipped", e);
        }
    }

    // Close cookie banner if present
    public void closeCookieBanner() {
        logger.info("Attempting to close cookie banner");

        try {
            Thread.sleep(2000);

            if (isElementPresent(CLOSE_COOKIE_BANNER)) {
                clickElement(CLOSE_COOKIE_BANNER);
                Thread.sleep(1000);
                logger.info("Cookie banner closed");
            }

        } catch (Exception e) {
            logger.debug("Cookie banner not present");
        }
    }

    // Navigate to Opinion section
    public OpinionPage navigateToOpinionSection() {
        logger.info("Navigating to Opinion section");

        closeCookieBanner();

        try {
            if (isElementPresent(OPINION_SECTION)) {
                clickElement(OPINION_SECTION);
            } else {
                logger.info("Opinion link not visible, using menu");
                clickElement(HAMBURGER_MENU);
                Thread.sleep(1000);
                clickElement(OPINION_SECTION);
            }

        } catch (Exception e) {
            logger.warn("Click navigation failed. Using direct URL");
            driver.get("https://elpais.com/opinion/");
        }

        waitForPageLoad();
        return new OpinionPage(driver);
    }

    // Verify home page is loaded
    public boolean isHomePageLoaded() {
        try {
            waitForPageLoad();
            return isElementPresent(OPINION_SECTION) || isElementPresent(HAMBURGER_MENU);
        } catch (Exception e) {
            logger.error("Home page not loaded", e);
            return false;
        }
    }
}
