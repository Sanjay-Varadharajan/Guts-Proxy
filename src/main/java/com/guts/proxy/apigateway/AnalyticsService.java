package com.guts.proxy.apigateway;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.resource.NoResourceFoundException;

@RequiredArgsConstructor
@Service
public class AnalyticsService {




    private final ApiKeyRepository apiKeyRepository;


    public Page<ApiKeyAnalytics> viewAnalyticByKey(String apiKey, Pageable pageable, HttpServletRequest httpServletRequest) throws BadRequestException {


        Page<ApiKeyAnalytics> analytics=apiKeyRepository.findByApiKey(apiKey,pageable);

        if (analytics.isEmpty()){
            throw new BadRequestException("No Analytics found");
        }
        return analytics;
    }
}
