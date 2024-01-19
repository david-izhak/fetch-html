package com.example.fetchhtml.service;

import com.example.fetchhtml.utils.HtmlFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Dmitry Itskov
 */
@Service
public class HtmlHandler {

    @Autowired
    private HtmlFetcher htmlFetcher;

    @Value("${fetcher.base_url}")
    private String baseUrl;

    @Value("${fetcher.depth_factor}")
    private int depthFactor;

    @PostConstruct
    public void startHandlingUrls() {
        Set<String> urlsForNewJob = new HashSet<>();
        urlsForNewJob.add(baseUrl);
        for (int i = 0; i <= depthFactor; i++) {
            urlsForNewJob = handleUrls(urlsForNewJob, i);
        }
    }

    private Set<String> handleUrls(Set<String> urls, int currentDepth) {
        Set<String> urlsForNewJob = new HashSet<>();
        for (String url : urls) {
            CompletableFuture<Set<String>> childrenUrlsSet = null;
            try {
                childrenUrlsSet = htmlFetcher.fetchHtmlDocument(url, currentDepth);
            } catch (IOException e) {
                e.printStackTrace();
            }
            urlsForNewJob.addAll(childrenUrlsSet != null ? childrenUrlsSet.join() : Collections.emptySet());
        }
        return urlsForNewJob;
    }
}
