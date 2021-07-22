package com.example.fetchhtml.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Dmitry Itskov
 */
@Component
public class HtmlFetcher {

    @Value("${fetcher.directory_for_url}")
    private String htmlsDirectory;

    @Value("${fetcher.max_urls_from_page}")
    private long maxUrlsFromPage;

    @Value("${fetcher.is_cross_level_uniqueness}")
    private boolean isCrossLevelUniqueness;

    private final static Set<String> globalUrls = new HashSet<>(); // App uses this set to check if URL uniq or not

    @Async
    public CompletableFuture<Set<String>> fetchHtmlDocument(String url, int depth) throws IOException {
        try {
            Document document = Jsoup.connect(url).get();
            saveHtmlDocumentToFile(document, depth, url);
            Set<String> result = findUniqUrlsInDocument(document);
            return CompletableFuture.completedFuture(result);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void saveHtmlDocumentToFile(Document doc, int depth, String url) {
        String urlWithoutUtm = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
        String urlWithoutSpecificChars = urlWithoutUtm
                .replace("://", "_")
                .replace("/", "_");
        String dirName = htmlsDirectory + depth + "/";
        try {
            Files.createDirectories(Paths.get(dirName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fullFileName = dirName + urlWithoutSpecificChars + ".html";
        try {
            Files.write(
                    Paths.get(fullFileName),
                    doc.outerHtml().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
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
