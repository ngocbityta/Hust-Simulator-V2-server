package com.hustsimulator.auth.user;

import com.hustsimulator.auth.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {
    List<User> findAll();
    User findById(UUID id);
    User create(User user);
    User update(UUID id, User updated);
    void delete(UUID id);
}
