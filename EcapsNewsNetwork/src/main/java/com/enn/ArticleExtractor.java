package com.enn;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Orchestration layer that detects input type and delegates to specialized extractors
 */
public class ArticleExtractor {
    
    private static final int TIMEOUT_MS = 15000; // 15 second timeout
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    
    /**
     * Data container for extracted article information
     */
    public static class ArticleData {
        public final String title;
        public final String author;
        public final String publicationDate;
        public final String publisher;
        public final String content;
        public final String sourceType; // "URL" or "TEXT"
        public final String originalInput;
        
        public ArticleData(String title, String author, String publicationDate, 
                          String publisher, String content, String sourceType, String originalInput) {
            this.title = title;
            this.author = author;
            this.publicationDate = publicationDate;
            this.publisher = publisher;
            this.content = content;
            this.sourceType = sourceType;
            this.originalInput = originalInput;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Title: ").append(title != null ? title : "Unknown").append("\n");
            sb.append("Author: ").append(author != null ? author : "Unknown").append("\n");
            sb.append("Publication Date: ").append(publicationDate != null ? publicationDate : "Unknown").append("\n");
            sb.append("Publisher: ").append(publisher != null ? publisher : "Unknown").append("\n");
            sb.append("Source Type: ").append(sourceType).append("\n");
            sb.append("Content Length: ").append(content != null ? content.length() + " chars" : "None");
            return sb.toString();
        }
        
        /**
         * Check if critical data is missing
         */
        public boolean hasCriticalData() {
            return title != null || content != null;
        }
        
        /**
         * Get completeness score (0-100)
         */
        public int getCompletenessScore() {
            int score = 0;
            if (title != null && !title.isEmpty()) score += 20;
            if (author != null && !author.isEmpty()) score += 20;
            if (publicationDate != null && !publicationDate.isEmpty()) score += 20;
            if (publisher != null && !publisher.isEmpty()) score += 20;
            if (content != null && content.length() > 100) score += 20;
            return score;
        }
    }
    
    /**
     * Main entry point - automatically detects input type and extracts data
     */
    public static ArticleData extract(String input) throws ExtractionException {
        if (input == null || input.isBlank()) {
            throw new ExtractionException("Input cannot be null or empty");
        }
        
        String trimmed = input.trim();
        
        if (isUrl(trimmed)) {
            return extractFromUrl(trimmed);
        } else {
            return extractFromText(trimmed);
        }
    }
    
    /**
     * Check if input string is a valid URL
     */
    private static boolean isUrl(String input) {
        try {
            URI uri = new URI(input);
            String scheme = uri.getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
        } catch (URISyntaxException e) {
            return false;
        }
    }
    
    /**
     * Extract article data from URL
     */
    private static ArticleData extractFromUrl(String url) throws ExtractionException {
        Document doc;
        
        try {
            System.out.println("🌐 Fetching URL: " + url);
            doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .ignoreHttpErrors(false) // Throw exception on 404, 403, etc.
                    .get();
            System.out.println("✅ Successfully fetched page");
            
        } catch (IOException e) {
            throw new ExtractionException("Failed to fetch URL: " + e.getMessage(), e);
        }
        
        // Extract publisher from domain
        String publisher = extractPublisherFromUrl(url);
        
        // Use your existing extraction methods
        String title = UrlArticleExtraction.extractTitle(doc);
        String author = UrlArticleExtraction.extractAuthor(doc);
        String publicationDate = UrlArticleExtraction.extractPublication(doc);
        String content = UrlArticleExtraction.extractContent(doc);
        
        // Convert error strings to null
        title = cleanExtractedValue(title, "no title found");
        author = cleanExtractedValue(author, "no author named /or found");
        publicationDate = cleanExtractedValue(publicationDate, "No publication date associated");
        content = cleanExtractedValue(content, "No article content found");
        
        return new ArticleData(title, author, publicationDate, publisher, content, "URL", url);
    }
    
    /**
     * Extract article data from raw text
     */
    private static ArticleData extractFromText(String text) {
        System.out.println("📝 Parsing text input (" + text.length() + " characters)");
        
        String title = TextArticleExtraction.extractTitle(text);
        String author = TextArticleExtraction.extractAuthor(text);
        String publicationDate = TextArticleExtraction.extractPublication(text);
        String content = TextArticleExtraction.extractContent(text);
        
        // Convert error strings to null
        title = cleanExtractedValue(title, "No title found");
        author = cleanExtractedValue(author, "no author found");
        publicationDate = cleanExtractedValue(publicationDate, "No publication Date found");
        
        // Try to extract publisher from text content
        String publisher = extractPublisherFromText(text);
        
        return new ArticleData(title, author, publicationDate, publisher, content, "TEXT", null);
    }
    
    /**
     * Extract publisher/source name from URL domain
     */
    private static String extractPublisherFromUrl(String urlString) {
        try {
            URI uri = new URI(urlString);
            String host = uri.getHost();
            
            if (host == null) return null;
            
            // Remove www. prefix
            host = host.toLowerCase();
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            
            // Extract main domain (before TLD)
            String[] parts = host.split("\\.");
            if (parts.length >= 2) {
                String mainDomain = parts[0];
                
                // Capitalize and format known publishers
                return formatPublisherName(mainDomain);
            }
            
            return formatPublisherName(host);
            
        } catch (URISyntaxException e) {
            return null;
        }
    }
    
    /**
     * Try to extract publisher from text content
     */
    private static String extractPublisherFromText(String text) {
        // Common patterns for publisher mentions
        String[] patterns = {
            "(?i)(?:published by|source:|via)\\s+([A-Z][A-Za-z\\s&]+?)(?:\\.|,|$)",
            "(?i)\\b(BBC|Reuters|Guardian|CNN|AP|NPR|Bloomberg|New York Times|Sky News|Washington Post|Financial Times)\\b"
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(text);
            if (m.find()) {
                return m.group(1).trim();
            }
        }
        
        return null;
    }
    
    /**
     * Format publisher name with proper capitalization
     */
    private static String formatPublisherName(String domain) {
        // Known mappings
        switch (domain.toLowerCase()) {
            case "bbc": return "BBC";
            case "cnn": return "CNN";
            case "npr": return "NPR";
            case "ap": return "Associated Press";
            case "nytimes": return "New York Times";
            case "theguardian": return "The Guardian";
            case "reuters": return "Reuters";
            case "bloomberg": return "Bloomberg";
            case "washingtonpost": return "Washington Post";
            case "ft": return "Financial Times";
            case "wsj": return "Wall Street Journal";
            case "forbes": return "Forbes";
            case "economist": return "The Economist";
            case "time": return "Time Magazine";
            case "newsweek": return "Newsweek";
            case "skynews": return "Sky News";
            default:
                // Capitalize first letter of each word
                return capitalizeWords(domain.replace("-", " ").replace("_", " "));
        }
    }
    
    /**
     * Convert error message strings to null
     */
    private static String cleanExtractedValue(String value, String errorMessage) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase(errorMessage)) {
            return null;
        }
        return value.trim();
    }
    
    /**
     * Capitalize first letter of each word
     */
    private static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        
        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Custom exception for extraction failures
     */
    public static class ExtractionException extends Exception {
        public ExtractionException(String message) {
            super(message);
        }
        
        public ExtractionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
