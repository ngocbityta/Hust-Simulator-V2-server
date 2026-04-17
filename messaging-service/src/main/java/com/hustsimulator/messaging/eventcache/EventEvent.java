package com.hustsimulator.messaging.eventcache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEvent implements Serializable {

    public enum EventType {
        CREATED, UPDATED, DELETED
    }

    private EventType eventType;
    private UUID eventId;
    private String name;
    private String description;
    private UUID mapId;
    private String status;
}
