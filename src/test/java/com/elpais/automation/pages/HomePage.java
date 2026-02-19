package com.elpais.automation.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

// Page Object for El País home page
public class HomePage extends BasePage {

    private static final Logger logger = LogManager.getLogger(HomePage.class);

    // Locators (more stable)
    private static final By OPINION_LINK =
            By.xpath("//a[contains(@href,'/opinion')] | //a[text()='Opinión']");
    private static final By COOKIE_ACCEPT =
            By.cssSelector("#didomi-notice-agree-button, button[id*='agree'], button[class*='agree']");
    private static final By HAMBURGER_MENU =
            By.cssSelector("#btn_hamb, .btn_hamb, #hamburger");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    // Navigate to El País website
    public void navigate(String url) {

        if (url == null || url.trim().isEmpty()) {
            url = "https://elpais.com";
            logger.warn("Empty URL provided. Using default {}", url);
        }

        logger.info("Navigating to {}", url);
        driver.get(url);

        waitForPageLoad();
        closeCookieBanner();
    }

    // Close cookie banner
    public void closeCookieBanner() {
        try {
            if (waitForElementVisible(COOKIE_ACCEPT, 5)) {
                clickElement(COOKIE_ACCEPT);
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
            if (waitForElementClickable(OPINION_LINK, 5)) {
                clickElement(OPINION_LINK);
            } else {
                logger.info("Opinion link hidden. Using menu");
                clickElement(HAMBURGER_MENU);
                waitForElementClickable(OPINION_LINK, 5);
                clickElement(OPINION_LINK);
            }

        } catch (Exception e) {
            logger.warn("UI navigation failed. Opening direct URL");
            driver.get("https://elpais.com/opinion/");
        }

        waitForPageLoad();
        return new OpinionPage(driver);
    }

    // Verify home page
    public boolean isHomePageLoaded() {
        return waitForElementVisible(OPINION_LINK, 5)
                || waitForElementVisible(HAMBURGER_MENU, 5);
    }
}
