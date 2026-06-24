package com.guts.proxy.model;

import com.guts.proxy.apigateway.Decision;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "proxy_logs")
@Data
@Builder
public class ProxyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestId;

    private String clientIp;

    private String method;

    private String sourcePath;

    @Column(length = 1000)
    private String targetUrl;

    private Integer statusCode;

    private Long latencyMs;

    private Boolean success;

    @Column(length = 2000)
    private String errorMessage;

    private LocalDateTime loggedTime;

    private String apiKey;

    private Decision gatewayDecision;
}
