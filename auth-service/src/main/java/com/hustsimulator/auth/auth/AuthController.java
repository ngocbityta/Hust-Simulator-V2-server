package com.hustsimulator.auth.auth;

import com.hustsimulator.auth.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Operations for user registration and login")
public class AuthController {

    private final AuthService authService;
    private final com.hustsimulator.auth.user.UserRepository userRepository;

    @PostMapping("/login")
    @Operation(summary = "Login to get JWT token")
    public AuthDTO.AuthResponse login(@Valid @RequestBody AuthDTO.LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user account")
    public User register(@Valid @RequestBody AuthDTO.RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token", description = "Used by API Gateway auth_request to validate requests")
    public org.springframework.http.ResponseEntity<?> validateToken() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String phonenumber = auth.getName();
            java.util.Optional<User> optionalUser = userRepository.findByPhonenumber(phonenumber);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                return org.springframework.http.ResponseEntity.ok()
                        .header("X-User-Id", user.getId().toString())
                        .header("X-User-Phonenumber", user.getPhonenumber())
                        .header("X-User-Role", user.getRole().name())
                        .build();
            }
        }
        return org.springframework.http.ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
