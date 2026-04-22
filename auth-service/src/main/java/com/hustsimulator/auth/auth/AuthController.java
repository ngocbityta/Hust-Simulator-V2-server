package com.hustsimulator.auth.auth;

import com.hustsimulator.auth.entity.User;
import com.hustsimulator.auth.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Operations for user registration and login")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

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
    public ResponseEntity<?> validateToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String phonenumber = auth.getName();
        Optional<User> optionalUser = userRepository.findByPhonenumber(phonenumber);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = optionalUser.get();
        return ResponseEntity.ok()
                .header("X-User-Id", user.getId().toString())
                .header("X-User-Phonenumber", user.getPhonenumber())
                .build();
    }
}

