package com.hustsimulator.context.dashboard;

import com.hustsimulator.context.common.DashboardConstants;
import com.hustsimulator.context.enums.TimeRangeFilter;
import com.hustsimulator.context.building.BuildingRepository;
import com.hustsimulator.context.campusnode.CampusNodeRepository;
import com.hustsimulator.context.entity.Building;
import com.hustsimulator.context.entity.CampusNode;
import com.hustsimulator.context.entity.Event;
import com.hustsimulator.context.enums.EventStatus;
import com.hustsimulator.context.enums.RecurringEventStatus;
import com.hustsimulator.context.enums.RoomStatus;
import com.hustsimulator.context.event.EventRepository;
import com.hustsimulator.context.heatmap.HeatmapHistoryRepository;
import com.hustsimulator.context.recurringevent.RecurringEventRepository;
import com.hustsimulator.context.room.RoomRepository;
import com.hustsimulator.context.userstate.UserStateRepository;
import com.hustsimulator.context.issue.FacilityIssueRepository;
import com.hustsimulator.context.enums.IssueStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private volatile DashboardStatsDTO cachedStats;
    private volatile long cachedStatsTime;
    private volatile String cachedStatsTimeRange;
    private static final long CACHE_TTL_MS = 30_000;

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final EventRepository eventRepository;
    private final RecurringEventRepository recurringEventRepository;
    private final com.hustsimulator.context.recurringeventdetail.RecurringEventDetailRepository recurringEventDetailRepository;
    private final UserStateRepository userStateRepository;
    private final HeatmapHistoryRepository heatmapHistoryRepository;
    private final CampusNodeRepository campusNodeRepository;
    private final FacilityIssueRepository facilityIssueRepository;

    @Value("${simulation.grid.meters-per-lat:111000.0}")
    private double metersPerLat;

    @Value("${simulation.grid.map-center-lat:21.003}")
    private double mapCenterLat;

    @Value("${simulation.grid.cell-size:50}")
    private int cellSize;

    @Value("#{'${simulation.dashboard.library-keywords:Thư viện,Thư Viện,Nhà T}'.split(',')}")
    private List<String> libraryKeywords;

    @Value("${simulation.dashboard.default-class-capacity:50}")
    private int defaultClassCapacity;

    @Override
    public DashboardStatsDTO getStats(String timeRange) {
        String effectiveTimeRange = (timeRange == null || timeRange.isBlank()) ? "1d" : timeRange;
        long now = System.currentTimeMillis();
        if (cachedStats != null && effectiveTimeRange.equals(cachedStatsTimeRange)
                && (now - cachedStatsTime) < CACHE_TTL_MS) {
            log.debug("Returning cached dashboard stats");
            return cachedStats;
        }

        TimeRangeFilter timeFilter = TimeRangeFilter.fromCode(effectiveTimeRange);
        var allRooms = roomRepository.findAll();
        long roomsBusy = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.BUSY).count();
        long roomsEmpty = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.EMPTY).count();
        long roomsClosed = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.CLOSED).count();
        long roomsWithIssues = facilityIssueRepository.countByStatus(IssueStatus.OPEN);

        Map<UUID, String> buildingNames = buildingRepository.findAll().stream()
                .collect(Collectors.toMap(Building::getId, Building::getName));

        DashboardStatsDTO stats = new DashboardStatsDTO(
                buildingRepository.count(),
                roomRepository.count(),
                roomsBusy, roomsEmpty, roomsClosed, roomsWithIssues,
                eventRepository.count(),
                eventRepository.findByStatus(EventStatus.ONGOING).size(),
                recurringEventRepository.count(),
                recurringEventRepository.findByStatus(RecurringEventStatus.ONGOING).size(),
                getBehaviorDistribution(buildingNames),
                Map.of(
                    DashboardConstants.ROOM_STATUS_EMPTY, roomsEmpty, 
                    DashboardConstants.ROOM_STATUS_BUSY, roomsBusy, 
                    DashboardConstants.ROOM_STATUS_CLOSED, roomsClosed
                ),
                getEventStatusDistribution(),
                getUsersPerBuilding(buildingNames),
                getHeatmapTimeline(timeFilter),
                getTopNodes(timeFilter),
                getEventsTimeline(),
                getRoomOccupancy(buildingNames),
                getLiveClassAttendance()
        );

        cachedStats = stats;
        cachedStatsTime = now;
        cachedStatsTimeRange = effectiveTimeRange;
        return stats;
    }

    private List<DashboardStatsDTO.BuildingUserCount> getUsersPerBuilding(Map<UUID, String> buildingNames) {
        List<DashboardStatsDTO.BuildingUserCount> usersPerBuilding = new ArrayList<>();
        for (Object[] row : userStateRepository.countUsersByBuilding()) {
            UUID buildingId = (UUID) row[0];
            long count = (Long) row[1];
            usersPerBuilding.add(new DashboardStatsDTO.BuildingUserCount(
                    buildingId.toString(), 
                    buildingNames.getOrDefault(buildingId, "Unknown"), 
                    count
            ));
        }
        return usersPerBuilding;
    }

    private List<DashboardStatsDTO.BehaviorDistribution> getBehaviorDistribution(Map<UUID, String> buildingNames) {
        long learningCount = 0;
        long eventCount = 0;
        long libraryCount = 0;
        long roamingCount = 0;
        long outsideCount = 0;

        for (Object[] row : userStateRepository.countByActivityState()) {
            com.hustsimulator.context.enums.UserActivityState state = com.hustsimulator.context.enums.UserActivityState.valueOf(row[0].toString());
            long count = (Long) row[1];

            switch (state) {
                case IN_EVENT:
                case SPECTATING_EVENT:
                    eventCount += count;
                    break;
                case IN_RECURRING_EVENT:
                case IN_ROOM:
                case SPECTATING_RECURRING_EVENT:
                    learningCount += count;
                    break;
                case OUTSIDE_MAP:
                    outsideCount += count;
                    break;
                case IN_BUILDING:
                case SPECTATING_BUILDING:
                case ROAMING:
                default:
                    roamingCount += count;
                    break;
            }
        }

        for (Object[] row : userStateRepository.countUsersByBuildingAndState()) {
            UUID buildingId = (UUID) row[0];
            com.hustsimulator.context.enums.UserActivityState state = (com.hustsimulator.context.enums.UserActivityState) row[1];
            long count = (Long) row[2];

            String name = buildingNames.getOrDefault(buildingId, "Unknown");
            if (libraryKeywords.stream().anyMatch(name::contains)) {
                libraryCount += count;
                switch (state) {
                    case IN_EVENT:
                    case SPECTATING_EVENT:
                        eventCount -= count;
                        break;
                    case IN_RECURRING_EVENT:
                    case IN_ROOM:
                    case SPECTATING_RECURRING_EVENT:
                        learningCount -= count;
                        break;
                    case OUTSIDE_MAP:
                        outsideCount -= count;
                        break;
                    case IN_BUILDING:
                    case SPECTATING_BUILDING:
                    case ROAMING:
                    default:
                        roamingCount -= count;
                        break;
                }
            }
        }

        return List.of(
                new DashboardStatsDTO.BehaviorDistribution(DashboardConstants.BEHAVIOR_LEARNING, Math.max(0, learningCount)),
                new DashboardStatsDTO.BehaviorDistribution(DashboardConstants.BEHAVIOR_EVENT, Math.max(0, eventCount)),
                new DashboardStatsDTO.BehaviorDistribution(DashboardConstants.BEHAVIOR_LIBRARY, libraryCount),
                new DashboardStatsDTO.BehaviorDistribution(DashboardConstants.BEHAVIOR_ROAMING, Math.max(0, roamingCount)),
                new DashboardStatsDTO.BehaviorDistribution(DashboardConstants.BEHAVIOR_OUTSIDE, Math.max(0, outsideCount))
        );
    }

    private Map<String, Long> getEventStatusDistribution() {
        Map<String, Long> eventStatusDistribution = new LinkedHashMap<>();
        for (EventStatus status : EventStatus.values()) {
            eventStatusDistribution.put(status.name(), eventRepository.countByStatus(status));
        }
        return eventStatusDistribution;
    }

    private List<DashboardStatsDTO.HeatmapDensityPoint> getHeatmapTimeline(TimeRangeFilter filter) {
        List<DashboardStatsDTO.HeatmapDensityPoint> heatmapDensityTimeline = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(filter.getFormat());
        LocalDateTime since = filter.getSince(LocalDateTime.now());
        try {
            List<Object[]> rows;
            switch (filter.getBucketType()) {
                case "day": rows = heatmapHistoryRepository.findDailyDensitySince(since); break;
                case "month": rows = heatmapHistoryRepository.findMonthlyDensitySince(since); break;
                case "hour":
                default: rows = heatmapHistoryRepository.findHourlyDensitySince(since); break;
            }
            for (Object[] row : rows) {
                java.sql.Timestamp ts = (java.sql.Timestamp) row[0];
                long totalCount = ((Number) row[1]).longValue();
                heatmapDensityTimeline.add(new DashboardStatsDTO.HeatmapDensityPoint(
                        ts.toLocalDateTime().format(fmt), totalCount));
            }
        } catch (Exception e) {
            log.warn("Failed to load heatmap density timeline: {}", e.getMessage());
        }
        return heatmapDensityTimeline;
    }

    private List<DashboardStatsDTO.TopNode> getTopNodes(TimeRangeFilter filter) {
        final double metersPerLng = metersPerLat * Math.cos(mapCenterLat * Math.PI / 180.0);
        List<DashboardStatsDTO.TopNode> topNodesList = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(filter.getFormat());
        LocalDateTime since = filter.getSince(LocalDateTime.now());

        List<Object[]> peakRows = heatmapHistoryRepository.findPeakDensitiesSince(since);
        Map<String, Object[]> peakMap = new HashMap<>();
        for (Object[] row : peakRows) {
            if (row != null && row[0] != null && row[1] != null) {
                Integer cx = ((Number) row[0]).intValue();
                Integer cy = ((Number) row[1]).intValue();
                peakMap.put(cx + "," + cy, row);
            }
        }

        var activeNodes = campusNodeRepository.findByIsActiveTrue();
        for (CampusNode node : activeNodes) {
            if (node.getNodeType() != null && node.getNodeType().equals("BUILDING")) continue;
            
            int cellX = (int) Math.floor((node.getLongitude() * metersPerLng) / cellSize);
            int cellY = (int) Math.floor((node.getLatitude() * metersPerLat) / cellSize);

            Object[] peakRow = peakMap.get(cellX + "," + cellY);
            if (peakRow != null && peakRow[2] != null && peakRow[3] != null) {
                java.sql.Timestamp ts = (java.sql.Timestamp) peakRow[2];
                long peakCount = ((Number) peakRow[3]).longValue();
                topNodesList.add(new DashboardStatsDTO.TopNode(
                        node.getName(), ts.toLocalDateTime().format(fmt), peakCount));
            } else {
                topNodesList.add(new DashboardStatsDTO.TopNode(node.getName(), "--:--", 0));
            }
        }

        topNodesList.sort((a, b) -> Long.compare(b.estimate(), a.estimate()));
        return topNodesList.stream().limit(5).toList();
    }

    private List<DashboardStatsDTO.EventTimelineItem> getEventsTimeline() {
        List<DashboardStatsDTO.EventTimelineItem> eventsTimeline = new ArrayList<>();
        var timelineEvents = eventRepository.findByStatusInAndEndTimeAfter(
            List.of(EventStatus.SCHEDULED, EventStatus.ONGOING), 
            LocalDateTime.now()
        );
        timelineEvents.sort(Comparator.comparing(Event::getStartTime));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DashboardConstants.TIME_FORMAT_DATETIME);
        
        for (Event event : timelineEvents.stream().limit(20).toList()) {
            String type = event.getEventType() != null ? event.getEventType().name() : "UNKNOWN";
            String buildingId = null;
            if (event instanceof com.hustsimulator.context.entity.IndoorEvent) {
                java.util.UUID bId = ((com.hustsimulator.context.entity.IndoorEvent) event).getBuildingId();
                if (bId != null) {
                    buildingId = bId.toString();
                }
            }

            eventsTimeline.add(new DashboardStatsDTO.EventTimelineItem(
                    event.getId().toString(),
                    event.getName(),
                    type,
                    event.getStatus().name(),
                    event.getStartTime().format(fmt),
                    event.getEndTime().format(fmt),
                    event.getEstimatedParticipants() != null ? event.getEstimatedParticipants() : 0,
                    buildingId
            ));
        }
        return eventsTimeline;
    }

    private List<DashboardStatsDTO.RoomOccupancy> getRoomOccupancy(Map<UUID, String> buildingNames) {
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
            
            // Filter out non-lecture buildings
            if (name.contains("Công trình phụ") || name.contains("Ký túc xá") || 
                name.contains("Trạm") || name.contains("Nhà ăn") || 
                name.contains("Căng tin") || name.contains("Nhà vệ sinh") ||
                name.contains("Khán đài")) {
                continue;
            }
            
            long busyCount = entry.getValue().getOrDefault(RoomStatus.BUSY, 0L);
            long emptyCount = entry.getValue().getOrDefault(RoomStatus.EMPTY, 0L);
            roomOccupancyByBuilding.add(new DashboardStatsDTO.RoomOccupancy(name, busyCount, emptyCount));
        }
        
        // Sort by busy count (descending), then by total count (descending)
        roomOccupancyByBuilding.sort((a, b) -> {
            int cmp = Long.compare(b.busyCount(), a.busyCount());
            if (cmp == 0) {
                return Long.compare(b.busyCount() + b.emptyCount(), a.busyCount() + a.emptyCount());
            }
            return cmp;
        });
        
        return roomOccupancyByBuilding.stream().limit(15).toList();
    }

    private List<DashboardStatsDTO.LiveClassAttendance> getLiveClassAttendance() {
        List<DashboardStatsDTO.LiveClassAttendance> liveClassAttendance = new ArrayList<>();
        var ongoingClasses = recurringEventRepository.findByStatus(RecurringEventStatus.ONGOING);
        List<UUID> ongoingClassIds = ongoingClasses.stream().map(com.hustsimulator.context.entity.RecurringEvent::getId).toList();
        
        Map<UUID, Long> classAttendanceMap = new HashMap<>();
        if (!ongoingClassIds.isEmpty()) {
            for (Object[] row : userStateRepository.countUsersByEventIds(ongoingClassIds)) {
                classAttendanceMap.put((UUID) row[0], (Long) row[1]);
            }
        }
        
        List<UUID> roomIds = ongoingClasses.stream().limit(5)
                .map(com.hustsimulator.context.entity.RecurringEvent::getRoomId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, String> roomNames = new HashMap<>();
        if (!roomIds.isEmpty()) {
            for (var room : roomRepository.findAllById(roomIds)) {
                roomNames.put(room.getId(), room.getName());
            }
        }

        for (var clazz : ongoingClasses.stream().limit(5).toList()) {
            long actual = classAttendanceMap.getOrDefault(clazz.getId(), 0L);
            String roomName = clazz.getRoomId() != null 
                    ? roomNames.getOrDefault(clazz.getRoomId(), "Unknown") 
                    : "Unknown";
            
            liveClassAttendance.add(new DashboardStatsDTO.LiveClassAttendance(
                    clazz.getName(), roomName, actual, defaultClassCapacity));
        }
        return liveClassAttendance;
    }
}
