package com.hustsimulator.streaming.livekit;

public interface LiveKitService {
    String createToken(String roomName, String identity, String displayName, boolean canPublish);
}
