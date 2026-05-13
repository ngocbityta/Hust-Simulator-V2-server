package com.hustsimulator.auth.auth;

import com.hustsimulator.auth.entity.RefreshToken;
import com.hustsimulator.auth.entity.User;
import com.hustsimulator.auth.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${hustsimulator.refreshTokenExpirationMs:604800000}") // 7 days default
    private long refreshTokenExpirationMs;

    /**
     * Create a new refresh token for the given user.
     */
    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Find a refresh token by its opaque token string.
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Verify that a refresh token is not expired.
     * If expired, delete it and throw an exception.
     */
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token has expired. Please log in again.");
        }
        return token;
    }

    /**
     * Rotate: delete old token, issue a new one for the same user.
     */
    @Transactional
    public RefreshToken rotateToken(RefreshToken oldToken) {
        UUID userId = oldToken.getUser().getId();
        refreshTokenRepository.delete(oldToken);
        return createRefreshToken(userId);
    }

    /**
     * Delete all refresh tokens for a user (logout from all devices).
     */
    @Transactional
    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("All refresh tokens deleted for user {}", userId);
    }

    /**
     * Cleanup expired tokens every hour.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.debug("Expired refresh tokens cleaned up");
    }
}
