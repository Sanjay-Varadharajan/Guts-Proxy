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
            HttpServletRequest request,
            String path,
            HttpMethod method,
            HttpHeaders headers,
            String body,
            String clientIp
    ) {

        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(targetUrl)
                    .path(path);

            //query param safely
            request.getParameterMap().forEach((key, values) -> {
                for (String value : values) {
                    builder.queryParam(key, value);
                }
            });


            String url = builder.build(true).toUriString();

            HttpHeaders safeHeaders = filterHeaders(headers);

            ResponseEntity<String> response = webClient
                    .method(method)
                    .uri(url)
                    .headers(h -> h.addAll(safeHeaders))
                    .bodyValue(body == null ? "" : body)
                    .exchangeToMono(clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .map(responseBody ->
                                            ResponseEntity
                                                    .status(clientResponse.statusCode())
                                                    .body(responseBody)
                                    )
                    )
                    .block();

            long latency = System.currentTimeMillis() - startTime;

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

            long latency = System.currentTimeMillis() - startTime;

            proxyLogService.saveLog(
                    requestId,
                    clientIp,
                    method.name(),
                    path,
                    targetUrl + path,
                    500,
                    latency,
                    false,
                    ex.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body("Proxy error while calling IAM service");
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