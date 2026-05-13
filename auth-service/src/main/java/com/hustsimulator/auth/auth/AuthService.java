package com.hustsimulator.auth.auth;

import com.hustsimulator.auth.entity.User;

public interface AuthService {
    AuthDTO.AuthResponse login(AuthDTO.LoginRequest loginRequest);
    User register(AuthDTO.RegisterRequest registerRequest);
    AuthDTO.TokenRefreshResponse refreshToken(AuthDTO.RefreshTokenRequest request);
    void logout(String refreshToken);
}
