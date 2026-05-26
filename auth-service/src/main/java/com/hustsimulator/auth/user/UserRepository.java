package com.hustsimulator.auth.user;

import com.hustsimulator.auth.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhonenumber(String phonenumber);

    boolean existsByPhonenumber(String phonenumber);

    Page<User> findByUsernameContainingIgnoreCaseOrPhonenumberContainingIgnoreCase(String username, String phonenumber, Pageable pageable);

    long countByOnline(Boolean online);

    long countByRole(com.hustsimulator.auth.enums.Role role);

    long countByStatus(com.hustsimulator.auth.enums.UserStatus status);
}
