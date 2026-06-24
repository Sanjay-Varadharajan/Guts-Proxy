package com.guts.proxy.service;

import com.guts.proxy.apigateway.Decision;
import com.guts.proxy.model.ProxyLog;
import com.guts.proxy.repository.ProxyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProxyLogService {

    private final ProxyLogRepository proxyLogRepository;

    public void saveLog(
            String requestId,
            String clientIp,
            String method,
            String sourcePath,
            String targetUrl,
            Integer statusCode,
            Long latencyMs,
            Boolean success,
            String errorMessage,
            String apiKey,
            Decision gateWayDecision) {

        ProxyLog log = ProxyLog.builder()
                .requestId(requestId)
                .clientIp(clientIp)
                .method(method)
                .sourcePath(sourcePath)
                .targetUrl(targetUrl)
                .statusCode(statusCode)
                .latencyMs(latencyMs)
                .success(success)
                .errorMessage(errorMessage)
                .loggedTime(LocalDateTime.now())
                .apiKey(apiKey)
                .gatewayDecision(gateWayDecision)
                .build();

        proxyLogRepository.save(log);
    }

}
