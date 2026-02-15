package com.elpais.automation.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;


//Base Page Object Model class

public class BasePage {
    protected static final Logger logger = LogManager.getLogger(BasePage.class);
    protected WebDriver driver;
    protected WebDriverWait wait;
    private static final int TIMEOUT_SECONDS = 15;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
    }

//Wait for element to be visible and return it

    protected WebElement waitForElement(By locator) {
        logger.debug("Waiting for element: {}", locator);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

//Wait for element to be clickable and click it
    
    protected void clickElement(By locator) {
        logger.debug("Clicking element: {}", locator);
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

//Send keys to an element

    protected void sendKeys(By locator, String text) {
        logger.debug("Sending keys to element: {}", locator);
        waitForElement(locator).sendKeys(text);
    }

//Get text from element

    protected String getText(By locator) {
        logger.debug("Getting text from element: {}", locator);
        return waitForElement(locator).getText();
    }

//Get all elements matching the locator
     
    protected List<WebElement> getElements(By locator) {
        logger.debug("Getting all elements: {}", locator);
        return driver.findElements(locator);
    }

//Get single element without waiting

    protected WebElement findElement(By locator) {
        return driver.findElement(locator);
    }

//Check if element is present

    protected boolean isElementPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

//Wait for element to be present

    protected void waitForElementPresent(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

//Get attribute value
    
    protected String getAttribute(By locator, String attributeName) {
        logger.debug("Getting attribute {} from element: {}", attributeName, locator);
        return waitForElement(locator).getAttribute(attributeName);
    }

//Scroll to element by locator
  
    protected void scrollToElement(By locator) {
        logger.debug("Scrolling to element: {}", locator);
        WebElement element = waitForElement(locator);
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

//Scroll to element (WebElement)

    protected void scrollToElement(WebElement element) {
        logger.debug("Scrolling to element");
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

//Execute JavaScript
  
    protected Object executeScript(String script, Object... args) {
        return ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(script, args);
    }

//Wait for page to load
    
    protected void waitForPageLoad() {
        wait.until(driver -> {
            Object readyState = executeScript("return document.readyState");
            return "complete".equals(readyState);
        });
        logger.debug("Page loaded");
    }
}

