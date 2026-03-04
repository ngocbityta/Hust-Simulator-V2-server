package com.hustsimulator.context.chat;

import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.Conversation;
import com.hustsimulator.context.entity.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;
    @InjectMocks private ChatService chatService;

    @Test
    void findConversationById_shouldReturn() {
        UUID id = UUID.randomUUID();
        Conversation conv = Conversation.builder().partnerAId(UUID.randomUUID()).partnerBId(UUID.randomUUID()).build();
        conv.setId(id);
        when(conversationRepository.findById(id)).thenReturn(Optional.of(conv));

        Conversation result = chatService.findConversationById(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findConversationById_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(conversationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.findConversationById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getOrCreateConversation_shouldCreateNew_whenNotExists() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        Conversation conv = Conversation.builder().partnerAId(a).partnerBId(b).build();
        when(conversationRepository.findByPartnerAIdAndPartnerBId(a, b)).thenReturn(Optional.empty());
        when(conversationRepository.findByPartnerAIdAndPartnerBId(b, a)).thenReturn(Optional.empty());
        when(conversationRepository.save(any())).thenReturn(conv);

        Conversation result = chatService.getOrCreateConversation(a, b);

        assertThat(result.getPartnerAId()).isEqualTo(a);
        verify(conversationRepository).save(any());
    }

    @Test
    void sendMessage_shouldSave() {
        Message msg = Message.builder().conversationId(UUID.randomUUID()).content("Hello").build();
        when(messageRepository.save(msg)).thenReturn(msg);

        Message result = chatService.sendMessage(msg);

        assertThat(result.getContent()).isEqualTo("Hello");
    }

    @Test
    void deleteMessage_shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        Message msg = Message.builder().content("Hello").build();
        msg.setId(id);
        when(messageRepository.findById(id)).thenReturn(Optional.of(msg));

        chatService.deleteMessage(id);

        assertThat(msg.getIsDeleted()).isTrue();
        verify(messageRepository).save(msg);
    }
}
