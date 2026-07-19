package com.guts.proxy.apigateway;

import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiKeyAnalyticsDto {

    private Long apiAnalyticsId;

    private String apiKey;

    private String UsedOn;

    private long totalRequests;

    private long successfulRequests;

    private long failedRequests;

    private LocalDateTime lastUsedAt;
}
