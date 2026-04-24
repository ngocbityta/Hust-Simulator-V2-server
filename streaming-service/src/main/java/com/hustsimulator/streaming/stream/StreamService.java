package com.hustsimulator.streaming.stream;

import com.hustsimulator.streaming.enums.StreamEntityType;
import java.util.List;
import java.util.UUID;

public interface StreamService {
    StreamDTO.StreamTokenResponse startStream(StreamDTO.StartStreamRequest request, UUID userId);
    StreamDTO.StreamTokenResponse joinStream(String roomName, StreamDTO.JoinStreamRequest request, UUID userId);
    void endStream(String roomName, UUID userId);
    List<StreamDTO.StreamSessionInfo> getActiveStreams();
    StreamDTO.StreamSessionInfo getStreamByEntity(StreamEntityType entityType, UUID entityId);
}
