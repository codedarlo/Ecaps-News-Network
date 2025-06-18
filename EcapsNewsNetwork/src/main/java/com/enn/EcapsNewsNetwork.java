package com.enn;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.Scanner;

import com.enn.UrlArticleExtraction;
import com.enn.TextArticleExtraction;
import com.enn.Summarizer;
import com.enn.CredibilityScanner;

/*
 * SID: 2319257
 * Team Name: enn Team
 * Project: AI-Based News Summariser (Element 010)
 */



public class EcapsNewsNetwork {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("choose Input method: ");
        System.out.println("1. Enter Article URL: ");
        System.out.println("2. Enter Article text: ");
        System.out.println("Enter your choice (1 or 2)");

        int choice = scanner.nextInt();
        scanner.nextLine();

        String title, author, publicationDate, content;
        if (choice == 1) {
            System.out.println("\n Enter the Article Url: ");
            String url = scanner.nextLine();

            try {
                Document document = Jsoup.connect(url).get();

                title = UrlArticleExtraction.extractTitle(document);
                publicationDate = UrlArticleExtraction.extractPublication(document);
                author = UrlArticleExtraction.extractAuthor(document);
                content = UrlArticleExtraction.extractContent(document);

            } catch (IOException e) {
                System.err.println("Error fetching the site: " + e.getMessage());
                return;
            }
        } else if (choice == 2) {
            System.out.println("\n Enter the text below: (type 'END' on a new line to finish): ");
            StringBuilder articleText = new StringBuilder();
            String line;

            while (!(line = scanner.nextLine()).equalsIgnoreCase("END")) {
                articleText.append(line).append("\n\n");
            }
            title = TextArticleExtraction.extractTitle(articleText.toString());
            publicationDate = TextArticleExtraction.extractPublication(articleText.toString());
            author = TextArticleExtraction.extractAuthor(articleText.toString());
            content = TextArticleExtraction.extractContent(articleText.toString());

        } else {
            System.out.println("Invalid choice. please restart program");
            return;
        }


        //printing the results of the url//
        System.out.println("\n================================");
        System.out.println("title: " + title);
        System.out.println("================================");
        System.out.println("publication Date: \n" + publicationDate);
        System.out.println("================================");
        System.out.println("Author(s): \n" + author);
        System.out.println("================================");
        System.out.println("content: \n" + content);
        System.out.println("================================");

        // run creditability scan
        CredibilityScanner.analyzeMetadata(title, author, publicationDate, content);


        // Navigation to summarization mode available to the user
        System.out.println("\nwould you like to Summarize this article? (Yes/No)");
        String userResponse = scanner.nextLine().trim().toLowerCase();

        if (userResponse.equals("yes")) {
            Summarizer summarizer = new Summarizer(content);

            System.out.println("Choose summarization mode:");
            System.out.println("1. Flash Mode ");
            System.out.println("2. Insight Mode ");
            System.out.println("3. Deep Dive Mode ");
            System.out.print("Enter your choice (1, 2, or 3): ");
            String summaryChoice = scanner.nextLine().trim();


            String summary = "";

            try {
                switch (summaryChoice) {
                    case "1" -> summary = summarizer.FlashModeSummarize();
                    case "2" -> summary = summarizer.insightModeSummarize();
                    case "3" -> summary = summarizer.deepDiveModeSummarize();
                    default -> summary = "Invalid summarization mode selected.";
                }

                System.out.println("\n========== Summary ==========");
                System.out.println(summary);
                System.out.println("=============================");

            } catch (Exception e) {
                System.err.println("Error during summarization: " + e.getMessage());
            }

            System.out.println("Do you want to copy the summary or save it as PDF? (copy/pdf/skip)");
            String option = scanner.nextLine().trim().toLowerCase();

            if (option.equals("copy")) {
                CopyContent.copyToClipboard(summary);
                System.out.println("Summary copied to clipboard.");

            } else if (option.equals("pdf")) {
                System.out.print("Enter the file path to save your PDF (e.g., D:\\MySummaries\\summary.pdf): ");
                String filePath = scanner.nextLine();
                CopyContent.saveToPDF(summary, filePath);

            } else {
                System.out.println("Skipped saving.");
            }
        }

        scanner.close();
    }
}

