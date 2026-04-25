package com.hustsimulator.messaging.realtime;

import com.corundumstudio.socketio.SocketIOServer;
import com.hustsimulator.messaging.entity.Message;
import com.hustsimulator.messaging.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealTimeServiceImpl implements RealTimeService {

    private final SocketIOServer server;
    private final MessageService messageService;

    @PostConstruct
    public void initListeners() {
        server.addConnectListener(client -> {
            String userIdStr = client.getHandshakeData().getHttpHeaders().get("X-User-Id");
            if (userIdStr != null) {
                client.set("userId", userIdStr);
            }
            log.info("SocketIO Client connected: {} with userId: {}", client.getSessionId(), userIdStr);
        });

        server.addDisconnectListener(client ->
            log.info("SocketIO Client disconnected: {}", client.getSessionId())
        );

        server.addEventListener("class:join", Object.class, (client, data, ackRequest) -> {
            log.info("Received class:join. Data type: {}, Data: {}", data != null ? data.getClass().getName() : "null", data);
            String detailId = data.toString();
            String roomName = "class_" + detailId;
            client.joinRoom(roomName);
            log.info("Client {} joined classroom session: {}. Room size: {}", client.getSessionId(), detailId, server.getRoomOperations(roomName).getClients().size());

            try {
                // history is now scoped to the specific occurrence (detailId)
                List<Message> history = messageService.getHistory(UUID.fromString(detailId));
                client.sendEvent("class:history", history);
            } catch (Exception e) {
                log.warn("Could not load chat history for detail {}: {}", detailId, e.getMessage());
            }

            client.sendEvent("class:joined", detailId);
        });

        server.addEventListener("class:message", Object.class, (client, data, ackRequest) -> {
            log.info("Received class:message from client {}: {}. Data type: {}", client.getSessionId(), data, data != null ? data.getClass().getName() : "null");
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapData = (Map<String, Object>) data;
                // eventId here is actually the detailId (occurrence)
                String detailId = (String) mapData.get("eventId");
                
                // Get senderId directly from secure client attributes first
                String secureUserId = client.get("userId");
                String senderId = secureUserId != null ? secureUserId : (String) mapData.get("senderId");
                
                String typeStr = (String) mapData.getOrDefault("type", "TEXT");
                com.hustsimulator.messaging.enums.MessageType type = com.hustsimulator.messaging.enums.MessageType.TEXT;
                try {
                    type = com.hustsimulator.messaging.enums.MessageType.valueOf(typeStr.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    log.warn("Invalid message type {}, falling back to TEXT", typeStr);
                }
                
                String content = (String) mapData.get("content");
                String fileIdStr = (String) mapData.get("fileId");
                UUID fileId = fileIdStr != null ? UUID.fromString(fileIdStr) : null;

                Message saved = messageService.save(
                    UUID.fromString(detailId),
                    UUID.fromString(senderId),
                    type,
                    content,
                    fileId
                );

                server.getRoomOperations("class_" + detailId).sendEvent("class:message", saved);

                log.debug("Message persisted and broadcast: type={} detail={}", type, detailId);
            } catch (Exception e) {
                log.error("Failed to process message: {}. Data: {}", e.getMessage(), data, e);
            }
        });

        server.addEventListener("class:leave", Object.class, (client, data, ackRequest) -> {
            String detailId = data.toString();
            client.leaveRoom("class_" + detailId);
            log.info("Client {} left classroom session: {}", client.getSessionId(), detailId);
        });

        server.start();
        log.info("SocketIO Server started on port: {}", server.getConfiguration().getPort());
    }

    @PreDestroy
    public void stopServer() {
        if (server != null) {
            server.stop();
            log.info("SocketIO Server stopped.");
        }
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
