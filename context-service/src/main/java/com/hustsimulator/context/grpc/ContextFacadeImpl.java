package com.hustsimulator.context.grpc;

import com.hustsimulator.context.building.BuildingService;
import com.hustsimulator.context.entity.Building;
import com.hustsimulator.context.entity.RecurringEvent;
import com.hustsimulator.context.enums.UserActivityState;
import com.hustsimulator.context.grpc.proto.ContextProto;
import com.hustsimulator.context.recurringevent.RecurringEventService;
import com.hustsimulator.context.userstate.UserStateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hustsimulator.context.common.ContextConstants;
import com.hustsimulator.context.common.GeometryUtils;
import com.hustsimulator.context.entity.Room;
import com.hustsimulator.context.room.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextFacadeImpl implements IContextFacade {

	private final UserStateService userStateService;
	private final BuildingService buildingService;
	private final RoomService roomService;
	private final RecurringEventService recurringEventService;
	private final ObjectMapper objectMapper;

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
				.map(e -> {
					Map<String, String> payload = new HashMap<>();
					if (e.getRoomId() != null) {
						try {
							Room room = roomService.findById(e.getRoomId());
							Building building = buildingService.findById(room.getBuildingId());
							double[] centroid = GeometryUtils.getCentroid(building.getCoordinates(), objectMapper);
							// Map coordinates (lon, lat) -> (lng, lat)
							payload.put(ContextConstants.PAYLOAD_LONGITUDE, String.valueOf(centroid[0]));
							payload.put(ContextConstants.PAYLOAD_LATITUDE, String.valueOf(centroid[1]));
							payload.put(ContextConstants.PAYLOAD_BUILDING_NAME, building.getName());
							payload.put(ContextConstants.PAYLOAD_ROOM_NAME, room.getName());
						} catch (Exception ex) {
							log.warn("Failed to get coordinates for event {}: {}", e.getId(), ex.getMessage());
						}
					}

					return ContextProto.ContextEvent.newBuilder()
							.setEventId(e.getId().toString())
							.setPlayerId(request.getPlayerId())
							.setEventType(ContextConstants.EVENT_TYPE_VIRTUAL_CLASS)
							.setTitle(e.getName())
							.setDescription(e.getDescription() != null ? e.getDescription() : "")
							.putAllPayload(payload)
							.build();
				})
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
