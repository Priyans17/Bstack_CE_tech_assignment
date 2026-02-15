package com.elpais.automation.tests;

import com.elpais.automation.drivers.DriverManager;
import com.elpais.automation.pages.HomePage;
import com.elpais.automation.pages.OpinionPage;
import com.elpais.automation.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.*;
import java.util.List;

/**
 * Test class for article scraping functionality
 */
public class ArticleScrapingTest {
    private static final Logger logger = LogManager.getLogger(ArticleScrapingTest.class);
    private String env;
    private String browserName;
    private String browserVersion;
    private String os;
    private String osVersion;
    private String resolution;

    @Parameters({"env", "browserName", "browserVersion", "os", "osVersion", "resolution"})
    @BeforeMethod
    public void setUp(@Optional("local") String env,
                      @Optional("chrome") String browserName,
                      @Optional("latest") String browserVersion,
                      @Optional("Windows") String os,
                      @Optional("11") String osVersion,
                      @Optional("1920x1080") String resolution) {
        this.env = env;
        this.browserName = browserName;
        this.browserVersion = browserVersion;
        this.os = os;
        this.osVersion = osVersion;
        this.resolution = resolution;

        logger.info("Starting test with: env={}, browser={}, os={}", env, browserName, os);
        DriverManager.initializeDriver(env, browserName, browserVersion, os, osVersion, resolution);
    }

    @Test(description = "Test article scraping from El País Opinion section")
    public void testArticleScraping() {
        logger.info("Test: Article Scraping");
        String appUrl = ConfigManager.get("app.url");

        // Navigate to home page
        HomePage homePage = new HomePage(DriverManager.getDriver());
        homePage.navigate(appUrl);

        // Verify home page is loaded
        assert homePage.isHomePageLoaded() : "Home page failed to load";
        logger.info("Home page loaded successfully");

        // Navigate to Opinion section
        OpinionPage opinionPage = homePage.navigateToOpinionSection();
        opinionPage.waitForArticlesLoad();
        logger.info("Opinion page loaded successfully");

        // Get top articles
        int articleCount = 5;
        List<OpinionPage.ArticleData> articles = opinionPage.getTopArticles(articleCount);

        // Verify articles were scraped
        assert !articles.isEmpty() : "No articles were scraped";
        logger.info("Successfully scraped {} articles", articles.size());

        // Log article details
        for (int i = 0; i < articles.size(); i++) {
            OpinionPage.ArticleData article = articles.get(i);
            logger.info("Article {}: Title={}", i + 1, article.title);
        }
    }

    @Test(description = "Test multiple pages of articles")
    public void testMultipleArticles() {
        logger.info("Test: Multiple Articles Scraping");
        String appUrl = ConfigManager.get("app.url");

        HomePage homePage = new HomePage(DriverManager.getDriver());
        homePage.navigate(appUrl);

        OpinionPage opinionPage = homePage.navigateToOpinionSection();
        opinionPage.waitForArticlesLoad();

        List<OpinionPage.ArticleData> articles = opinionPage.getTopArticles(10);
        assert articles.size() >= 5 : "Expected at least 5 articles but got " + articles.size();
        logger.info("Test passed: Found {} articles", articles.size());
    }

    @AfterMethod
    public void tearDown() {
        logger.info("Closing WebDriver");
        DriverManager.quitDriver();
    }
}

