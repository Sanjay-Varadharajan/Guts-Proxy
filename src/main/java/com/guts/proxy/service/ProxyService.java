package com.guts.proxy.service;

import com.guts.proxy.apigateway.Decision;
import com.guts.proxy.apigateway.MasterKeyValidator;
import com.guts.proxy.apigateway.RedisApiKeyValidator;
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

    private final RedisApiKeyValidator apiKeyValidator;
    private final MasterKeyValidator masterKeyValidator;

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
            String clientIp,
            String apiKey
    ) {

        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {

            boolean isApiKeyManagementEndpoint =
                    path.startsWith("/api/v1/key/admin");

            String masterKey = headers.getFirst("X-MASTER-KEY");

            System.out.println("API KEY RECEIVED => " + apiKey);
            System.out.println("MASTER KEY RECEIVED => " + masterKey);

            if (isApiKeyManagementEndpoint) {

                if (!masterKeyValidator.isValid(masterKey)) {

                    proxyLogService.saveLog(
                            requestId,
                            clientIp,
                            method.name(),
                            path,
                            "BLOCKED",
                            403,
                            0L,
                            false,
                            "Invalid Master Key",
                            "MASTER_KEY",
                            Decision.BLOCKED
                    );

                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Invalid Master Key");
                }

            }
                else if (apiKey == null || !apiKeyValidator.isValid(apiKey)){
                proxyLogService.saveLog(
                        requestId,
                        clientIp,
                        method.name(),
                        path,
                        "BLOCKED",
                        403,
                        0L,
                        false,
                        "Invalid API Key",
                        apiKey,
                        Decision.BLOCKED
                );

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Invalid API Key");
            }

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

            ResponseEntity<String> iamResponse = webClient
                    .method(method)
                    .uri(url)
                    .headers(h -> h.addAll(safeHeaders))
                    .bodyValue(body == null ? "" : body)
                    .exchangeToMono(response -> response.toEntity(String.class))
                    .block();

            long latency = System.currentTimeMillis() - startTime;

            String keyUsed = isApiKeyManagementEndpoint
                    ? "MASTER_KEY"
                    : apiKey;


            proxyLogService.saveLog(
                    requestId,
                    clientIp,
                    method.name(),
                    path,
                    url,
                    iamResponse.getStatusCode().value(),
                    latency,
                    iamResponse.getStatusCode().is2xxSuccessful(),
                    null,
                    keyUsed,
                    Decision.ALLOWED
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
                    ex.getMessage(),
                    apiKey,
                    Decision.ERROR
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