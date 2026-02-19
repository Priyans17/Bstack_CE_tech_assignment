package com.elpais.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;

// Utility for downloading and saving images
public class ImageDownloader {

    private static final Logger logger = LogManager.getLogger(ImageDownloader.class);
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    // Download image from URL using Selenium to bypass 403
    public static String downloadImageWithSelenium(String imageUrl, String outputPath, WebDriver driver) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "";
        }

        try {
            new File(outputPath).mkdirs();
            String filename = "img_" + System.currentTimeMillis() + ".png";
            String filePath = outputPath + File.separator + filename;

            String originalUrl = driver.getCurrentUrl();
            driver.get(imageUrl);
            
            byte[] imageBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(imageBytes);
            }
            
            driver.get(originalUrl);
            logger.info("Image captured via Selenium: {}", filePath);
            return filePath;
        } catch (Exception e) {
            logger.error("Failed to capture image via Selenium", e);
            return "";
        }
    }
    public static String downloadImage(String imageUrl, String outputPath) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            logger.warn("Image URL is empty");
            return "";
        }

        try {
            // Clean up URL
            imageUrl = imageUrl.split("\\?")[0]; // Remove query parameters
            if (!imageUrl.startsWith("http")) {
                imageUrl = "https:" + imageUrl;
            }

            // Create directory if not exists
            new File(outputPath).mkdirs();

            // Generate filename from URL
            String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            if (filename.isEmpty() || !filename.contains(".")) {
                filename = "image_" + System.currentTimeMillis() + ".jpg";
            }

            String filePath = outputPath + File.separator + filename;

            // Download file with browser-like headers
            logger.info("Downloading image from: {}", imageUrl);
            URL url = new URL(imageUrl);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // Important headers to avoid 403 from ElPais CDN
            connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
            );
            connection.setRequestProperty("Referer", "https://elpais.com/");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9,es;q=0.8");
            connection.setRequestProperty("Sec-Fetch-Dest", "image");
            connection.setRequestProperty("Sec-Fetch-Mode", "no-cors");
            connection.setRequestProperty("Sec-Fetch-Site", "cross-site");
            connection.setRequestProperty("Connection", "keep-alive");

            try (InputStream inputStream = connection.getInputStream(); FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }

                logger.info("Image downloaded successfully: {}", filePath);
                return filePath;
            }
        } catch (Exception e) {
            logger.error("Error downloading image from URL: {}", imageUrl, e);
            return "";
        }
    }

    // Download image asynchronously
    public static void downloadImageAsync(String imageUrl, String outputPath) {
        executorService.submit(() -> downloadImage(imageUrl, outputPath));
    }

    // Shutdown executor service
    public static void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                logger.warn("Executor service forced shutdown");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            logger.error("Error shutting down executor service", e);
        }
    }

    // Check if file exists
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
}
