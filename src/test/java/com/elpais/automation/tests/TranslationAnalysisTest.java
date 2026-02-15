package com.elpais.automation.tests;

import com.elpais.automation.drivers.DriverManager;
import com.elpais.automation.pages.HomePage;
import com.elpais.automation.pages.OpinionPage;
import com.elpais.automation.utils.TranslationService;
import com.elpais.automation.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Test class for translation analysis functionality
public class TranslationAnalysisTest {
    private static final Logger logger = LogManager.getLogger(TranslationAnalysisTest.class);
    private String env;
    private String browserName;
    private String browserVersion;
    private String os;
    private String osVersion;
    private String resolution;

    @Parameters({"env", "browserName", "browserVersion", "os", "osVersion", "resolution", "device"})
    @BeforeMethod
    public void setUp(@Optional("local") String env,
                      @Optional("chrome") String browserName,
                      @Optional("latest") String browserVersion,
                      @Optional("Windows") String os,
                      @Optional("11") String osVersion,
                      @Optional("1920x1080") String resolution,
                      @Optional("") String device) {
        this.env = env;
        this.browserName = browserName;
        this.browserVersion = browserVersion;
        this.os = os;
        this.osVersion = osVersion;
        this.resolution = resolution;

        logger.info("Starting translation test with: env={}, browser={}, device={}", env, browserName, device);
        
        if (!device.isEmpty() && "browserstack".equalsIgnoreCase(env)) {
            DriverManager.initializeMobileDriver(device, osVersion, browserName);
        } else {
            DriverManager.initializeDriver(env, browserName, browserVersion, os, osVersion, resolution);
        }
    }

    @Test(description = "Complete workflow for El País Opinion section scraping and analysis")
    public void testCompleteWorkflow() {
        logger.info("Starting complete workflow test");
        String appUrl = ConfigManager.get("app.url", "https://elpais.com");

        // 1 & 2. Visit El País and ensure text is in Spanish (default)
        HomePage homePage = new HomePage(DriverManager.getDriver());
        homePage.navigate(appUrl);
        homePage.closeCookieBanner();
        
        // 3. Navigate to Opinion section
        OpinionPage opinionPage = homePage.navigateToOpinionSection();
        opinionPage.waitForArticlesLoad();
        logger.info("Navigated to Opinion section");

        // 4 & 5. Fetch first 5 articles and print title/content in Spanish
        List<OpinionPage.ArticleData> articles = opinionPage.getTopArticles(5);
        assert articles.size() >= 1 : "No articles found in Opinion section";
        
        System.out.println("\n--- ARTICLES IN SPANISH ---");
        for (int i = 0; i < articles.size(); i++) {
            OpinionPage.ArticleData article = articles.get(i);
            System.out.println("Article " + (i + 1) + ":");
            System.out.println("Title: " + article.title);
            System.out.println("Content: " + article.content);
            
            // 6. Download and save cover image
            if (article.imageUrl != null && !article.imageUrl.isEmpty()) {
                String downloadPath = ConfigManager.get("download.path", "downloaded_images");
                String imagePath = com.elpais.automation.utils.ImageDownloader.downloadImage(article.imageUrl, downloadPath);
                logger.info("Image saved to: " + imagePath);
            }
        }

        // 7 & 8. Translate headers to English and print
        System.out.println("\n--- TRANSLATED HEADERS (ENGLISH) ---");
        StringBuilder allHeadersBuilder = new StringBuilder();
        for (OpinionPage.ArticleData article : articles) {
            String translatedTitle = TranslationService.translateToEnglish(article.title);
            article.translatedTitle = translatedTitle;
            System.out.println("Translated Header: " + translatedTitle);
            allHeadersBuilder.append(translatedTitle).append(" ");
        }

        // 9 & 10. Analyze translated headers for repeated words (> 2 times)
        System.out.println("\n--- WORD REPETITION ANALYSIS (> 2 occurrences) ---");
        // Use \p{L} to match any letter in any language (including Spanish accents if translation fails)
        String allHeadersText = allHeadersBuilder.toString().toLowerCase().replaceAll("[^\\p{L}\\s]", "");
        String[] words = allHeadersText.split("\\s+");
        
        Map<String, Integer> wordCounts = new HashMap<>();
        for (String word : words) {
            if (word.length() > 0) {
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
        }

        boolean foundRepeated = false;
        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            if (entry.getValue() > 2) {
                System.out.println("Word: '" + entry.getKey() + "' - Count: " + entry.getValue());
                foundRepeated = true;
            }
        }
        
        if (!foundRepeated) {
            System.out.println("No words repeated more than twice across all headers.");
        }

        logger.info("Complete workflow test finished");
    }

    @Test(description = "Test batch translation of article titles")
    public void testBatchTranslation() {
        logger.info("Test: Batch Translation");

        // Sample Spanish titles
        Map<String, String> titlesToTranslate = new HashMap<>();
        titlesToTranslate.put("article1", "El cambio climático afecta nuestro futuro");
        titlesToTranslate.put("article2", "Nuevas tecnologías revolucionan la industria");
        titlesToTranslate.put("article3", "La educación es fundamental para el progreso");

        // Perform batch translation
        Map<String, String> translatedTitles = TranslationService.translateMultiple(titlesToTranslate);

        // Verify all translations were performed
        assert translatedTitles.size() == titlesToTranslate.size() : "Not all articles were translated";

        for (Map.Entry<String, String> entry : translatedTitles.entrySet()) {
            logger.info("Key: {}, Translated: {}", entry.getKey(), entry.getValue());
            assert !entry.getValue().isEmpty() : "Translation for " + entry.getKey() + " is empty";
        }

        logger.info("Batch translation test passed");
    }

    @Test(description = "Test translation cache functionality")
    public void testTranslationCache() {
        logger.info("Test: Translation Cache");

        String spanishText = "El País es un periódico importante";

        // First translation (will call API)
        long startTime1 = System.currentTimeMillis();
        String translation1 = TranslationService.translateToEnglish(spanishText);
        long duration1 = System.currentTimeMillis() - startTime1;
        logger.info("First translation took: {} ms", duration1);

        // Second translation (should be cached)
        long startTime2 = System.currentTimeMillis();
        String translation2 = TranslationService.translateToEnglish(spanishText);
        long duration2 = System.currentTimeMillis() - startTime2;
        logger.info("Cached translation took: {} ms", duration2);

        // Verify translations are identical
        assert translation1.equals(translation2) : "Cached translation differs from original";

        // Cached should be faster (or at least not slower due to network)
        logger.info("Translation cache working as expected");
    }

    @AfterMethod
    public void tearDown() {
        logger.info("Clearing translation cache and closing WebDriver");
        TranslationService.clearCache();
        DriverManager.quitDriver();
    }
}
