package com.guts.proxy.service;

    import com.guts.proxy.extract.IpExtract;
    import jakarta.servlet.http.HttpServletRequest;
    import lombok.RequiredArgsConstructor;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.http.*;
    import org.springframework.stereotype.Service;
    import org.springframework.web.reactive.function.client.WebClient;
    import org.springframework.web.util.UriComponentsBuilder;

    import java.util.UUID;

@Service
    @RequiredArgsConstructor
    public class ProxyService {

        private final ProxyLogService proxyLogService;


        private final WebClient webClient;



    @Value("${proxy.target-url}")
    private String targetUrl;

    public ResponseEntity<String> forwardRequest(
            String path,
            HttpMethod method,
            HttpHeaders headers,
            String body,
            String clientIp
    ) {

        String url = UriComponentsBuilder
                .fromUriString(targetUrl)
                .path(path)
                .toUriString();

        HttpHeaders safeHeaders = filterHeaders(headers);


        String requestId = UUID.randomUUID().toString();

        long startTime = System.currentTimeMillis();



        try {

            ResponseEntity<String> response = webClient
                    .method(method)
                    .uri(url)
                    .headers(h -> h.addAll(safeHeaders))
                    .bodyValue(body == null ? "" : body)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            long latency =
                    System.currentTimeMillis() - startTime;

            proxyLogService.saveLog(
                    requestId,
                    clientIp,
                    method.name(),
                    path,
                    url,
                    response.getStatusCode().value(),
                    latency,
                    true,
                    null
            );


            return response;

        } catch (Exception ex) {

            long latency =
                    System.currentTimeMillis() - startTime;


            proxyLogService.saveLog(
                    requestId,
                    clientIp,
                    method.name(),
                    path,
                    url,
                    500,
                    latency,
                    false,
                    ex.getMessage()
            );

            throw ex;
        }
    }

    private HttpHeaders filterHeaders(HttpHeaders original) {

        HttpHeaders headers = new HttpHeaders();

        original.forEach((key, value) -> {
            if (!key.equalsIgnoreCase("host") &&
                    !key.equalsIgnoreCase("content-length") &&
                    !key.equalsIgnoreCase("connection") &&
                    !key.equalsIgnoreCase("transfer-encoding")) {
                headers.put(key, value);
            }
        });

        return headers;
    }
}