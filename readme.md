# El País Automation Framework — BrowserStack Assignment

This project is a Selenium WebDriver automation framework developed in Java using TestNG and Maven.  
It automates scraping articles from the El País Opinion section, translates Spanish headlines to English using RapidAPI, performs word frequency analysis, and supports execution on both local and BrowserStack environments.

## Project Structure

BrowserstackAssignment/
├── src/test/java/com/elpais/automation/
│ ├── config/
│ ├── drivers/
│ ├── pages/
│ ├── tests/
│ └── utils/
├── src/test/resources/
│ ├── config.properties
│ └── log4j2.xml
├── pom.xml
└── testng.xml

## Prerequisites

- Java 17 or higher  
- Maven 3.9+  
- Chrome browser  
- BrowserStack account  
- RapidAPI key  

