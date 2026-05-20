package com.hustsimulator.social.directmessage.repository;

import com.hustsimulator.social.entity.DirectMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

    Page<DirectMessage> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    Optional<DirectMessage> findFirstByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    long countByConversationIdAndReceiverIdAndIsReadFalse(UUID conversationId, UUID receiverId);
}
