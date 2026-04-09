package com.hustsimulator.context.message;

import com.hustsimulator.context.entity.Message;
import java.util.List;
import java.util.UUID;

public interface MessageService {
    Message save(UUID eventId, UUID senderId, String type, String content, UUID fileId);
    List<Message> getHistory(UUID eventId);
}
