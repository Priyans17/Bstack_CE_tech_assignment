package com.elpais.automation.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.ArrayList;
import java.util.List;

// Page Object for El País Opinion section
public class OpinionPage extends BasePage {
    private static final Logger logger = LogManager.getLogger(OpinionPage.class);

    // Locators
    private static final By ARTICLE_ITEMS = By.xpath("//article[contains(@class, 'c c-o')]");
    private static final By ARTICLE_TITLE = By.cssSelector("h2.c_t, h2[class*='c_t'], h2 a");
    private static final By ARTICLE_CONTENT = By.cssSelector("p.c_d, p[class*='c_d'], .c_d, .article_description");
    private static final By ARTICLE_LINK = By.cssSelector("h2 a");
    private static final By ARTICLE_IMAGE = By.cssSelector("img.c_m_e, img[class*='c_m_e'], img");

    public OpinionPage(WebDriver driver) {
        super(driver);
    }

    // Wait for articles to load
    public void waitForArticlesLoad() {
        logger.info("Waiting for articles to load");
        try {
            waitForElementPresent(ARTICLE_ITEMS);
        } catch (Exception e) {
            logger.warn("Article items not found with default locator, waiting for generic article tags");
            waitForElementPresent(By.tagName("article"));
        }
    }

    // Get all article elements
    private List<WebElement> getAllArticles() {
        logger.info("Fetching all articles");
        List<WebElement> articles = getElements(ARTICLE_ITEMS);
        if (articles.isEmpty()) {
            articles = getElements(By.tagName("article"));
        }
        return articles;
    }

    // Get first N articles with their details
    public List<ArticleData> getTopArticles(int count) {
        logger.info("Getting top {} articles", count);
        waitForArticlesLoad();

        List<ArticleData> articles = new ArrayList<>();
        List<WebElement> articleElements = getAllArticles();

        for (int i = 0; i < Math.min(count, articleElements.size()); i++) {
            try {
                WebElement article = articleElements.get(i);
                scrollToElement(article);
                Thread.sleep(2000); // Allow time for images/content to load after scroll

                String title = "";
                String content = "";
                String imageUrl = "";
                String articleUrl = "";

                try {
                    title = article.findElement(ARTICLE_TITLE).getText().trim();
                } catch (Exception e) {
                    logger.debug("Could not find title for article {} using default locator", i);
                    try {
                        title = article.findElement(By.tagName("h2")).getText().trim();
                    } catch (Exception e2) {}
                }

                try {
                    content = article.findElement(ARTICLE_CONTENT).getText().trim();
                } catch (Exception e) {
                    logger.debug("Could not find content for article {}", i);
                }

                try {
                    WebElement img = null;
                    try {
                        img = article.findElement(ARTICLE_IMAGE);
                    } catch (Exception e) {
                        img = article.findElement(By.tagName("img"));
                    }

                    if (img != null) {
                        imageUrl = img.getAttribute("src");
                        
                        // Check for lazy-loaded image sources
                        String dataSrc = img.getAttribute("data-src");
                        if (dataSrc != null && !dataSrc.isEmpty()) {
                            imageUrl = dataSrc;
                        }
                        
                        // Handle high-res images from srcset
                        String srcset = img.getAttribute("srcset");
                        if (srcset != null && !srcset.isEmpty()) {
                            // Take the last (usually highest res) or first
                            String[] sources = srcset.split(",");
                            imageUrl = sources[0].trim().split(" ")[0];
                        }
                    }
                    
                    // Ensure URL is absolute
                    if (imageUrl != null && imageUrl.startsWith("//")) {
                        imageUrl = "https:" + imageUrl;
                    }
                    
                    if (imageUrl == null || imageUrl.isEmpty()) {
                         logger.debug("Image URL empty for article {}", i);
                    }
                } catch (Exception e) {
                    logger.debug("Could not find image for article {}", i);
                }

                try {
                    articleUrl = article.findElement(ARTICLE_LINK).getAttribute("href");
                } catch (Exception e) {
                    logger.debug("Could not find URL for article {}", i);
                }

                if (!title.isEmpty()) {
                    articles.add(new ArticleData(title, content, imageUrl, articleUrl));
                    logger.info("Article {} scraped: {}", i + 1, title);
                }
            } catch (Exception e) {
                logger.warn("Error processing article at index {}", i, e);
            }
        }

        return articles;
    }

    // Article data holder class
    public static class ArticleData {
        public String title;
        public String content;
        public String imageUrl;
        public String articleUrl;
        public String translatedTitle;

        public ArticleData(String title, String content, String imageUrl, String articleUrl) {
            this.title = title;
            this.content = content;
            this.imageUrl = imageUrl;
            this.articleUrl = articleUrl;
        }

        @Override
        public String toString() {
            return "ArticleData{" +
                    "title='" + title + '\'' +
                    ", content='" + content + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", articleUrl='" + articleUrl + '\'' +
                    ", translatedTitle='" + translatedTitle + '\'' +
                    '}';
        }
    }
}
