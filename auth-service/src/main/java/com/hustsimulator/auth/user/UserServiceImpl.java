package com.hustsimulator.auth.user;

import com.hustsimulator.auth.entity.User;
import com.hustsimulator.auth.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher userEventPublisher;

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
        if (userRepository.existsByPhonenumber(user.getPhonenumber())) {
            throw new IllegalArgumentException("Lỗi: Số điện thoại này đã được sử dụng!");
        }
        if (user.getUsername() != null && userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Lỗi: Tên đăng nhập này đã được sử dụng!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        userEventPublisher.publish(saved, UserEvent.EventType.CREATED);
        return saved;
    }

    @Override
    public User update(UUID id, User updated) {
        User user = findById(id);
        if (updated.getUsername() != null && !updated.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updated.getUsername())) {
                throw new IllegalArgumentException("Lỗi: Tên đăng nhập này đã được sử dụng!");
            }
            user.setUsername(updated.getUsername());
        }
        if (updated.getPhonenumber() != null && !updated.getPhonenumber().equals(user.getPhonenumber())) {
            if (userRepository.existsByPhonenumber(updated.getPhonenumber())) {
                throw new IllegalArgumentException("Lỗi: Số điện thoại này đã được sử dụng!");
            }
            user.setPhonenumber(updated.getPhonenumber());
        }
        if (updated.getFullName() != null) user.setFullName(updated.getFullName());
        if (updated.getRole() != null) user.setRole(updated.getRole());
        if (updated.getAvatar() != null) user.setAvatar(updated.getAvatar());
        if (updated.getCoverImage() != null) user.setCoverImage(updated.getCoverImage());
        if (updated.getDescription() != null) user.setDescription(updated.getDescription());
        User saved = userRepository.save(user);
        userEventPublisher.publish(saved, UserEvent.EventType.UPDATED);
        return saved;
    }

    @Override
    public void delete(UUID id) {
        User user = findById(id);
        userRepository.deleteById(id);
        userEventPublisher.publish(user, UserEvent.EventType.DELETED);
    }

    @Override
    public com.hustsimulator.auth.common.PageResponse<User> getUsersPaged(String search, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<User> userPage;
        if (search != null && !search.trim().isEmpty()) {
            userPage = userRepository.searchUsers(search.trim(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        return new com.hustsimulator.auth.common.PageResponse<>(userPage);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String phonenumber)
            throws org.springframework.security.core.userdetails.UsernameNotFoundException {
        User user = userRepository.findByPhonenumber(phonenumber)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "User Not Found with phonenumber: " + phonenumber));

        return new org.springframework.security.core.userdetails.User(
                user.getPhonenumber(),
                user.getPassword(),
                java.util.Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_USER")));
    }

    public UserRepository getRepository() {
        return this.userRepository;
    }
}
