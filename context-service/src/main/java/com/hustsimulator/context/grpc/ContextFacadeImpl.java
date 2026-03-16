package com.hustsimulator.context.grpc;

import com.hustsimulator.context.building.BuildingService;
import com.hustsimulator.context.entity.Building;
import com.hustsimulator.context.enums.UserActivityState;
import com.hustsimulator.context.grpc.proto.CommonProto;
import com.hustsimulator.context.grpc.proto.ContextProto;
import com.hustsimulator.context.userstate.UserStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextFacadeImpl implements IContextFacade {

    private final UserStateService userStateService;
    private final BuildingService buildingService;

    @Override
    public ContextProto.ZoneCheckResponse checkPlayerZone(ContextProto.ZoneCheckRequest request) {
        String playerId = request.getPlayerId();
        double lat = request.getPosition().getLatitude();
        double lon = request.getPosition().getLongitude();

        // In a real implementation, we would use a spatial query (PostGIS)
        // For now, we iterate over active buildings on the map if we knew the mapId, 
        // or just all buildings as a fallback for the stub.
        List<Building> buildings = buildingService.findActive();
        
        List<ContextProto.Zone> zones = buildings.stream()
                .filter(b -> buildingService.isPointInsideBuilding(b.getId(), lon, lat))
                .map(this::mapToZoneProto)
                .collect(Collectors.toList());

        return ContextProto.ZoneCheckResponse.newBuilder()
                .addAllZones(zones)
                .build();
    }

    @Override
    public CommonProto.StatusResponse reportSpatialTrigger(ContextProto.SpatialTriggerEvent request) {
        log.info("Processing spatial trigger for player {}: {} on zone {}", 
                request.getPlayerId(), request.getTriggerType(), request.getZoneId());
        
        // Logic to transition player state based on entry/exit could go here
        
        return CommonProto.StatusResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Trigger processed")
                .build();
    }

    @Override
    public CommonProto.StatusResponse updatePlayerState(ContextProto.UpdatePlayerStateRequest request) {
        try {
            UUID userId = UUID.fromString(request.getPlayerId());
            UserActivityState activityState = UserActivityState.valueOf(request.getActivityState());
            
            UUID mapId = request.getMapId().isEmpty() ? null : UUID.fromString(request.getMapId());
            UUID eventId = request.getEventId().isEmpty() ? null : UUID.fromString(request.getEventId());

            userStateService.updateActivity(userId, activityState, null);
            
            // If mapId or eventId changed, we could call specific service methods here
            // e.g. userStateService.changeMap(userId, mapId) if it differs from current

            return CommonProto.StatusResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Player state synchronized")
                    .build();
        } catch (Exception e) {
            log.error("Failed to update player state: {}", e.getMessage());
            return CommonProto.StatusResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Update failed: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ContextProto.ActiveEventsResponse getActiveEvents(ContextProto.ActiveEventsRequest request) {
        // Placeholder for event active events query
        return ContextProto.ActiveEventsResponse.newBuilder().build();
    }

    private ContextProto.Zone mapToZoneProto(Building building) {
        return ContextProto.Zone.newBuilder()
                .setZoneId(building.getId().toString())
                .setName(building.getName())
                .setType("BUILDING")
                .build();
    }
}
