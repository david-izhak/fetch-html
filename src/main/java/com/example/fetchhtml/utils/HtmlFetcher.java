package com.example.fetchhtml.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Dmitry Itskov
 */
@Component
public class HtmlFetcher {

    @Autowired
    private HtmlSaver htmlSaver;

    @Value("${fetcher.max_urls_from_page}")
    private long maxUrlsFromPage;

    @Value("${fetcher.is_cross_level_uniqueness}")
    private boolean isCrossLevelUniqueness;

    private final static Set<String> globalUrls = new HashSet<>(); // App uses this set to check if URL uniq or not

    @Async
    public CompletableFuture<Set<String>> fetchHtmlDocument(String url, int depth) throws IOException {
        try {
            Document document = Jsoup.connect(url).get();
            htmlSaver.saveHtmlDocumentToFile(document, depth, url);
            Set<String> result = findUniqUrlsInDocument(document);
            return CompletableFuture.completedFuture(result);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Set<String> findUniqUrlsInDocument(Document htmlDocument) {
        Elements htmlLinks = htmlDocument.select("a");
        return htmlLinks.stream()
                .map(a -> a.absUrl("href"))
                .filter(b -> !b.isEmpty())
                .filter(c -> !isCrossLevelUniqueness || !globalUrls.contains(c))
                .peek(globalUrls::add)
                .limit(maxUrlsFromPage)
                .collect(Collectors.toSet());
    }
}
