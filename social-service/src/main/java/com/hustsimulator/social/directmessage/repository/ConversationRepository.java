package com.hustsimulator.social.directmessage.repository;

import com.hustsimulator.social.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("SELECT c FROM Conversation c WHERE (c.partnerAId = :userId1 AND c.partnerBId = :userId2) OR (c.partnerAId = :userId2 AND c.partnerBId = :userId1)")
    Optional<Conversation> findByPartners(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);

    @Query("SELECT c FROM Conversation c WHERE c.partnerAId = :userId OR c.partnerBId = :userId")
    List<Conversation> findAllByUserId(@Param("userId") UUID userId);
}
