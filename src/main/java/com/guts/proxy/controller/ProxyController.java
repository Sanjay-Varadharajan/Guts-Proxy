package com.guts.proxy.controller;

import com.guts.proxy.extract.IpExtract;
import com.guts.proxy.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ProxyController {

    private final ProxyService proxyService;

    private final IpExtract ipExtract;




    @RequestMapping("/**")
    public ResponseEntity<String> proxy(
            HttpServletRequest request,
            @RequestBody(required = false) String body
    ) throws IOException {

        String path = request.getRequestURI();

        String query = request.getQueryString();
        if (query != null) {
            path += "?" + query;
        }

        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        HttpHeaders headers = new HttpHeaders();

        request.getHeaderNames().asIterator()
                .forEachRemaining(header ->
                        headers.add(header, request.getHeader(header)));


        String ip=ipExtract.extractClientIp(request);




        return proxyService.forwardRequest(path, method, headers, body,ip);
    }


}
