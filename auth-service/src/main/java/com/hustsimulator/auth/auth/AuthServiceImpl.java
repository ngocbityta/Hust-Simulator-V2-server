package com.hustsimulator.auth.auth;

import com.hustsimulator.auth.entity.RefreshToken;
import com.hustsimulator.auth.entity.User;
import com.hustsimulator.auth.common.JwtUtils;
import com.hustsimulator.auth.user.UserEvent;
import com.hustsimulator.auth.user.UserEventPublisher;
import com.hustsimulator.auth.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserEventPublisher userEventPublisher;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.phonenumber(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByPhonenumber(loginRequest.phonenumber())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        // Generate JWT access token with userId embedded as claim
        String accessToken = jwtUtils.generateToken(loginRequest.phonenumber(), user.getId());

        // Generate opaque refresh token (stored in DB)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthDTO.AuthResponse(accessToken, refreshToken.getToken(), user.getId(), user.getPhonenumber(), user.getUsername());
    }

    @Transactional
    public User register(AuthDTO.RegisterRequest registerRequest) {
        if (userRepository.existsByPhonenumber(registerRequest.phonenumber())) {
            throw new IllegalArgumentException("Error: Phonenumber is already taken!");
        }

        User user = User.builder()
                .phonenumber(registerRequest.phonenumber())
                .password(passwordEncoder.encode(registerRequest.password()))
                .username(registerRequest.username())
                .build();

        User saved = userRepository.save(user);
        userEventPublisher.publish(saved, UserEvent.EventType.CREATED);
        return saved;
    }

    @Transactional
    public AuthDTO.TokenRefreshResponse refreshToken(AuthDTO.RefreshTokenRequest request) {
        RefreshToken existingToken = refreshTokenService.findByToken(request.refreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Verify not expired
        refreshTokenService.verifyExpiration(existingToken);

        // Rotate: delete old, create new refresh token
        RefreshToken newRefreshToken = refreshTokenService.rotateToken(existingToken);

        // Issue new access token
        User user = newRefreshToken.getUser();
        String newAccessToken = jwtUtils.generateToken(user.getPhonenumber(), user.getId());

        return new AuthDTO.TokenRefreshResponse(newAccessToken, newRefreshToken.getToken());
    }

    @Transactional
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Delete all refresh tokens for this user (logout from all devices)
        refreshTokenService.deleteByUserId(token.getUser().getId());
    }
}
