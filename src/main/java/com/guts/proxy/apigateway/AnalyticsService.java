package com.guts.proxy.apigateway;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
public class AnalyticsService {


    private final ApiKeyRepository apiKeyRepository;


    public ApiKeyAnalyticsDto viewAnalyticByKey(String apiKey) throws BadRequestException {
        ApiKeyAnalytics analytics=apiKeyRepository.findByApiKey(apiKey).orElseThrow(
                ()->new BadRequestException()
        );
        ApiKeyAnalyticsDto apiKeyAnalyticsDto=new ApiKeyAnalyticsDto();

        apiKeyAnalyticsDto.setApiAnalyticsId(analytics.getApiAnalyticsId());
        apiKeyAnalyticsDto.setApiKey(maskApiKey(analytics.getApiKey()));
        apiKeyAnalyticsDto.setFailedRequests(analytics.getFailedRequests());
        apiKeyAnalyticsDto.setUsedOn(analytics.getUsedOn());
        apiKeyAnalyticsDto.setTotalRequests(analytics.getTotalRequests());
        apiKeyAnalyticsDto.setLastUsedAt(analytics.getLastUsedAt());
        apiKeyAnalyticsDto.setSuccessfulRequests(analytics.getSuccessfulRequests());

        return apiKeyAnalyticsDto;

    }

    public static String maskApiKey(String apiKey) {

        if (apiKey == null || apiKey.length() <= 8) {
            return "********";
        }

        String start = apiKey.substring(0, 4);
        String end = apiKey.substring(apiKey.length() - 4);

        return start + "*".repeat(apiKey.length() - 8) + end;
    }
}
