package com.hustsimulator.messaging.realtime;

public interface RealTimeService {
    void broadcast(String roomName, String eventName, Object data);
    void broadcastToAll(String eventName, Object data);
}
