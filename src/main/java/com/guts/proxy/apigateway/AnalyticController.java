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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analytics")
public class AnalyticController {


   private final AnalyticsService analyticsService;

    @GetMapping("/view")
    public ResponseEntity<Page<ApiKeyAnalytics>> viewAnalyticByKey(@RequestParam String apiKey,
                                                              @PageableDefault(
                                                                      page = 0,
                                                                      size = 10,
                                                                      sort ="lastUsedAt",
                                                                      direction = Sort.Direction.DESC)
                                                                      Pageable pageable
                                                                   ,
                                                                   HttpServletRequest httpServletRequest
                                                              )throws BadRequestException {



        Page<ApiKeyAnalytics> apiKeyAnalytics=analyticsService.viewAnalyticByKey(apiKey,pageable,httpServletRequest);

        return ResponseEntity.ok(apiKeyAnalytics);






    }

}
