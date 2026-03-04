package com.hustsimulator.context.chat;

import com.hustsimulator.context.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    List<Message> findByConversationIdAndIsReadFalseAndReceiverIdOrderByCreatedAtAsc(UUID conversationId, UUID receiverId);
}
