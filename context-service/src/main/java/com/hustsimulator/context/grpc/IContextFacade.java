package com.hustsimulator.context.grpc;

import com.hustsimulator.context.grpc.proto.CommonProto;
import com.hustsimulator.context.grpc.proto.ContextProto;
import io.grpc.stub.StreamObserver;

import java.util.UUID;

/**
 * Facade interface for all context-related operations.
 * Decouples gRPC transport from business logic.
 */
public interface IContextFacade {
    
    /**
     * Checks which zones a player is currently in based on their position.
     */
    ContextProto.ZoneCheckResponse checkPlayerZone(ContextProto.ZoneCheckRequest request);

    /**
     * Processes a spatial trigger event (entry/exit).
     */
    CommonProto.StatusResponse reportSpatialTrigger(ContextProto.SpatialTriggerEvent request);

    /**
     * Updates comprehensive player state (position, activity, map, event).
     */
    CommonProto.StatusResponse updatePlayerState(ContextProto.UpdatePlayerStateRequest request);

    /**
     * Gets active events for a player.
     */
    ContextProto.ActiveEventsResponse getActiveEvents(ContextProto.ActiveEventsRequest request);
}
