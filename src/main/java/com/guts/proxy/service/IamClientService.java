package com.guts.proxy.service;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class IamClientService {

    private final WebClient webClient;


    @CircuitBreaker(
            name = "iamService",
            fallbackMethod = "iamFallback"
    )
    public ResponseEntity<byte[]> callIam(
            HttpMethod method,
            String url,
            HttpHeaders headers,
            String body
    ){

        return webClient
                .method(method)
                .uri(url)
                .headers(h -> h.addAll(headers))
                .bodyValue(body == null ? "" : body)
                .exchangeToMono(response -> response.toEntity(byte[].class))
                .block();
    }


    public ResponseEntity<byte[]> iamFallback(
            HttpMethod method,
            String url,
            HttpHeaders headers,
            String body,
            Throwable ex
    ){

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("IAM service temporarily unavailable".getBytes());
    }
}
