package com.hustsimulator.context.user;

import com.hustsimulator.context.entity.User;
import java.util.List;
import java.util.UUID;

public interface UserService {
    List<User> findAll();
    User findById(UUID id);
    User create(User user);
    User update(UUID id, User updated);
    void delete(UUID id);
}
