package com.hustsimulator.auth.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class AuthDTO {

        public record LoginRequest(
                        @NotBlank(message = "Phone number is required") String phonenumber,

                        @NotBlank(message = "Password is required") String password) {
        }

        public record RegisterRequest(
                        @NotBlank(message = "Phone number is required") @Size(min = 9, max = 15, message = "Phone number must be between 9 and 15 characters") String phonenumber,

                        @NotBlank(message = "Password is required") @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters") String password,

                        @NotBlank(message = "Username is required") @Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters") String username) {
        }

        public record AuthResponse(String accessToken, String refreshToken, UUID id, String phonenumber,
                        String username) {
        }

        public record RefreshTokenRequest(
                        @NotBlank(message = "Refresh token is required") String refreshToken) {
        }

        public record TokenRefreshResponse(String accessToken, String refreshToken) {
        }
}
