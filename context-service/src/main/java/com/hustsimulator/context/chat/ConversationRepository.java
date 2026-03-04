package com.hustsimulator.context.chat;

import com.hustsimulator.context.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("SELECT c FROM Conversation c WHERE (c.partnerAId = :userId OR c.partnerBId = :userId) AND c.isDeleted = false")
    List<Conversation> findByUserId(UUID userId);

    Optional<Conversation> findByPartnerAIdAndPartnerBId(UUID partnerAId, UUID partnerBId);
}
