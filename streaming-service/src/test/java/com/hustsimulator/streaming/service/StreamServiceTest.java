package com.hustsimulator.streaming.service;

import com.hustsimulator.streaming.dto.StreamDTO;
import com.hustsimulator.streaming.entity.StreamSession;
import com.hustsimulator.streaming.repository.StreamSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreamServiceTest {

    @Mock private StreamSessionRepository streamSessionRepository;
    @Mock private LiveKitService liveKitService;

    private StreamService streamService;

    @BeforeEach
    void setUp() throws Exception {
        streamService = new StreamService(streamSessionRepository, liveKitService);
        // Inject livekitUrl via reflection
        var urlField = StreamService.class.getDeclaredField("livekitUrl");
        urlField.setAccessible(true);
        urlField.set(streamService, "http://livekit:7880");
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private StreamDTO.StartStreamRequest buildStartRequest(UUID entityId, String entityType) {
        StreamDTO.StartStreamRequest req = new StreamDTO.StartStreamRequest();
        req.setEntityId(entityId);
        req.setEntityType(entityType);
        req.setParticipantName("TestHost");
        return req;
    }

    private StreamSession buildSession(UUID entityId, String entityType, String status) {
        return StreamSession.builder()
                .id(UUID.randomUUID())
                .roomName(entityType.toLowerCase() + "_stream_" + entityId)
                .entityType(entityType)
                .entityId(entityId)
                .status(status)
                .build();
    }

    // ─── startStream ────────────────────────────────────────────────────────

    @Nested
    class StartStream {

        @Test
        void shouldCreateNewSession_whenNoActiveExists() {
            UUID eventId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            StreamDTO.StartStreamRequest request = buildStartRequest(eventId, "EVENT");

            when(streamSessionRepository.findByEntityTypeAndEntityIdAndStatus("EVENT", eventId, "ACTIVE"))
                    .thenReturn(Optional.empty());
            when(streamSessionRepository.save(any(StreamSession.class)))
                    .thenAnswer(i -> i.getArgument(0));
            when(liveKitService.createToken(any(), eq(userId.toString()), eq("TestHost"), eq(true)))
                    .thenReturn("mock-jwt-token");

            StreamDTO.StreamTokenResponse response = streamService.startStream(request, userId);

            assertThat(response.getToken()).isEqualTo("mock-jwt-token");
            assertThat(response.getRoomName()).isEqualTo("event_stream_" + eventId);
            assertThat(response.getServerUrl()).isEqualTo("http://livekit:7880");

            // Verify session was saved
            ArgumentCaptor<StreamSession> captor = ArgumentCaptor.forClass(StreamSession.class);
            verify(streamSessionRepository).save(captor.capture());
            StreamSession saved = captor.getValue();
            assertThat(saved.getEntityType()).isEqualTo("EVENT");
            assertThat(saved.getEntityId()).isEqualTo(eventId);
            assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        void shouldReuseExistingSession_whenActiveExists() {
            UUID eventId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            StreamSession existing = buildSession(eventId, "EVENT", "ACTIVE");

            when(streamSessionRepository.findByEntityTypeAndEntityIdAndStatus("EVENT", eventId, "ACTIVE"))
                    .thenReturn(Optional.of(existing));
            when(liveKitService.createToken(eq(existing.getRoomName()), eq(userId.toString()), any(), eq(true)))
                    .thenReturn("reuse-token");

            StreamDTO.StartStreamRequest request = buildStartRequest(eventId, "EVENT");
            StreamDTO.StreamTokenResponse response = streamService.startStream(request, userId);

            assertThat(response.getToken()).isEqualTo("reuse-token");
            assertThat(response.getRoomName()).isEqualTo(existing.getRoomName());
            // Should NOT save a new session
            verify(streamSessionRepository, never()).save(any());
        }
    }

    // ─── joinStream ─────────────────────────────────────────────────────────

    @Nested
    class JoinStream {

        @Test
        void shouldReturnSubscriberToken_whenStreamIsActive() {
            UUID entityId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            StreamSession session = buildSession(entityId, "EVENT", "ACTIVE");

            when(streamSessionRepository.findByRoomName(session.getRoomName()))
                    .thenReturn(Optional.of(session));
            when(liveKitService.createToken(eq(session.getRoomName()), eq(userId.toString()), eq("Viewer"), eq(false)))
                    .thenReturn("viewer-token");

            StreamDTO.JoinStreamRequest request = new StreamDTO.JoinStreamRequest();
            request.setParticipantName("Viewer");

            StreamDTO.StreamTokenResponse response = streamService.joinStream(session.getRoomName(), request, userId);

            assertThat(response.getToken()).isEqualTo("viewer-token");
        }

        @Test
        void shouldThrow404_whenStreamNotFound() {
            UUID userId = UUID.randomUUID();
            when(streamSessionRepository.findByRoomName("nonexistent")).thenReturn(Optional.empty());

            StreamDTO.JoinStreamRequest request = new StreamDTO.JoinStreamRequest();
            request.setParticipantName("Viewer");

            assertThatThrownBy(() -> streamService.joinStream("nonexistent", request, userId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Stream not found");
        }

        @Test
        void shouldThrow400_whenStreamIsEnded() {
            UUID entityId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            StreamSession ended = buildSession(entityId, "EVENT", "ENDED");

            when(streamSessionRepository.findByRoomName(ended.getRoomName()))
                    .thenReturn(Optional.of(ended));

            StreamDTO.JoinStreamRequest request = new StreamDTO.JoinStreamRequest();
            request.setParticipantName("Viewer");

            assertThatThrownBy(() -> streamService.joinStream(ended.getRoomName(), request, userId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Stream is not active");
        }
    }

    // ─── endStream ──────────────────────────────────────────────────────────

    @Nested
    class EndStream {

        @Test
        void shouldMarkSessionAsEnded() {
            UUID entityId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            StreamSession session = buildSession(entityId, "EVENT", "ACTIVE");

            when(streamSessionRepository.findByRoomName(session.getRoomName()))
                    .thenReturn(Optional.of(session));
            when(streamSessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            streamService.endStream(session.getRoomName(), userId);

            ArgumentCaptor<StreamSession> captor = ArgumentCaptor.forClass(StreamSession.class);
            verify(streamSessionRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("ENDED");
        }

        @Test
        void shouldThrow_whenAlreadyEnded() {
            UUID entityId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            StreamSession session = buildSession(entityId, "EVENT", "ENDED");

            when(streamSessionRepository.findByRoomName(session.getRoomName()))
                    .thenReturn(Optional.of(session));

            assertThatThrownBy(() -> streamService.endStream(session.getRoomName(), userId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("already ended");
        }
    }

    // ─── getActiveStreams ────────────────────────────────────────────────────

    @Test
    void getActiveStreams_shouldReturnMappedList() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(streamSessionRepository.findAllByStatus("ACTIVE"))
                .thenReturn(List.of(buildSession(id1, "EVENT", "ACTIVE"), buildSession(id2, "POST", "ACTIVE")));

        List<StreamDTO.StreamSessionInfo> result = streamService.getActiveStreams();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEntityType()).isEqualTo("EVENT");
        assertThat(result.get(1).getEntityType()).isEqualTo("POST");
    }

    // ─── getStreamByEntity ──────────────────────────────────────────────────

    @Test
    void getStreamByEntity_shouldReturn_whenFound() {
        UUID eventId = UUID.randomUUID();
        StreamSession session = buildSession(eventId, "EVENT", "ACTIVE");

        when(streamSessionRepository.findByEntityTypeAndEntityIdAndStatus("EVENT", eventId, "ACTIVE"))
                .thenReturn(Optional.of(session));

        StreamDTO.StreamSessionInfo info = streamService.getStreamByEntity("EVENT", eventId);

        assertThat(info.getEntityId()).isEqualTo(eventId);
        assertThat(info.getRoomName()).isEqualTo(session.getRoomName());
    }

    @Test
    void getStreamByEntity_shouldThrow404_whenNotFound() {
        UUID eventId = UUID.randomUUID();
        when(streamSessionRepository.findByEntityTypeAndEntityIdAndStatus("EVENT", eventId, "ACTIVE"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> streamService.getStreamByEntity("EVENT", eventId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No active stream found");
    }
}
