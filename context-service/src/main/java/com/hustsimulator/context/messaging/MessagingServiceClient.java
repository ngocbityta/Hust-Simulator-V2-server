package com.hustsimulator.context.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * HTTP client để gọi messaging-service API.
 * Thay thế việc query trực tiếp cross-service vào bảng messages_chat.
 */
@Component
@Slf4j
public class MessagingServiceClient {

    private final RestTemplate restTemplate;
    private final String messagingServiceUrl;

    public MessagingServiceClient(
            RestTemplate restTemplate,
            @Value("${hustsimulator.messaging-service.url:http://messaging-service:8082}") String messagingServiceUrl) {
        this.restTemplate = restTemplate;
        this.messagingServiceUrl = messagingServiceUrl;
    }

    /**
     * Lấy danh sách event ID mà user đã từng tham gia (qua tin nhắn).
     * Gọi: GET {messaging-service}/api/messages/participated-events?userId={userId}
     */
    @CircuitBreaker(name = "messagingService", fallbackMethod = "fallbackParticipatedEvents")
    public List<UUID> getParticipatedEventIds(UUID userId) {
        String url = UriComponentsBuilder
                .fromHttpUrl(messagingServiceUrl + "/api/messages/participated-events")
                .queryParam("userId", userId.toString())
                .toUriString();

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

        try {
            List<UUID> result = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<UUID>>() {}
            ).getBody();
            return result != null ? result : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to call messaging-service for participated events of user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fallback method cho Circuit Breaker khi messaging-service gặp sự cố hoặc timeout
     */
    public List<UUID> fallbackParticipatedEvents(UUID userId, Throwable t) {
        log.warn("CircuitBreaker fallback triggered for getParticipatedEventIds. User: {}, Reason: {}", userId, t.getMessage());
        return Collections.emptyList();
    }
}
