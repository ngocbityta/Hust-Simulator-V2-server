package com.hustsimulator.messaging.entity;

import com.hustsimulator.messaging.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import com.hustsimulator.messaging.enums.MessageType;

@Entity
@Table(name = "messages_chat")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message extends BaseEntity {

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    /** text | file | image */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    /** Text content for type=text, optional caption for file/image */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** Reference to stored_files.id for type=file or type=image */
    @Column(name = "file_id")
    private UUID fileId;
}
