package com.guts.proxy.apigateway;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKeyAnalytics,Integer> {

    Optional<ApiKeyAnalytics> findByApiKey(String apiKey);


}
