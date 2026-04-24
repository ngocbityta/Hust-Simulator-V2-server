package com.hustsimulator.streaming.stream;

import com.hustsimulator.streaming.entity.StreamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hustsimulator.streaming.enums.StreamStatus;
import com.hustsimulator.streaming.enums.StreamEntityType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StreamSessionRepository extends JpaRepository<StreamSession, UUID> {
    Optional<StreamSession> findByRoomName(String roomName);
    Optional<StreamSession> findByEntityTypeAndEntityIdAndStatus(StreamEntityType entityType, UUID entityId, StreamStatus status);
    List<StreamSession> findAllByStatus(StreamStatus status);
}
