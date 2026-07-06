package com.guts.proxy.apigateway;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiKeyAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long apiAnalyticsId;

    @Column(unique = true, nullable = false)
    private String apiKey;

    private String UsedOn;

    private long totalRequests;

    private long successfulRequests;

    private long failedRequests;

    private LocalDateTime lastUsedAt;


}
