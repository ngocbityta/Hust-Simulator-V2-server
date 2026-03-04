package com.hustsimulator.context.user;

import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.User;
import com.hustsimulator.context.entity.UserRole;
import com.hustsimulator.context.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private UserService userService;

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
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.create(testUser);

        assertThat(result.getPhonenumber()).isEqualTo("0123456789");
        verify(userRepository).save(testUser);
    }

    @Test
    void update_shouldModifyAndSaveUser() {
        User updated = User.builder()
                .username("updated")
                .role(UserRole.GV)
                .status(UserStatus.LOCKED)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.update(userId, updated);

        verify(userRepository).save(testUser);
        assertThat(testUser.getUsername()).isEqualTo("updated");
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
