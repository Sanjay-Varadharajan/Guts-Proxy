package com.guts.proxy.apigateway;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AnalyticOrchestrationService {

   private final ApiKeyRepository apiKeyRepository;

    public void updateAnalytics(String apiKey, int statusCode,String usedOn) {
        ApiKeyAnalytics analytics = apiKeyRepository.findByApiKey(apiKey)
                .orElseGet(() -> {
                    ApiKeyAnalytics a = new ApiKeyAnalytics();
                    a.setApiKey(apiKey);
                    a.setTotalRequests(0);
                    a.setSuccessfulRequests(0);
                    a.setFailedRequests(0);
                    a.setUsedOn(usedOn);
                    return a;
                });

        analytics.setTotalRequests(analytics.getTotalRequests() + 1);

        if (statusCode >= 200 && statusCode < 400) {
            analytics.setSuccessfulRequests(
                    analytics.getSuccessfulRequests() + 1);
        } else {
            analytics.setFailedRequests(
                    analytics.getFailedRequests() + 1);
        }

        analytics.setLastUsedAt(LocalDateTime.now());

        apiKeyRepository.save(analytics);
    }
}

