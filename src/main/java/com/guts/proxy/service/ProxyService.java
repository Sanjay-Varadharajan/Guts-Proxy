package com.guts.proxy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ProxyService {

    private final WebClient webClient;

    @Value("${proxy.target-url}")
    private String targetUrl;

    public ProxyService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String forwardRequest(
            String path,
            HttpMethod method,
            HttpHeaders headers,
            String body
    ) {

        String url = targetUrl + "/" + path;

        return webClient
                .method(method)
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .bodyValue(body == null ? "" : body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}