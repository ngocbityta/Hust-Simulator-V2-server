package com.hustsimulator.social.context;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextServiceClient {

    private final RestTemplate restTemplate;

    @Value("${context-service.url:http://localhost:8081}")
    private String contextServiceUrl;

    public BuildingDTO getNearestBuilding(double lat, double lng, double radius) {
        try {
            String url = String.format("%s/api/buildings/nearest?lat=%f&lng=%f&radius=%f", 
                contextServiceUrl, lat, lng, radius);
            ResponseEntity<BuildingDTO> response = restTemplate.getForEntity(url, BuildingDTO.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null; // No building found
        } catch (Exception e) {
            log.error("Error calling context-service: {}", e.getMessage());
            return null;
        }
    }

    @Data
    public static class BuildingDTO {
        private UUID id;
        private String name;
        private String category;
    }
}
