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
            throw new IllegalArgumentException("Error: Phonenumber is already taken!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User update(UUID id, User updated) {
        User user = findById(id);
        user.setUsername(updated.getUsername());
        user.setAvatar(updated.getAvatar());
        user.setCoverImage(updated.getCoverImage());
        user.setDescription(updated.getDescription());
        return userRepository.save(user);
    }

    @Override
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
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
                                "ROLE_" + user.getRole().name())));
    }
}
