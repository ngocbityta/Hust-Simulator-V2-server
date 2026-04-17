package com.hustsimulator.streaming.repository;

import com.hustsimulator.streaming.entity.StreamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StreamSessionRepository extends JpaRepository<StreamSession, UUID> {
    Optional<StreamSession> findByRoomName(String roomName);
    Optional<StreamSession> findByEntityTypeAndEntityIdAndStatus(String entityType, UUID entityId, String status);
    List<StreamSession> findAllByStatus(String status);
}
