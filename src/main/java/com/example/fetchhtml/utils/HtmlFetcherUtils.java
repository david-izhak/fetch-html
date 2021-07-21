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
public class HtmlFetcherUtils {

    @Value("${fetcher.directory_for_url}")
    private String htmlsDirectory;

    @Value("${fetcher.max_urls_from_page}")
    private long maxUrlsFromPage;

    @Value("${fetcher.is_cross_level_uniqueness}")
    private boolean isCrossLevelUniqueness;

    private final static Set<String> globalUrls = new HashSet<>(); // App uses this set to check if URL uniq or not

    // async variant
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Set<String>> fetchHtmlDoc(String url, int depth) throws IOException {
        try {
            Document doc = Jsoup.connect(url).get();
            saveHtmlDocToFile(doc, depth, url);
            final Document finalDoc = doc;
            Set<String> res = findUniqUrlsInDoc(finalDoc);
            return CompletableFuture.completedFuture(res);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

    }

    // one thread variant
//    public Set<String> fetchHtmlDoc(String url, int depth) {
//        Document doc = null;
//        try {
//            doc = Jsoup.connect(url).get();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        saveHtmlDocToFile(doc, depth, url);
//        final Document finalDoc = doc;
//        return  findUniqUrlsInDoc(finalDoc);
//    }

    public void saveHtmlDocToFile(Document doc, int depth, String url) {
        String urlWithoutUtm;
        if (url.contains("?")) {
            urlWithoutUtm = url.substring(0, url.indexOf("?"));
        } else {
            urlWithoutUtm = url;
        }
        String urlWithoutSpecificChars = urlWithoutUtm.replace("://", "_").replace("/", "_").replace("?", "_");
        String dirName = htmlsDirectory + depth + "/";
        String fullFileName = dirName + urlWithoutSpecificChars + ".html";
        try {
            Files.createDirectories(Paths.get(dirName));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public Set findUniqUrlsInDoc(Document htmlDocument) {
        Elements htmlLinks = htmlDocument.select("a");
        return htmlLinks.stream()
                .map(a -> a.absUrl("href"))
                .filter(a -> !a.isEmpty())
                .filter(b -> isCrossLevelUniqueness ? !globalUrls.contains(b) : true)
                .peek(c -> globalUrls.add(c))
                .limit(maxUrlsFromPage)
                .collect(Collectors.toSet());
    }
}
