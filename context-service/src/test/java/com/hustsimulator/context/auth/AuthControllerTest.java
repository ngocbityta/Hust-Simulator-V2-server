package com.hustsimulator.context.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hustsimulator.context.common.JwtUtils;
import com.hustsimulator.context.entity.User;
import com.hustsimulator.context.enums.UserRole;
import com.hustsimulator.context.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    // Provide all beans required by SecurityConfig that might be picked up
    @MockBean
    private UserService userService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_shouldReturnCreatedUser() throws Exception {
        AuthDTO.RegisterRequest request = new AuthDTO.RegisterRequest("0123456789", "password", "testuser", UserRole.HV);
        User registeredUser = User.builder()
                .phonenumber("0123456789")
                .username("testuser")
                .role(UserRole.HV)
                .build();
        registeredUser.setId(UUID.randomUUID());

        when(authService.register(any(AuthDTO.RegisterRequest.class))).thenReturn(registeredUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Correct status is 201 Created for register
                .andExpect(jsonPath("$.phonenumber").value("0123456789"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void login_shouldReturnAuthResponse() throws Exception {
        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest("0123456789", "password");
        AuthDTO.AuthResponse response = new AuthDTO.AuthResponse(
                "mocked_jwt", UUID.randomUUID(), "0123456789", "testuser", UserRole.HV);

        when(authService.login(any(AuthDTO.LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked_jwt"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}
