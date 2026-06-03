package com.hustsimulator.social.config;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class RealTimeConfig {

    @Value("${socketio.host:0.0.0.0}")
    private String host;

    @Value("${socketio.port:9092}")
    private int port;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        
        // Add support for Java 8 Date/Time API (LocalDateTime) in Socket.IO JSON serialization
        com.fasterxml.jackson.datatype.jsr310.JavaTimeModule javaTimeModule = new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule();
        config.setJsonSupport(new com.corundumstudio.socketio.protocol.JacksonJsonSupport(javaTimeModule));
        
        return new SocketIOServer(config);
    }
}
