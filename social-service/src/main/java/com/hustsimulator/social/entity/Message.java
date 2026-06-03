package com.hustsimulator.social.entity;

import com.hustsimulator.social.common.BaseEntity;
import com.hustsimulator.social.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

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

    /** text | file | image — stored as lowercase to match DB check constraint */
    @Convert(converter = MessageType.TypeConverter.class)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    /** Text content for type=text, optional caption for file/image */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** Reference to stored_files.id for type=file or type=image */
    @Column(name = "file_id")
    private UUID fileId;

    /** Automatically join and fetch file info when querying messages */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "file_id", insertable = false, updatable = false)
    private StoredFile fileInfo;
}
