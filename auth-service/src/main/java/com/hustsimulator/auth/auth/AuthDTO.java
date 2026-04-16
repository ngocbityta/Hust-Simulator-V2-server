package com.hustsimulator.auth.auth;

import com.hustsimulator.auth.enums.UserRole;
import java.util.UUID;

/**
 * Data Transfer Objects for Authentication operations.
 */
public class AuthDTO {

    public record LoginRequest(String phonenumber, String password) {}

    public record RegisterRequest(String phonenumber, String password, String username, UserRole role) {}

    public record AuthResponse(String token, UUID id, String phonenumber, String username, UserRole role) {}
}
