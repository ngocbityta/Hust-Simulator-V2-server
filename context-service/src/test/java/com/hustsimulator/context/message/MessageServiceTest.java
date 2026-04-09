package com.hustsimulator.context.message;

import com.hustsimulator.context.entity.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private MessageRepository messageRepository;
    @InjectMocks private MessageServiceImpl messageService;

    @Test
    void save_shouldPersistAndReturn() {
        UUID eventId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        Message msg = Message.builder()
                .eventId(eventId)
                .senderId(senderId)
                .type("text")
                .content("hello")
                .build();

        when(messageRepository.save(any(Message.class))).thenReturn(msg);

        Message result = messageService.save(eventId, senderId, "text", "hello", null);

        assertThat(result.getContent()).isEqualTo("hello");
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void getHistory_shouldReturnList() {
        UUID eventId = UUID.randomUUID();
        when(messageRepository.findByEventIdOrderByCreatedAtAsc(eventId)).thenReturn(List.of(new Message()));

        List<Message> result = messageService.getHistory(eventId);

        assertThat(result).hasSize(1);
    }
}
