package com.hustsimulator.streaming.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LiveKitServiceTest {

    private LiveKitService liveKitService;

    @BeforeEach
    void setUp() throws Exception {
        liveKitService = new LiveKitService();
        // Inject values via reflection since @Value won't work in unit tests
        var apiKeyField = LiveKitService.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(liveKitService, "devkey");

        var apiSecretField = LiveKitService.class.getDeclaredField("apiSecret");
        apiSecretField.setAccessible(true);
        apiSecretField.set(liveKitService, "secret");
    }

    @Test
    void createToken_publisher_shouldReturnNonEmptyJwt() {
        String token = liveKitService.createToken("test_room", "user-123", "John", true);

        assertThat(token).isNotBlank();
        // JWT format: header.payload.signature
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void createToken_subscriber_shouldReturnNonEmptyJwt() {
        String token = liveKitService.createToken("test_room", "user-456", "Jane", false);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void createToken_differentRooms_shouldReturnDifferentTokens() {
        String token1 = liveKitService.createToken("room_a", "user-1", "Alice", true);
        String token2 = liveKitService.createToken("room_b", "user-1", "Alice", true);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void createToken_differentIdentities_shouldReturnDifferentTokens() {
        String token1 = liveKitService.createToken("room_a", "user-1", "Alice", true);
        String token2 = liveKitService.createToken("room_a", "user-2", "Bob", true);

        assertThat(token1).isNotEqualTo(token2);
    }
}
