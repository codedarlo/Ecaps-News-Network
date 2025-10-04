package com.enn;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.HashSet;
import java.util.Set;

public class UrlArticleExtraction {

    // Minimum content length to be considered valid article content
    private static final int MIN_CONTENT_LENGTH = 100;
    
    // Maximum reasonable title length (flags clickbait/errors)
    private static final int MAX_TITLE_LENGTH = 200;

    
    //title extraction//
    public static String extractTitle(Document doc) {
        if (doc == null) return null;

         // Try H1 first (most common for articles)
        Element h1 = doc.selectFirst("article h1, .article h1, main h1, h1");
        if (h1 != null) {
            String title = cleanText(h1.text());
            if (isValidTitle(title)) {
                return title;
            }
        }
        
        // Try OpenGraph title
        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null) {
            String title = cleanText(ogTitle.attr("content"));
            if (isValidTitle(title)) {
                return title;
            }
        }
        
        // Fall back to title tag
        Element titleTag = doc.selectFirst("title");
        if (titleTag != null) {
            String title = cleanText(titleTag.text());
            // Remove common site suffixes (e.g., " - BBC News", " | CNN")
            title = title.replaceFirst("\\s*[-|]\\s*[A-Z][A-Za-z\\s]+$", "");
            if (isValidTitle(title)) {
                return title;
            }
        }
        
