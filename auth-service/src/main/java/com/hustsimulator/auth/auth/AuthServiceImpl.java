package com.hustsimulator.auth.auth;

import com.hustsimulator.auth.entity.User;
import com.hustsimulator.auth.enums.UserRole;
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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserEventPublisher userEventPublisher;

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.phonenumber(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByPhonenumber(loginRequest.phonenumber())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        // Generate JWT with userId embedded as claim
        String jwt = jwtUtils.generateToken(loginRequest.phonenumber(), user.getId());

        return new AuthDTO.AuthResponse(jwt, user.getId(), user.getPhonenumber(), user.getUsername(), user.getRole());
    }

    public User register(AuthDTO.RegisterRequest registerRequest) {
        if (userRepository.existsByPhonenumber(registerRequest.phonenumber())) {
            throw new IllegalArgumentException("Error: Phonenumber is already taken!");
        }

        User user = User.builder()
                .phonenumber(registerRequest.phonenumber())
                .password(passwordEncoder.encode(registerRequest.password()))
                .username(registerRequest.username())
                .role(registerRequest.role() != null ? registerRequest.role() : UserRole.HV)
                .build();

        User saved = userRepository.save(user);
        userEventPublisher.publish(saved, UserEvent.EventType.CREATED);
        return saved;
    }
}
