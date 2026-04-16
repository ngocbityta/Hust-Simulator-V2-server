package com.hustsimulator.context.grpc;

import com.hustsimulator.context.building.BuildingService;
import com.hustsimulator.context.entity.Building;
import com.hustsimulator.context.entity.RecurringEvent;
import com.hustsimulator.context.enums.UserActivityState;
import com.hustsimulator.context.grpc.proto.ContextProto;
import com.hustsimulator.context.recurringevent.RecurringEventService;
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
        private final RecurringEventService recurringEventService;

        @Override
        public ContextProto.ZoneCheckResponse checkPlayerZone(ContextProto.ZoneCheckRequest request) {
                String playerId = request.getPlayerId();
                double lat = request.getPosition().getLatitude();
                double lon = request.getPosition().getLongitude();

                // Check against active buildings (using lon/lat from proto)
                List<Building> buildings = buildingService.findActive();

                List<ContextProto.Zone> zones = buildings.stream()
                                .filter(b -> buildingService.isPointInsideBuilding(b.getId(), lon, lat))
                                .map(this::mapToZoneProto)
                                .collect(Collectors.toList());

                log.debug("Zone check for player {}: found {} zones", playerId, zones.size());
                return ContextProto.ZoneCheckResponse.newBuilder()
                                .addAllZones(zones)
                                .build();
        }

        @Override
        public com.google.protobuf.Empty reportSpatialTrigger(ContextProto.SpatialTriggerEvent request) {
                log.info("Spatial trigger for player {}: {} on {}",
                                request.getPlayerId(), request.getTriggerType(), request.getZoneId());

                return com.google.protobuf.Empty.getDefaultInstance();
        }

        @Override
        public com.google.protobuf.Empty updatePlayerState(ContextProto.UpdatePlayerStateRequest request) {
                UUID userId = UUID.fromString(request.getPlayerId());
                UserActivityState activityState = UserActivityState.valueOf(request.getActivityState().name());

                // Sync activity and position
                userStateService.updateActivity(userId, activityState, null);
                userStateService.syncPositionState(userId, request.getPosition().getLongitude(),
                                request.getPosition().getLatitude());

                return com.google.protobuf.Empty.getDefaultInstance();
        }

        @Override
        public ContextProto.ActiveEventsResponse getActiveEvents(ContextProto.ActiveEventsRequest request) {
                List<RecurringEvent> activeEvents = recurringEventService.findActive();

                List<ContextProto.ContextEvent> eventProtos = activeEvents.stream()
                                .map(e -> ContextProto.ContextEvent.newBuilder()
                                                .setEventId(e.getId().toString())
                                                .setPlayerId(request.getPlayerId())
                                                .setEventType("VIRTUAL_CLASS")
                                                .setTitle(e.getName())
                                                .setDescription(e.getDescription() != null ? e.getDescription() : "")
                                                .build())
                                .collect(Collectors.toList());

                return ContextProto.ActiveEventsResponse.newBuilder()
                                .addAllEvents(eventProtos)
                                .build();
        }

        private ContextProto.Zone mapToZoneProto(Building building) {
                return ContextProto.Zone.newBuilder()
                                .setZoneId(building.getId().toString())
                                .setName(building.getName())
                                .setType("BUILDING")
                                .build();
        }
}
