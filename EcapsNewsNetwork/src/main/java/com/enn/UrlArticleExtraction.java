package com.enn;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class UrlArticleExtraction {

    //title extraction//
    public static String extractTitle(Document doc) {
        Element titleElement = doc.selectFirst("H1, title");// h1 and title are the most common uses of defining the title in html
        return (titleElement != null) ? titleElement.text() : "no title found";// error checking to stop the program from searching if there isn't a title to be found
    }

    //publication date//
    public static String extractPublication(Document doc) {
        Element publicationElement = doc.selectFirst(
                "time," + "meta[property=article:published_time], " +
                "span[class*=date]," + "div[class*=date]," + "meta[name=date]," +
                        "meta[itemprop=datePublished]," + "meta[name=pubdate], " +
                        "p[class*=date], " + "time[class*=timestamp], " + "time[class*=pubdate], " +
                        "abbr[class*=published], " + "span[itemprop*=datePublished], " + "div[itemprop*=datePublished], " +
                        "meta[name=DC.date.issued], " + "meta[property=og:article:published_time], " + "meta[name=article:published], " + "meta[property=publication_date]");
        return (publicationElement != null) ? publicationElement.text() : "No publication date associated" ;
    }

    //extracting the author(s)//
    public static String extractAuthor(Document doc) {
        Element authorElement = doc.selectFirst("meta[name=author], meta[property=article:author], " +
                "div[class*=author], span[class*=author], a[class*=author], " +
                "p[class*=byline], div[class*=byline], span[class*=byline]");
        return (authorElement != null) ? authorElement.text() : "no author named /or found" ;
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



