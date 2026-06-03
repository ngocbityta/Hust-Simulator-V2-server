package com.hustsimulator.auth.user;

import com.hustsimulator.auth.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable UUID id, @RequestBody UpdateUserRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setAvatar(request.avatar());
        user.setCoverImage(request.coverImage());
        user.setDescription(request.description());
        return userService.update(id, user);
    }

    public record UpdateUserRequest(
            String id,
            String phonenumber,
            String username,
            String avatar,
            String coverImage,
            String description
    ) {}

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }

    @GetMapping("/paged")
    public com.hustsimulator.auth.common.PageResponse<User> getUsersPaged(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.getUsersPaged(search, page, size);
    }

    @GetMapping("/stats")
    public java.util.Map<String, Object> getUserStats() {
        var repo = ((UserServiceImpl) userService).getRepository();
        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalUsers", repo.count());
        stats.put("onlineUsers", repo.countByOnline(true));
        stats.put("adminUsers", repo.countByRole(com.hustsimulator.auth.enums.Role.ADMIN));
        stats.put("regularUsers", repo.countByRole(com.hustsimulator.auth.enums.Role.USER));
        stats.put("activeUsers", repo.countByStatus(com.hustsimulator.auth.enums.UserStatus.ACTIVE));
        stats.put("lockedUsers", repo.countByStatus(com.hustsimulator.auth.enums.UserStatus.LOCKED));
        return stats;
    }
}
