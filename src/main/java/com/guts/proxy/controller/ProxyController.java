package com.guts.proxy.controller;

import com.guts.proxy.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ProxyController {

    private final ProxyService proxyService;

    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @RequestMapping("/**")
    public String proxy(
            HttpServletRequest request,
            @RequestBody(required = false) String body
    ) throws IOException {

        String path = request.getRequestURI();

        HttpMethod method =
                HttpMethod.valueOf(request.getMethod());

        HttpHeaders headers = new HttpHeaders();

        request.getHeaderNames().asIterator()
                .forEachRemaining(header ->
                        headers.add(header,
                                request.getHeader(header)));

        return proxyService.forwardRequest(
                path,
                method,
                headers,
                body
        );
    }
}