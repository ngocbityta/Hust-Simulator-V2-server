package com.hustsimulator.social.usercache;

import com.hustsimulator.social.entity.UserCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import java.util.Optional;

@Repository
public interface UserCacheRepository extends JpaRepository<UserCache, UUID> {
    @Override
    @Cacheable(value = "users", key = "#id")
    Optional<UserCache> findById(UUID id);
}
