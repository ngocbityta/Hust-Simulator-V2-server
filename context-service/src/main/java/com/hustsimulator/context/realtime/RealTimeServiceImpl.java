package com.hustsimulator.context.realtime;

import com.corundumstudio.socketio.SocketIOServer;
import com.hustsimulator.context.entity.Message;
import com.hustsimulator.context.message.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class RealTimeServiceImpl implements RealTimeService {

    private final SocketIOServer server;
    private final MessageService messageService;

    @Autowired
    public RealTimeServiceImpl(SocketIOServer server, MessageService messageService) {
        this.server = server;
        this.messageService = messageService;
    }

    @PostConstruct
    public void initListeners() {
        server.addConnectListener(client ->
            log.info("SocketIO Client connected: {}", client.getSessionId())
        );

        server.addDisconnectListener(client ->
            log.info("SocketIO Client disconnected: {}", client.getSessionId())
        );

        // Join a class room
        server.addEventListener("class:join", String.class, (client, classId, ackRequest) -> {
            String roomName = "class_" + classId;
            client.joinRoom(roomName);
            log.info("Client {} joined classroom: {}", client.getSessionId(), classId);

            try {
                List<Message> history = messageService.getHistory(UUID.fromString(classId));
                client.sendEvent("class:history", history);
            } catch (Exception e) {
                log.warn("Could not load chat history for class {}: {}", classId, e.getMessage());
            }

            client.sendEvent("class:joined", classId);
        });

        // Unified message handler
        server.addEventListener("class:message", Map.class, (client, data, ackRequest) -> {
            try {
                String eventId = (String) data.get("eventId");
                String senderId = (String) data.get("senderId");
                String type = (String) data.getOrDefault("type", "text");
                String content = (String) data.get("content");
                String fileIdStr = (String) data.get("fileId");
                UUID fileId = fileIdStr != null ? UUID.fromString(fileIdStr) : null;

                Message saved = messageService.save(
                    UUID.fromString(eventId),
                    UUID.fromString(senderId),
                    type,
                    content,
                    fileId
                );

                server.getRoomOperations("class_" + eventId).sendEvent("class:message", saved);

                log.debug("Message persisted and broadcast: type={} event={}", type, eventId);
            } catch (Exception e) {
                log.error("Failed to process message: {}", e.getMessage(), e);
            }
        });

        // Leave a class room
        server.addEventListener("class:leave", String.class, (client, classId, ackRequest) -> {
            client.leaveRoom("class_" + classId);
            log.info("Client {} left classroom: {}", client.getSessionId(), classId);
        });
    }

    @Override
    public void broadcast(String roomName, String eventName, Object data) {
        log.info("Broadcasting '{}' to room '{}'", eventName, roomName);
        server.getRoomOperations(roomName).sendEvent(eventName, data);
    }

    @Override
    public void broadcastToAll(String eventName, Object data) {
        log.info("Broadcasting '{}' to all clients", eventName);
        server.getBroadcastOperations().sendEvent(eventName, data);
    }
}
