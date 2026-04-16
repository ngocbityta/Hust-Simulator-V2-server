package com.hustsimulator.messaging.message;

import com.hustsimulator.messaging.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByEventIdOrderByCreatedAtAsc(UUID eventId);

    @Query("SELECT DISTINCT m.eventId FROM Message m WHERE m.senderId = :userId")
    List<UUID> findDistinctEventIdsBySenderId(@Param("userId") UUID userId);
}
