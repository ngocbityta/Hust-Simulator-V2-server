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

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE :search IS NULL OR :search = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.phonenumber) LIKE LOWER(CONCAT('%', :search, '%')) OR CAST(u.id AS string) LIKE CONCAT('%', :search, '%')")
    Page<User> searchUsers(@org.springframework.data.repository.query.Param("search") String search, Pageable pageable);

    long countByOnline(Boolean online);

    long countByRole(com.hustsimulator.auth.enums.Role role);

    long countByStatus(com.hustsimulator.auth.enums.UserStatus status);
}
