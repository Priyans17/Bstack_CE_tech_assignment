package com.elpais.automation.utils;

import com.elpais.automation.config.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// Translation service using Rapid Translate Multi Traduction API
public class TranslationService {
    private static final Logger logger = LogManager.getLogger(TranslationService.class);
    private static final Map<String, String> translationCache = new HashMap<>();
    private static final Gson gson = new Gson();

    private static final String RAPIDAPI_ENDPOINT = "https://rapid-translate-multi-traduction.p.rapidapi.com/t";

    // Translate text from Spanish to English using RapidAPI
    public static String translateToEnglish(String spanishText) {
        if (spanishText == null || spanishText.isEmpty()) {
            return "";
        }

        // Check cache first
        String cacheKey = "es-en-" + spanishText;
        if (translationCache.containsKey(cacheKey)) {
            logger.debug("Translation found in cache: {}", spanishText);
            return translationCache.get(cacheKey);
        }

        try {
            String translation = translateUsingRapidAPI(spanishText);
            translationCache.put(cacheKey, translation);
            logger.info("Translated: '{}' -> '{}'", spanishText, translation);
            return translation;
        } catch (Exception e) {
            logger.error("Error translating text: {}", spanishText, e);
            return spanishText; // Return original text if translation fails
        }
    }

    // Use Rapid Translate Multi Traduction API
    private static String translateUsingRapidAPI(String text) {
        String apiKey = ConfigManager.get("RAPIDAPI_KEY");
        String apiHost = ConfigManager.get("RAPIDAPI_HOST", "rapid-translate-multi-traduction.p.rapidapi.com");

        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your_rapidapi_key_here")) {
            logger.warn("RapidAPI Key is not configured. Returning original text.");
            return text;
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(RAPIDAPI_ENDPOINT);
            httpPost.setHeader("x-rapidapi-key", apiKey);
            httpPost.setHeader("x-rapidapi-host", apiHost);
            httpPost.setHeader("Content-Type", "application/json");

            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("from", "es");
            jsonBody.addProperty("to", "en");
            jsonBody.addProperty("text", text);

            StringEntity entity = new StringEntity(gson.toJson(jsonBody), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                
                // Check if the response contains error message
                if (responseBody.contains("message") || responseBody.contains("error")) {
                    logger.warn("RapidAPI Error or Message: {}. Returning original text.", responseBody);
                    return text;
                }

                // The API might return a JSON string "Translated Text" or an object
                if (responseBody.startsWith("\"") && responseBody.endsWith("\"")) {
                    return responseBody.substring(1, responseBody.length() - 1);
                }
                
                // Fallback for different response formats if needed
                try {
                    JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class);
                    if (jsonElement.isJsonPrimitive()) {
                        return jsonElement.getAsString();
                    }
                } catch (Exception e) {
                    // Not JSON or unexpected format
                }

                return responseBody;
            }
        } catch (Exception e) {
            logger.error("RapidAPI translation error", e);
            throw new RuntimeException("Translation failed", e);
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                logger.error("Error closing HTTP client", e);
            }
        }
    }

    // Translate multiple texts
    public static Map<String, String> translateMultiple(Map<String, String> textsToTranslate) {
        Map<String, String> translatedTexts = new HashMap<>();
        for (Map.Entry<String, String> entry : textsToTranslate.entrySet()) {
            translatedTexts.put(entry.getKey(), translateToEnglish(entry.getValue()));
        }
        return translatedTexts;
    }

    // Clear translation cache
    public static void clearCache() {
        translationCache.clear();
        logger.info("Translation cache cleared");
    }
}
