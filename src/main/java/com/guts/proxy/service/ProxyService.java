package com.guts.proxy.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProxyService {

    private final ProxyLogService proxyLogService;
    private final WebClient webClient;

    @Value("${proxy.target-url}")
    private String targetUrl;

    public ResponseEntity<String> forwardRequest(
            HttpServletRequest request,
            String path,
            HttpMethod method,
            HttpHeaders headers,
            String body,
            String clientIp
    ) {

        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {

            // Build target URL
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(targetUrl)
                    .path(path);

            request.getParameterMap().forEach((key, values) -> {
                for (String value : values) {
                    builder.queryParam(key, value);
                }
            });

            String url = builder.build(true).toUriString();

            HttpHeaders safeHeaders = filterHeaders(headers);

            // 🔥 TRUE PASS-THROUGH CALL (NO EXCEPTION ON 4xx/5xx)
            ResponseEntity<String> iamResponse = webClient
                    .method(method)
                    .uri(url)
                    .headers(h -> h.addAll(safeHeaders))
                    .bodyValue(body == null ? "" : body)
                    .exchangeToMono(response -> response.toEntity(String.class))
                    .block();

            long latency = System.currentTimeMillis() - startTime;

            // log everything
            proxyLogService.saveLog(
                    requestId,
                    clientIp,
                    method.name(),
                    path,
                    url,
                    iamResponse.getStatusCode().value(),
                    latency,
                    iamResponse.getStatusCode().is2xxSuccessful(),
                    null
            );
            return ResponseEntity
                    .status(iamResponse.getStatusCode())
                    .headers(iamResponse.getHeaders())
                    .body(iamResponse.getBody());

        } catch (Exception ex) {

            long latency = System.currentTimeMillis() - startTime;

            proxyLogService.saveLog(
                    requestId,
                    clientIp,
                    method.name(),
                    path,
                    targetUrl + path,
                    502,
                    latency,
                    false,
                    ex.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body("Proxy failed: " + ex.getMessage());
        }
    }

    private HttpHeaders filterHeaders(HttpHeaders original) {

        HttpHeaders headers = new HttpHeaders();

        original.forEach((key, value) -> {
            if (!key.equalsIgnoreCase("host")
                    && !key.equalsIgnoreCase("content-length")
                    && !key.equalsIgnoreCase("connection")
                    && !key.equalsIgnoreCase("transfer-encoding")) {
                headers.put(key, value);
            }
        });

        return headers;
    }
}