package com.enn;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Set;

public class CredibilityScanner {

    private static final Set<String> trustedSources = Set.of(
            "bbc", "reuters", "guardian", "ap", "new york times", "cnn", "sky news", "npr", "bloomberg"
    );

    public static void analyzeMetadata(String title, String author, String publicationDate, String content) {
        System.out.println("\n🧐 Credibility Scan Report:");
        System.out.println("------------------------------------------------");

        double datePercent = calculateDateScore(publicationDate);
        double authorPercent = calculateAuthorScore(author);
        double relatedPercent = calculateRelatedArticlesScore(title);

        double finalScore = (datePercent + authorPercent + relatedPercent) / 3.0;

        System.out.printf("📅 Date Score: %.1f%%\n", datePercent);
        System.out.printf("✍️ Author Score: %.1f%%\n", authorPercent);
        System.out.printf("🌐 Related Articles Score: %.1f%%\n", relatedPercent);

        System.out.println("------------------------------------------------");
        System.out.printf("🧪 Final Credibility Score: %.1f%%\n", finalScore);

        if (finalScore < 50) {
            System.out.println("⚠️  This article may lack credible backing. Cross-check recommended.");
        } else if (finalScore < 75) {
            System.out.println("ℹ️  Moderate credibility. Some metadata is present but not ideal.");
        } else {
            System.out.println("✅ Good credibility. Author/date/source seem solid.");
        }
    }

    public static double calculateDateScore(String publicationDate) {
        try {
            LocalDate pubDate = LocalDate.parse(publicationDate, DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH));
            long daysOld = ChronoUnit.DAYS.between(pubDate, LocalDate.now());

            if (daysOld > 365) return 100.0;
            if (daysOld > 180) return 75.0;
            if (daysOld > 30) return 50.0;
            return 25.0;
        } catch (Exception e) {
            return 40.0;
        }
    }

    public static double calculateAuthorScore(String author) {
        for (String known : trustedSources) {
            if (author != null && author.toLowerCase().contains(known)) {
                return 100.0;
            }
        }
        return 35.0;
    }

    public static double calculateRelatedArticlesScore(String title) {
        if (title == null || title.isBlank()) return 40.0;

        String normalized = title.toLowerCase();
        for (String source : trustedSources) {
            if (normalized.contains(source)) {
                return 100.0;
            }
        }
        return 60.0;
    }
}