        return null;
    }
    
    /**
     * Validate title is reasonable
     */
    private static boolean isValidTitle(String title) {
        if (title == null || title.length() < 10) return false;
        if (title.length() > MAX_TITLE_LENGTH) return false;
        // Check it's not just a URL or error message
        return !title.startsWith("http") && !title.toLowerCase().contains("error");
    }
    /**
     * Extract publication date with comprehensive fallback chain
     * Returns null if no valid date found (don't return error strings!)
     */
    
    //publication date//
    public static String extractPublication(Document doc) {
        if (doc == null) return null;
        
        // Priority order of selectors (most reliable first)
        String[] selectors = {
            "meta[property=article:published_time]",
            "meta[name=publish-date]",
            "meta[name=date]",
            "meta[itemprop=datePublished]",
            "meta[property=og:article:published_time]",
            "meta[name=DC.date.issued]",
            "time[datetime]",
            "time[itemprop=datePublished]",
            "span[itemprop=datePublished]",
            "div[itemprop=datePublished]",
            "meta[name=pubdate]",
            "meta[property=publication_date]",
            "span.date, span.timestamp, span.published",
            "div.date, div.timestamp, div.published",
            "p.date, p.timestamp, p.published",
            "time.pubdate, time.published",
            "abbr.published"
        };

        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element != null) {
                // Try datetime attribute first (most reliable)
                String date = element.attr("datetime");
                if (date == null || date.isEmpty()) {
                    date = element.attr("content");
                }
                if (date == null || date.isEmpty()) {
                    date = element.text();
                }
                
                date = cleanText(date);
                if (isValidDate(date)) {
                    return date;
                }
            }
        }
        
        return null;
    }
        /**
     * Basic validation that string looks like a date
     */
    private static boolean isValidDate(String date) {
        if (date == null || date.length() < 4) return false;
        // Must contain at least one digit (year/day/month)
        return date.matches(".*\\d+.*");
    }
    /**
     * Extract author with validation
     * Returns null if no valid author found
     */
    public static String extractAuthor(Document doc) {
        if (doc == null) return null;
        
        String[] selectors = {
            "meta[name=author]",
            "meta[property=article:author]",
            "meta[name=byl]",
            "span.author, span.byline, span.by-author",
            "div.author, div.byline, div.by-author",
            "a.author, a.byline",
            "p.byline, p.author",
            "[rel=author]",
            "[itemprop=author]"
        };
        
        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element != null) {
                String author = element.attr("content");
                if (author == null || author.isEmpty()) {
                    author = element.text();
                }
                
                author = cleanAuthor(author);
                if (isValidAuthor(author)) {
                    return author;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Clean and validate author name
     */
    private static String cleanAuthor(String author) {
        if (author == null) return null;
        
        // Remove common prefixes
        author = author.replaceFirst("(?i)^(by|written by|author:?)\\s+", "");
        author = cleanText(author);
        
        return author;
    }
    
    /**
     * Validate author looks like a real name
     */
    private static boolean isValidAuthor(String author) {
        if (author == null || author.length() < 3) return false;
        if (author.length() > 100) return false; // Too long to be a name
        
        // Must start with capital letter
        if (!Character.isUpperCase(author.charAt(0))) return false;
        
        // Should have at least first and last name (2 words)
        String[] words = author.split("\\s+");
        if (words.length < 2) return false;
        
        // Filter out common false positives
        String lower = author.toLowerCase();
        if (lower.contains("staff") || lower.contains("editor") || 
            lower.contains("reporter") || lower.contains("team") ||
            lower.contains("news") || lower.contains("desk")) {
            return false;
        }
        
        return true;
    }
    
    //content extraction//
    public static String extractContent(Document doc) {
        //removing unwanted items before extracting content, such as ads, videos, related articles.
        doc.select("aside, .related-articles, .promo, .advertisement, .ssrcss-1sen9vx-PromoHeadline, video, iframe, .video-container, .media-player").remove();

        //select the article
        Elements paragraphs = doc.select("article p, div[class*=content] p, div[class*=article-body] p, " +
                "div[class*=story] p, section[class*=content] p");


        if (paragraphs.isEmpty()) {
            return "No article content found.";
        }

        StringBuilder contentBuilder = new StringBuilder();

        for (Element paragraph : paragraphs) {
            contentBuilder.append(paragraph.text()).append("\n\n"); // Add spacing for readability
        }

        return contentBuilder.toString().trim();
    }
}
/**
     * BULLETPROOF content extraction
     * Key improvements:
     * 1. Identifies main article container
     * 2. Removes noise (ads, related articles, navigation)
     * 3. Validates content quality
     * 4. Prevents false positives from other page elements
     */
    public static String extractContent(Document doc) {
        if (doc == null) return null;
        
        // PHASE 1: Remove all noise BEFORE extracting content
        removeNoise(doc);
        
        // PHASE 2: Find the main article container
        Element articleContainer = findArticleContainer(doc);
        
        if (articleContainer == null) {
            return null; // No valid article found
        }
        
        // PHASE 3: Extract paragraphs from the container
        Elements paragraphs = articleContainer.select("p");
        
        if (paragraphs.isEmpty()) {
            return null;
        }
        
        // PHASE 4: Filter and assemble content
        StringBuilder contentBuilder = new StringBuilder();
        Set<String> seenText = new HashSet<>(); // Prevent duplicates
        
        for (Element p : paragraphs) {
            String text = p.text().trim();
            
            // Skip if too short (likely not article content)
            if (text.length() < 20) continue;
            
            // Skip if we've seen this exact text (handles duplicates)
            if (seenText.contains(text)) continue;
            
            // Skip if it looks like navigation/boilerplate
            if (isBoilerplate(text)) continue;
            
            seenText.add(text);
            contentBuilder.append(text).append("\n\n");
        }
        
        String content = contentBuilder.toString().trim();
        
        // PHASE 5: Validate final content
        if (content.length() < MIN_CONTENT_LENGTH) {
            return null; // Too short to be real article
        }
        
        return content;
    }
    
    /**
     * Remove all noise elements before extraction
     */
    private static void removeNoise(Document doc) {
        // Ads and promotions
        doc.select("aside, .advertisement, .ad, .promo, .sponsored").remove();
        
        // Related content and recommendations
        doc.select(".related-articles, .related-content, .recommendations, " +
                  ".more-stories, .also-read, .you-may-like").remove();
        
        // Social and sharing elements
        doc.select(".social-share, .share-buttons, .social-links").remove();
        
        // Navigation and menus
        doc.select("nav, .navigation, .menu, header, footer").remove();
        
        // Comments section
        doc.select(".comments, #comments, .comment-section, .discussion").remove();
        
        // Video and media players (keep images but remove players)
        doc.select("video, iframe, .video-container, .media-player, " +
                  ".video-player, .player-container").remove();
        
        // Banners and pop-ups
        doc.select(".banner, .popup, .modal, .newsletter-signup").remove();
        
        // BBC-specific noise
        doc.select(".ssrcss-1sen9vx-PromoHeadline, .promo-text").remove();
        
        // Cookie notices and legal
        doc.select(".cookie-notice, .gdpr-notice, .privacy-banner").remove();
    }
    
    /**
     * Find the main article container using multiple strategies
     */
    private static Element findArticleContainer(Document doc) {
        // Strategy 1: Look for semantic HTML5 article tag
        Element article = doc.selectFirst("article");
        if (article != null && hasSubstantialContent(article)) {
            return article;
        }
        
        // Strategy 2: Look for main content area
        Element main = doc.selectFirst("main");
        if (main != null && hasSubstantialContent(main)) {
            return main;
        }
        
        // Strategy 3: Look for common content container classes/IDs
        String[] containerSelectors = {
            ".article-body",
            ".article-content",
            ".story-body",
            ".story-content",
            ".entry-content",
            ".post-content",
            ".content-body",
            "#article-body",
            "#story-body",
            "[itemprop=articleBody]"
        };
        
        for (String selector : containerSelectors) {
            Element container = doc.selectFirst(selector);
            if (container != null && hasSubstantialContent(container)) {
                return container;
            }
        }
        
        // Strategy 4: Find the element with the most paragraph text
        return findElementWithMostText(doc);
    }
    
    /**
     * Check if element has substantial content (not just navigation/fluff)
     */
    private static boolean hasSubstantialContent(Element element) {
        String text = element.text();
        if (text.length() < MIN_CONTENT_LENGTH) return false;
        
        // Count paragraphs with meaningful content
        long meaningfulParas = element.select("p").stream()
            .filter(p -> p.text().length() > 20)
            .count();
        
        return meaningfulParas >= 3; // At least 3 real paragraphs
    }
    
    /**
     * Find element with most paragraph text (last resort)
     */
    private static Element findElementWithMostText(Document doc) {
        Element best = null;
        int maxLength = 0;
        
        // Check divs and sections
        for (Element element : doc.select("div, section")) {
            Elements paragraphs = element.select("p");
            int totalLength = paragraphs.stream()
                .mapToInt(p -> p.text().length())
                .sum();
            
            if (totalLength > maxLength) {
                maxLength = totalLength;
                best = element;
            }
        }
        
        return maxLength >= MIN_CONTENT_LENGTH ? best : null;
    }
    
    /**
     * Detect boilerplate text that's not article content
     */
    private static boolean isBoilerplate(String text) {
        String lower = text.toLowerCase();
        
        // Common boilerplate phrases
        String[] boilerplatePatterns = {
            "click here",
            "read more",
            "sign up",
            "subscribe",
            "follow us",
            "share this",
            "all rights reserved",
            "copyright ©",
            "privacy policy",
            "terms of use",
            "cookie policy",
            "view gallery",
            "image caption",
            "related topics",
            "more on this story"
        };
        
        for (String pattern : boilerplatePatterns) {
            if (lower.contains(pattern)) {
                return true;
            }
        }
        
        // Check if it's just a link/CTA
        if (text.split("\\s+").length < 5 && lower.matches(".*(click|tap|watch|listen).*")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Clean extracted text (remove extra whitespace, etc.)
     */
    private static String cleanText(String text) {
        if (text == null) return null;
        
        text = text.trim();
        // Normalize whitespace
        text = text.replaceAll("\\s+", " ");
        // Remove zero-width characters
        text = text.replaceAll("[\\u200B-\\u200D\\uFEFF]", "");
        
        return text.isEmpty() ? null : text;
    }
}



