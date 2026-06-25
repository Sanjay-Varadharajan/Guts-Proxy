package com.guts.proxy.ratelimit;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String,String> redisTemplate;

    private static final String PREFIX= "rate_limit:";
    private static final long LIMIT = 5;


    public boolean isAllowed(String apiKey){

        String key=PREFIX+apiKey;
        Long count=redisTemplate.opsForValue().increment(key);
        System.out.println("API Key rate  = " + apiKey);
        System.out.println("Count rate = " + count);

        if (count!=null || count==1){
            redisTemplate.expire(key, Duration.ofSeconds(5)); //timer of 1 seconds
        }

        return count != null && count <= LIMIT;
    }
}
