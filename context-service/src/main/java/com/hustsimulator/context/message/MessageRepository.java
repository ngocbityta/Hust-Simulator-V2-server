package com.hustsimulator.context.message;

import com.hustsimulator.context.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByEventIdOrderByCreatedAtAsc(UUID eventId);
}
