package com.enn;

import java.util.regex.Pattern;
import java.util.regex.Matcher;



public class TextArticleExtraction {

    public static String extractTitle(String text) {
        String[] lines = text.split("\n", 2);
        return lines.length > 0 ? lines[0] : "No title found";
    }

    public static String extractPublication(String text) {
        String[] datePatterns = {
                "\\b\\d{1,2} [A-Za-z]+ \\d{4}\\b",
                "\\b\\d{1,2}/\\d{1,2}/\\d{4}\\b"
        };

        for (String pattern : datePatterns) {
            Matcher matcher = Pattern.compile(pattern).matcher(text);
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return "No publication Date found";
    }
    public static String extractAuthor(String text) {
        //This is used to find and remove the title, this removes any names in the title that maybe considered as an author//
        String title = extractTitle(text);
        String textWithoutTitle = text.replaceFirst(Pattern.quote(title),"").trim();

        //Author name search parameters
        String namePattern = "(?i)(?<=\\bAuthor:?\\s?|\\bWritten by\\s?|\\bReported by\\s?|\\bEditor:?\\s?)" +
                "([A-Z][a-z]+(?:\\s[A-Z][a-z]+)*(?:\\s[A-Z]\\.)?(?:\\s(?:van|de|del|da|dos)\\s[A-Z][a-z]+)*)";

        Pattern pattern = Pattern.compile(namePattern);
        Matcher matcher = pattern.matcher(textWithoutTitle);

        if (matcher.find()) {
            return matcher.group(1);
        }

        String standaloneNamePattern = "\\b[A-Z][a-z]+(?:\\s[A-Z][a-z]+)*(?:\\s[A-Z]\\.)?(?:\\s(?:van|de|del|da|dos)\\s[A-Z][a-z]+)*\\b";
        Pattern fallbackPattern = Pattern.compile(standaloneNamePattern);
        Matcher fallbackMatcher = fallbackPattern.matcher(text);

        if (fallbackMatcher.find() && !title.contains(fallbackMatcher.group())){
            return fallbackMatcher.group();
        }
        return "no author found";
    }

    public static String extractContent(String text){
        return text.replaceAll("(?i)by\\s+[A-Z][a-z]+\\s[A-Z][a-z]+", "").trim();
    }

}
