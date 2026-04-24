package com.hustsimulator.context.grpc;

import com.hustsimulator.context.grpc.proto.ContextProto;


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
    com.google.protobuf.Empty reportSpatialTrigger(ContextProto.SpatialTriggerEvent request);

    /**
     * Updates comprehensive player state (position, activity, map, event).
     */
    com.google.protobuf.Empty updatePlayerState(ContextProto.UpdatePlayerStateRequest request);

    /**
     * Gets active events for a player.
     */
    ContextProto.ActiveEventsResponse getActiveEvents(ContextProto.ActiveEventsRequest request);
}
