package com.hustsimulator.context.common;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JwtUtils — validates token parsing and validation only.
 * Token generation is now in auth-service.
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secret);
    }

    private String createTestToken(String phonenumber, UUID userId, long expirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(phonenumber)
                .claim("userId", userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    @Test
    void validateToken_shouldReturnTrue_whenTokenIsValid() {
        String token = createTestToken("0123456789", UUID.randomUUID(), 3600000);
        assertThat(jwtUtils.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsMalformed() {
        assertThat(jwtUtils.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsExpired() {
        String token = createTestToken("0123456789", UUID.randomUUID(), -1000);
        assertThat(jwtUtils.validateToken(token)).isFalse();
    }

    @Test
    void getPhonenumberFromToken_shouldExtractCorrectSubject() {
        String phonenumber = "9876543210";
        String token = createTestToken(phonenumber, UUID.randomUUID(), 3600000);
        assertThat(jwtUtils.getPhonenumberFromToken(token)).isEqualTo(phonenumber);
    }

    @Test
    void getUserIdFromToken_shouldExtractCorrectUserId() {
        UUID userId = UUID.randomUUID();
        String token = createTestToken("0123456789", userId, 3600000);
        assertThat(jwtUtils.getUserIdFromToken(token)).isEqualTo(userId.toString());
    }
}
