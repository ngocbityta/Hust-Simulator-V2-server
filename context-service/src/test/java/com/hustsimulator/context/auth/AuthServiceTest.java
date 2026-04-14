package com.hustsimulator.context.auth;

import com.hustsimulator.context.common.JwtUtils;
import com.hustsimulator.context.entity.User;
import com.hustsimulator.context.enums.UserRole;
import com.hustsimulator.context.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private AuthDTO.RegisterRequest registerRequest;
    private AuthDTO.LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new AuthDTO.RegisterRequest("0123456789", "password", "testuser", UserRole.HV);
        loginRequest = new AuthDTO.LoginRequest("0123456789", "password");
        
        testUser = User.builder()
                .phonenumber("0123456789")
                .username("testuser")
                .role(UserRole.HV)
                .password("encoded_password")
                .build();
        testUser.setId(UUID.randomUUID());
    }

    @Test
    void register_shouldCreateUser_whenPhonenumberIsNew() {
        when(userRepository.existsByPhonenumber(registerRequest.phonenumber())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPhonenumber()).isEqualTo(registerRequest.phonenumber());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenPhonenumberExists() {
        when(userRepository.existsByPhonenumber(registerRequest.phonenumber())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    void login_shouldReturnAuthResponse_whenCredentialsAreValid() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtils.generateToken(loginRequest.phonenumber())).thenReturn("mocked_jwt");
        when(userRepository.findByPhonenumber(loginRequest.phonenumber())).thenReturn(Optional.of(testUser));

        AuthDTO.AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mocked_jwt");
        assertThat(response.username()).isEqualTo(testUser.getUsername());
    }

    @Test
    void login_shouldThrowException_whenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}
