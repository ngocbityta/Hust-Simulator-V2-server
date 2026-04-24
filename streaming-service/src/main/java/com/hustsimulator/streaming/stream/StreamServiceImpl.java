package com.hustsimulator.streaming.stream;

import com.hustsimulator.streaming.entity.StreamSession;
import com.hustsimulator.streaming.livekit.LiveKitService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.hustsimulator.streaming.enums.StreamStatus;
import com.hustsimulator.streaming.enums.StreamEntityType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StreamServiceImpl implements StreamService {

    private final StreamSessionRepository streamSessionRepository;
    private final LiveKitService liveKitService;

    @Value("${livekit.url:http://localhost:7880}")
    private String livekitUrl;

    /**
     * Start a new stream or rejoin an existing one for the given entity.
     * Uses the authenticated userId as the LiveKit participant identity.
     */
    @Transactional
    public StreamDTO.StreamTokenResponse startStream(StreamDTO.StartStreamRequest request, UUID userId) {
        // Check if an active stream already exists for this entity
        Optional<StreamSession> existingSession = streamSessionRepository
                .findByEntityTypeAndEntityIdAndStatus(
                        request.getEntityType(), request.getEntityId(), StreamStatus.ACTIVE
                );

        String roomName;
        if (existingSession.isPresent()) {
            roomName = existingSession.get().getRoomName();
        } else {
            roomName = request.getEntityType().name().toLowerCase() + "_stream_" + request.getEntityId();

            StreamSession newSession = StreamSession.builder()
                    .roomName(roomName)
                    .entityType(request.getEntityType())
                    .entityId(request.getEntityId())
                    .status(StreamStatus.ACTIVE)
                    .build();

            streamSessionRepository.save(newSession);
        }

        // Use userId as identity so LiveKit can uniquely identify the participant
        String identity = userId.toString();
        String displayName = request.getParticipantName();
        String token = liveKitService.createToken(roomName, identity, displayName, true);
        return new StreamDTO.StreamTokenResponse(token, roomName, livekitUrl);
    }

    /**
     * Join an existing active stream as a viewer (subscriber only).
     */
    public StreamDTO.StreamTokenResponse joinStream(String roomName, StreamDTO.JoinStreamRequest request, UUID userId) {
        StreamSession session = streamSessionRepository.findByRoomName(roomName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stream not found"));

        if (StreamStatus.ACTIVE != session.getStatus()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stream is not active");
        }

        String identity = userId.toString();
        String displayName = request.getParticipantName();
        String token = liveKitService.createToken(roomName, identity, displayName, false);
        return new StreamDTO.StreamTokenResponse(token, roomName, livekitUrl);
    }

    /**
     * End an active stream session.
     */
    @Transactional
    public void endStream(String roomName, UUID userId) {
        StreamSession session = streamSessionRepository.findByRoomName(roomName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stream not found"));

        if (StreamStatus.ACTIVE != session.getStatus()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stream is already ended");
        }

        session.setStatus(StreamStatus.ENDED);
        streamSessionRepository.save(session);
    }

    /**
     * List all currently active stream sessions.
     */
    public List<StreamDTO.StreamSessionInfo> getActiveStreams() {
        return streamSessionRepository.findAllByStatus(StreamStatus.ACTIVE).stream()
                .map(this::toSessionInfo)
                .toList();
    }

    /**
     * Find the active stream for a specific entity (event or post).
     */
    public StreamDTO.StreamSessionInfo getStreamByEntity(StreamEntityType entityType, UUID entityId) {
        StreamSession session = streamSessionRepository
                .findByEntityTypeAndEntityIdAndStatus(entityType, entityId, StreamStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No active stream found for this entity"));

        return toSessionInfo(session);
    }

    private StreamDTO.StreamSessionInfo toSessionInfo(StreamSession session) {
        return StreamDTO.StreamSessionInfo.builder()
                .id(session.getId())
                .roomName(session.getRoomName())
                .entityType(session.getEntityType())
                .entityId(session.getEntityId())
                .status(session.getStatus())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
