package com.guts.proxy.service;

import com.guts.proxy.apigateway.AnalyticOrchestrationService;
import com.guts.proxy.apigateway.Decision;
import com.guts.proxy.apigateway.MasterKeyValidator;
import com.guts.proxy.apigateway.RedisApiKeyValidator;
import com.guts.proxy.ratelimit.RateLimiterService;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class ProxyService {

    private final RedisApiKeyValidator apiKeyValidator;
    private final MasterKeyValidator masterKeyValidator;

    private final ProxyLogService proxyLogService;
    private final WebClient webClient;
    private final RateLimiterService rateLimiterService;

    private final IamClientService iamClientService;

    private final AnalyticOrchestrationService analyticService;

    private final ExecutorService executor =
            Executors.newVirtualThreadPerTaskExecutor();

    @Value("${proxy.target-url}")
    private String targetUrl;


    public ResponseEntity<?> forwardRequest(
            HttpServletRequest request,
            String path,
            HttpMethod method,
            HttpHeaders headers,
            String body,
            String clientIp,
            String apiKey
    ) {

        long start = System.currentTimeMillis();

        Future<ResponseEntity<?>> future = executor.submit(() ->
                forwardRequestInternal(
                        request,
                        path,
                        method,
                        headers,
                        body,
                        clientIp,
                        apiKey
                )
        );

        try {

            return future.get(5, TimeUnit.SECONDS);

        }
        catch (TimeoutException e) {

            future.cancel(true);

            proxyLogService.saveLog(
                    UUID.randomUUID().toString(),
                    clientIp,
                    method.name(),
                    path,
                    targetUrl + path,
                    504,
                    System.currentTimeMillis()-start,
                    false,
                    "Request Timed Out",
                    apiKey,
                    Decision.ERROR
            );

            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body("Request timed out");
        }
        catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body("Proxy failed: " + e.getMessage());
        }
    }

    public ResponseEntity<?> forwardRequestInternal(
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

        boolean isApiKeyManagementEndpoint =
                path.startsWith("/api/v1/key/admin");

        String masterKey = headers.getFirst("X-MASTER-KEY");

        String keyUsed = isApiKeyManagementEndpoint
                ? "MASTER_KEY"
                : apiKey;

        try {

            if (isApiKeyManagementEndpoint) {

                if (!masterKeyValidator.isValid(masterKey)) {

                    long latency = System.currentTimeMillis() - startTime;

                    proxyLogService.saveLog(
                            requestId,
                            clientIp,
                            method.name(),
                            path,
                            "BLOCKED",
                            403,
                            latency,
                            false,
                            "Invalid Master Key",
                            keyUsed,
                            Decision.BLOCKED
                    );

                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Invalid Master Key");
                }

            }


            else {

                if (apiKey == null || !apiKeyValidator.isValid(apiKey)) {

                    long latency = System.currentTimeMillis() - startTime;

                    proxyLogService.saveLog(
                            requestId,
                            clientIp,
                            method.name(),
                            path,
                            "BLOCKED",
                            403,
                            latency,
                            false,
                            "Invalid API Key",
                            apiKey,
                            Decision.BLOCKED
                    );

                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Invalid API Key");
                }




                if (!rateLimiterService.isAllowed(apiKey)) {

                    long latency = System.currentTimeMillis() - startTime;

                    proxyLogService.saveLog(
                            requestId,
                            clientIp,
                            method.name(),
                            path,
                            "BLOCKED",
                            429,
                            latency,
                            false,
                            "Rate Limit Exceeded",
                            apiKey,
                            Decision.BLOCKED
                    );

                    analyticService.updateAnalytics(apiKey, 429,targetUrl);


                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body("Rate Limit Exceeded");
                }
            }


            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(targetUrl)
                    .path(path);

            request.getParameterMap().forEach((k, values) -> {
                for (String value : values) {
                    builder.queryParam(k, value);
                }
            });

            String url = builder.build(true).toUriString();

            HttpHeaders safeHeaders = filterHeaders(headers);
            ResponseEntity<byte[]> iamResponse =
                    iamClientService.callIam(
                            method,
                            url,
                            safeHeaders,
                            body
                    );



            if (!isApiKeyManagementEndpoint) {
                analyticService.updateAnalytics(
                        apiKey,
                        iamResponse.getStatusCode().value(),
                        path
                );
            }



            long latency = System.currentTimeMillis() - startTime;

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
                    keyUsed,
                    Decision.ERROR
            );


            if (!isApiKeyManagementEndpoint) {
                analyticService.updateAnalytics(apiKey, 502,path);
            }

            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
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

    @PreDestroy
    public void shutdownExecutor() {  //destroy the executor when tomcat stops
        executor.shutdown();
    }
}