package com.hustsimulator.social.usercache;

import com.hustsimulator.social.entity.UserCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserCacheRepository extends JpaRepository<UserCache, UUID> {
}
