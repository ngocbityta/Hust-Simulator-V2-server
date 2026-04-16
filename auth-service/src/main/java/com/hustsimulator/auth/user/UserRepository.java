package com.hustsimulator.auth.user;

import com.hustsimulator.auth.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhonenumber(String phonenumber);

    boolean existsByPhonenumber(String phonenumber);
}
