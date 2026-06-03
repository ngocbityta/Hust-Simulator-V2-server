package com.hustsimulator.social.directmessage.repository;

import com.hustsimulator.social.entity.DirectMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

    Page<DirectMessage> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    Optional<DirectMessage> findFirstByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    long countByConversationIdAndReceiverIdAndIsReadFalse(UUID conversationId, UUID receiverId);

    @Modifying
    @Query("UPDATE DirectMessage m SET m.isRead = true WHERE m.conversation.id = :conversationId AND m.receiverId = :receiverId AND m.isRead = false")
    void markAsReadByConversationIdAndReceiverId(@Param("conversationId") UUID conversationId, @Param("receiverId") UUID receiverId);
}
