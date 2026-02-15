package com.elpais.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.util.stream.Collectors;

// Utility for analyzing word frequency in text
public class WordFrequencyAnalyzer {
    private static final Logger logger = LogManager.getLogger(WordFrequencyAnalyzer.class);

    // Common stop words in Spanish and English
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        // English
        "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "from",
        "is", "are", "am", "be", "been", "being", "have", "has", "had", "do", "does", "did", "will", "would",
        "could", "should", "may", "might", "must", "can", "shall", "as", "if", "because", "while", "when",
        "where", "what", "which", "who", "whom", "whose", "why", "how", "all", "each", "every", "both",
        "few", "more", "most", "other", "some", "such", "that", "this", "these", "those", "i", "you", "he",
        "she", "it", "we", "they", "me", "him", "her", "us", "them", "my", "your", "his", "her", "its", "our",
        "their", "no", "not", "nor", "so", "up", "out", "it's", "that's", "what's", "it", "",

        // Spanish
        "el", "la", "los", "las", "un", "una", "unos", "unas", "de", "del", "al", "a", "ante", "bajo",
        "cabe", "con", "contra", "desde", "durante", "entre", "hacia", "hasta", "para", "por", "según",
        "sin", "sobre", "tras", "y", "o", "u", "pero", "mas", "sino", "que", "quien", "el", "ella",
        "ello", "ellas", "ellos", "me", "te", "se", "nos", "os", "les", "mí", "ti", "sí", "nosotros",
        "vosotros", "ustedes", "mi", "tu", "su", "nuestro", "vuestro", "mío", "tuyo", "suyo", "nuestros",
        "vuestros", "míos", "tuyos", "suyos", "este", "ese", "aquel", "esto", "eso", "aquello", "estos",
        "esos", "aquellos", "está", "estás", "estamos", "estáis", "están", "estoy", "estoy", "sea", "seas",
        "seamos", "seáis", "sean", "soy", "eres", "somos", "sois", "sois", "es", "son"
    ));

    // Analyze word frequency in given text
    // @param text The text to analyze
    // @return Map of word frequencies sorted by frequency (descending)
    public static Map<String, Integer> analyzeFrequency(String text) {
        if (text == null || text.isEmpty()) {
            logger.warn("Received empty or null text for analysis");
            return new HashMap<>();
        }

        logger.debug("Analyzing text with length: {}", text.length());

        // Convert to lowercase and split into words
        String[] words = text.toLowerCase()
                .replaceAll("[^a-záéíóúñ\\s]", "") // Keep Spanish characters
                .trim()
                .split("\\s+");

        Map<String, Integer> frequencyMap = new HashMap<>();

        for (String word : words) {
            if (!word.isEmpty() && !STOP_WORDS.contains(word) && word.length() > 2) {
                frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
            }
        }

        // Sort by frequency (descending)
        Map<String, Integer> sortedMap = frequencyMap.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        logger.info("Text analysis complete. Found {} unique words (after filtering stop words)", sortedMap.size());
        return sortedMap;
    }

    // Get top N most frequent words
    // @param text The text to analyze
    // @param topN Number of top words to return
    // @return List of top N words with frequencies
    public static List<WordFrequency> getTopWords(String text, int topN) {
        logger.debug("Getting top {} words from text", topN);

        Map<String, Integer> frequencies = analyzeFrequency(text);

        return frequencies.entrySet()
                .stream()
                .limit(topN)
                .map(e -> new WordFrequency(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    // Get word frequency statistics
    // @param text The text to analyze
    // @return Statistics object containing analysis results
    public static WordFrequencyStats getStatistics(String text) {
        logger.debug("Calculating frequency statistics");

        Map<String, Integer> frequencies = analyzeFrequency(text);

        if (frequencies.isEmpty()) {
            return new WordFrequencyStats(0, 0, 0, 0);
        }

        int totalWords = frequencies.values().stream().mapToInt(Integer::intValue).sum();
        int uniqueWords = frequencies.size();
        int maxFrequency = frequencies.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        double averageFrequency = (double) totalWords / uniqueWords;

        logger.info("Statistics - Unique words: {}, Total occurrences: {}, Avg frequency: {}, Max frequency: {}",
                uniqueWords, totalWords, String.format("%.2f", averageFrequency), maxFrequency);

        return new WordFrequencyStats(uniqueWords, totalWords, maxFrequency, averageFrequency);
    }

    // Inner class to hold word frequency data
    public static class WordFrequency {
        public String word;
        public int frequency;

        public WordFrequency(String word, int frequency) {
            this.word = word;
            this.frequency = frequency;
        }

        @Override
        public String toString() {
            return word + ": " + frequency;
        }
    }

    // Inner class to hold frequency statistics
    public static class WordFrequencyStats {
        public int uniqueWords;
        public int totalOccurrences;
        public int maxFrequency;
        public double averageFrequency;

        public WordFrequencyStats(int uniqueWords, int totalOccurrences, int maxFrequency, double averageFrequency) {
            this.uniqueWords = uniqueWords;
            this.totalOccurrences = totalOccurrences;
            this.maxFrequency = maxFrequency;
            this.averageFrequency = averageFrequency;
        }

        @Override
        public String toString() {
            return "WordFrequencyStats{" +
                    "uniqueWords=" + uniqueWords +
                    ", totalOccurrences=" + totalOccurrences +
                    ", maxFrequency=" + maxFrequency +
                    ", averageFrequency=" + String.format("%.2f", averageFrequency) +
                    '}';
        }
    }
}
