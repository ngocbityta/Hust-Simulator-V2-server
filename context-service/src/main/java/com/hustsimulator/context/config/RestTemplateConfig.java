package com.hustsimulator.context.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Bean configuration cho RestTemplate — dùng để gọi HTTP tới các microservice khác
 * (ví dụ: messaging-service endpoint /api/messages/participated-events)
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
