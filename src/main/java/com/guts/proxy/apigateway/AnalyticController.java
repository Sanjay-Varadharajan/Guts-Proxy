package com.guts.proxy.apigateway;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internals")
public class AnalyticController {


   private final AnalyticsService analyticsService;

    @GetMapping("/analytics")
    public ResponseEntity<ApiKeyAnalyticsDto> viewAnalyticByKey(@RequestParam String apiKey
                                                              )throws BadRequestException {



        ApiKeyAnalyticsDto apiKeyAnalytics=analyticsService.viewAnalyticByKey(apiKey);

        return ResponseEntity.ok(apiKeyAnalytics);
    }
}
