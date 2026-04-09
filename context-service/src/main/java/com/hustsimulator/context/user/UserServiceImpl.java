package com.hustsimulator.context.user;

import com.hustsimulator.context.entity.User;
import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    public User create(User user) {
        return userRepository.save(user);
    }

    @Override
    public User update(UUID id, User updated) {
        User user = findById(id);
        user.setUsername(updated.getUsername());
        user.setAvatar(updated.getAvatar());
        user.setCoverImage(updated.getCoverImage());
        user.setDescription(updated.getDescription());
        user.setRole(updated.getRole());
        user.setStatus(updated.getStatus());
        user.setOnline(updated.getOnline());
        return userRepository.save(user);
    }

    @Override
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }
}
