package com.hustsimulator.context.dashboard;

import com.hustsimulator.context.building.BuildingRepository;
import com.hustsimulator.context.entity.Building;
import com.hustsimulator.context.entity.Event;
import com.hustsimulator.context.enums.EventStatus;
import com.hustsimulator.context.enums.RecurringEventStatus;
import com.hustsimulator.context.enums.RoomStatus;
import com.hustsimulator.context.event.EventRepository;
import com.hustsimulator.context.heatmap.HeatmapHistoryRepository;
import com.hustsimulator.context.recurringevent.RecurringEventRepository;
import com.hustsimulator.context.room.RoomRepository;
import com.hustsimulator.context.userstate.UserStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final EventRepository eventRepository;
    private final RecurringEventRepository recurringEventRepository;
    private final com.hustsimulator.context.recurringeventdetail.RecurringEventDetailRepository recurringEventDetailRepository;
    private final UserStateRepository userStateRepository;
    private final HeatmapHistoryRepository heatmapHistoryRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public DashboardStatsDTO getStats() {
        // --- Summary counts ---
        long totalBuildings = buildingRepository.count();
        long totalRooms = roomRepository.count();

        var allRooms = roomRepository.findAll();
        long roomsBusy = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.BUSY).count();
        long roomsEmpty = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.EMPTY).count();
        long roomsClosed = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.CLOSED).count();

        long totalEvents = eventRepository.count();
        long eventsOngoing = eventRepository.findByStatus(EventStatus.ONGOING).size();

        long totalRecurringEvents = recurringEventRepository.count();
        long recurringEventsOngoing = recurringEventRepository.findByStatus(RecurringEventStatus.ONGOING).size();

        // --- User Activity Distribution ---
        Map<String, Long> userActivityDistribution = new LinkedHashMap<>();
        for (Object[] row : userStateRepository.countByActivityState()) {
            userActivityDistribution.put(row[0].toString(), (Long) row[1]);
        }

        // --- Room Status Distribution ---
        Map<String, Long> roomStatusDistribution = new LinkedHashMap<>();
        roomStatusDistribution.put("EMPTY", roomsEmpty);
        roomStatusDistribution.put("BUSY", roomsBusy);
        roomStatusDistribution.put("CLOSED", roomsClosed);

        // --- Event Status Breakdown ---
        Map<String, Long> eventStatusDistribution = new LinkedHashMap<>();
        for (EventStatus status : EventStatus.values()) {
            long count = eventRepository.findByStatus(status).size();
            eventStatusDistribution.put(status.name(), count);
        }

        // --- Users per Building ---
        List<DashboardStatsDTO.BuildingUserCount> usersPerBuilding = new ArrayList<>();
        Map<UUID, String> buildingNames = buildingRepository.findAll().stream()
                .collect(Collectors.toMap(Building::getId, Building::getName));

        for (Object[] row : userStateRepository.countUsersByBuilding()) {
            UUID buildingId = (UUID) row[0];
            long count = (Long) row[1];
            String name = buildingNames.getOrDefault(buildingId, "Unknown");
            usersPerBuilding.add(new DashboardStatsDTO.BuildingUserCount(buildingId.toString(), name, count));
        }

        // --- Heatmap Density Timeline (last 24h) ---
        List<DashboardStatsDTO.HeatmapDensityPoint> heatmapDensityTimeline = new ArrayList<>();
        LocalDateTime since24h = LocalDateTime.now().minusHours(24);
        try {
            for (Object[] row : heatmapHistoryRepository.findHourlyDensitySince(since24h)) {
                java.sql.Timestamp ts = (java.sql.Timestamp) row[0];
                long totalCount = ((Number) row[1]).longValue();
                String timeLabel = ts.toLocalDateTime().format(TIME_FMT);
                heatmapDensityTimeline.add(new DashboardStatsDTO.HeatmapDensityPoint(timeLabel, totalCount));
            }
        } catch (Exception e) {
            log.warn("Failed to load heatmap density timeline: {}", e.getMessage());
        }

        // --- Recent Events (10 newest) ---
        List<DashboardStatsDTO.RecentEvent> recentEvents = new ArrayList<>();
        var recentPage = eventRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        for (Event event : recentPage.getContent()) {
            String type = "UNKNOWN";
            if (event instanceof com.hustsimulator.context.entity.IndoorEvent) type = "INDOOR";
            else if (event instanceof com.hustsimulator.context.entity.OutdoorEvent) type = "OUTDOOR";
            recentEvents.add(new DashboardStatsDTO.RecentEvent(
                    event.getId().toString(),
                    event.getName(),
                    type,
                    event.getStatus().name(),
                    event.getStartTime().format(DATETIME_FMT)
            ));
        }

        // --- Events Timeline (SCHEDULED + ONGOING, sorted by startTime) ---
        List<DashboardStatsDTO.EventTimelineItem> eventsTimeline = new ArrayList<>();
        var timelineEvents = eventRepository.findByStatusIn(List.of(EventStatus.SCHEDULED, EventStatus.ONGOING));
        timelineEvents.sort(Comparator.comparing(Event::getStartTime));
        for (Event event : timelineEvents.stream().limit(20).toList()) {
            String type = "UNKNOWN";
            if (event instanceof com.hustsimulator.context.entity.IndoorEvent) type = "INDOOR";
            else if (event instanceof com.hustsimulator.context.entity.OutdoorEvent) type = "OUTDOOR";
            eventsTimeline.add(new DashboardStatsDTO.EventTimelineItem(
                    event.getId().toString(),
                    event.getName(),
                    type,
                    event.getStatus().name(),
                    event.getStartTime().format(DATETIME_FMT),
                    event.getEndTime().format(DATETIME_FMT),
                    event.getEstimatedParticipants() != null ? event.getEstimatedParticipants() : 0
            ));
        }

        // --- School Admin: Room Occupancy by Building ---
        List<DashboardStatsDTO.RoomOccupancy> roomOccupancyByBuilding = new ArrayList<>();
        Map<UUID, Map<RoomStatus, Long>> buildingRoomStatus = new HashMap<>();
        for (Object[] row : roomRepository.countRoomStatusByBuilding()) {
            UUID buildingId = (UUID) row[0];
            RoomStatus status = (RoomStatus) row[1];
            long count = (Long) row[2];
            buildingRoomStatus.computeIfAbsent(buildingId, k -> new HashMap<>()).put(status, count);
        }
        for (Map.Entry<UUID, Map<RoomStatus, Long>> entry : buildingRoomStatus.entrySet()) {
            String name = buildingNames.getOrDefault(entry.getKey(), "Unknown");
            long busyCount = entry.getValue().getOrDefault(RoomStatus.BUSY, 0L);
            long emptyCount = entry.getValue().getOrDefault(RoomStatus.EMPTY, 0L);
            roomOccupancyByBuilding.add(new DashboardStatsDTO.RoomOccupancy(name, busyCount, emptyCount));
        }

        // --- School Admin: Live Class Attendance ---
        List<DashboardStatsDTO.LiveClassAttendance> liveClassAttendance = new ArrayList<>();
        var ongoingClasses = recurringEventRepository.findByStatus(RecurringEventStatus.ONGOING);
        List<UUID> ongoingClassIds = ongoingClasses.stream().map(com.hustsimulator.context.entity.RecurringEvent::getId).toList();
        Map<UUID, Long> classAttendanceMap = new HashMap<>();
        if (!ongoingClassIds.isEmpty()) {
            for (Object[] row : userStateRepository.countUsersByEventIds(ongoingClassIds)) {
                classAttendanceMap.put((UUID) row[0], (Long) row[1]);
            }
        }
        for (var clazz : ongoingClasses.stream().limit(5).toList()) {
            long actual = classAttendanceMap.getOrDefault(clazz.getId(), 0L);
            String roomName = "Unknown";
            if (clazz.getRoomId() != null) {
                roomName = roomRepository.findById(clazz.getRoomId()).map(com.hustsimulator.context.entity.Room::getName).orElse("Unknown");
            }
            liveClassAttendance.add(new DashboardStatsDTO.LiveClassAttendance(clazz.getName(), roomName, actual, 50)); // default estimated 50
        }

        // --- School Admin: Today's Classes Status ---
        Map<String, Long> todayClassesStatus = new HashMap<>();
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        for (Object[] row : recurringEventDetailRepository.countStatusByScheduledAtBetween(startOfDay, endOfDay)) {
            todayClassesStatus.put(row[0].toString(), (Long) row[1]);
        }

        return new DashboardStatsDTO(
                totalBuildings, totalRooms, roomsBusy, roomsEmpty, roomsClosed,
                totalEvents, eventsOngoing,
                totalRecurringEvents, recurringEventsOngoing,
                userActivityDistribution,
                roomStatusDistribution,
                eventStatusDistribution,
                usersPerBuilding,
                heatmapDensityTimeline,
                recentEvents,
                eventsTimeline,
                roomOccupancyByBuilding,
                liveClassAttendance,
                todayClassesStatus
        );
    }
}
