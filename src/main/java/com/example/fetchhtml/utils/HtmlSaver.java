package com.example.fetchhtml.utils;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author Dmitry Itskov
 */
@Component
public class HtmlSaver {

    @Value("${fetcher.directory_for_url}")
    private String htmlsDirectory;

    @Async
    public void saveHtmlDocumentToFile(Document doc, int depth, String url) {

        String dirName = htmlsDirectory + depth + "/";
        try {
            Files.createDirectories(Paths.get(dirName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Files.write(
                    Paths.get(dirName + convertUrlToFileName(url)),
                    doc.outerHtml().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convertUrlToFileName(String url) {
        String urlWithoutUtm = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
        String urlWithoutSpecificChars = urlWithoutUtm
                .replace("://", "_")
                .replace("/", "_");
        return urlWithoutSpecificChars + ".html";
    }
}
