package com.guts.proxy.apigateway;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisApiKeyValidator {


    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "api_key:";

    public boolean isValid(String apiKey) {

        if (apiKey == null || apiKey.isBlank()) return false;

        String value = redisTemplate.opsForValue().get(PREFIX + apiKey);

        return "ACTIVE".equals(value);
    }
}