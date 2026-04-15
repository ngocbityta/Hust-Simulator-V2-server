package com.hustsimulator.context.user;

import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.User;
import com.hustsimulator.context.enums.UserRole;
import com.hustsimulator.context.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .phonenumber("0123456789")
                .password("password")
                .username("testuser")
                .role(UserRole.HV)
                .status(UserStatus.ACTIVE)
                .build();
        testUser.setId(userId);
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<User> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User result = userService.findById(userId);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void findById_shouldThrowException_whenNotExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldSaveAndReturnUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.create(testUser);

        assertThat(result.getPhonenumber()).isEqualTo("0123456789");
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(testUser);
    }

    @Test
    void update_shouldModifyAndSaveUser() {
        User updated = User.builder()
                .username("updated_username")
                .description("Updated description")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.update(userId, updated);

        verify(userRepository).save(testUser);
        assertThat(testUser.getUsername()).isEqualTo("updated_username");
        assertThat(testUser.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void delete_shouldDeleteUser_whenExists() {
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.delete(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void delete_shouldThrowException_whenNotExists() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
