package com.guts.proxy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ProxyService {

    private final WebClient webClient;

    @Value("${proxy.target-url}")
    private String targetUrl;

    public ProxyService(WebClient webClient) {
        this.webClient = webClient;
    }

    public ResponseEntity<String> forwardRequest(
            String path,
            HttpMethod method,
            HttpHeaders headers,
            String body
    ) {

        String url = UriComponentsBuilder
                .fromUriString(targetUrl)
                .path(path)
                .toUriString();

        HttpHeaders safeHeaders = filterHeaders(headers);

        return webClient
                .method(method)
                .uri(url)
                .headers(h -> h.addAll(safeHeaders))
                .bodyValue(body == null ? "" : body)
                .retrieve()
                .toEntity(String.class)
                .block();
    }

    private HttpHeaders filterHeaders(HttpHeaders original) {

        HttpHeaders headers = new HttpHeaders();

        original.forEach((key, value) -> {
            if (!key.equalsIgnoreCase("host") &&
                    !key.equalsIgnoreCase("content-length") &&
                    !key.equalsIgnoreCase("connection") &&
                    !key.equalsIgnoreCase("transfer-encoding")) {
                headers.put(key, value);
            }
        });

        return headers;
    }
}