package com.hustsimulator.context.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final int expiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", expiration);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        String phonenumber = "0123456789";
        String token = jwtUtils.generateToken(phonenumber);

        assertThat(token).isNotNull();
        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.getPhonenumberFromToken(token)).isEqualTo(phonenumber);
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsMalformed() {
        String invalidToken = "invalid.token.here";
        assertThat(jwtUtils.validateToken(invalidToken)).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsExpired() {
        // Set short expiration
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -1000);
        String token = jwtUtils.generateToken("0123456789");

        assertThat(jwtUtils.validateToken(token)).isFalse();
    }

    @Test
    void getPhonenumberFromToken_shouldExtractCorrectSubject() {
        String phonenumber = "9876543210";
        String token = jwtUtils.generateToken(phonenumber);

        String result = jwtUtils.getPhonenumberFromToken(token);
        assertThat(result).isEqualTo(phonenumber);
    }
}
