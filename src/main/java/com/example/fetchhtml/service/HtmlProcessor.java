package com.example.fetchhtml.service;

import com.example.fetchhtml.utils.HtmlFetcherUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Dmitry Itskov
 */
@Service
public class HtmlProcessor {

    @Autowired
    private HtmlFetcherUtils htmlFetcherUtils;

    // variables with annotation @Value get data from application.properties
    @Value("${fetcher.base_url}")
    private String baseUrl;

    @Value("${fetcher.depth_factor}")
    private int depthFactor;

    // this method is automatically called when app.context ready
    @PostConstruct
    public void startFetching() {
        fetch(baseUrl, 0);
    }

// async variant
    public void fetch(String url, int depth) {
        if (depth <= depthFactor) {
            CompletableFuture<Set<String>> childUrlsSet = null;
            try {
                childUrlsSet = htmlFetcherUtils.fetchHtmlDoc(url, depth);
            } catch (IOException e) {
                e.printStackTrace();
            }
            final int d = depth + 1;
            try {
                childUrlsSet.get().forEach(u -> fetch(u, d));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
    }

    // one thread variant
//    public void fetch(String url, int depth) {
//        if (depth <= depthFactor) {
//            Set<String> childUrlsSet = htmlFetcherUtils.fetchHtmlDoc(url, depth);
//            final int d = depth + 1;
//            childUrlsSet.stream().forEach(u -> fetch(u, d));
//        }
//    }
}