package com.hustsimulator.context.dashboard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record DashboardStatsDTO(
    // Summary counts
    long totalBuildings,
    long totalRooms,
    long roomsBusy,
    long roomsEmpty,
    long roomsClosed,
    long totalEvents,
    long eventsOngoing,
    long totalRecurringEvents,
    long recurringEventsOngoing,
    // Activity distribution directly mapped for UI
    List<BehaviorDistribution> studentBehaviorDistribution,
    // Room utilization (key = RoomStatus name, value = count)
    Map<String, Long> roomStatusDistribution,
    // Event status breakdown (key = EventStatus name, value = count)
    Map<String, Long> eventStatusDistribution,
    // Users per building
    List<BuildingUserCount> usersPerBuilding,
    // Heatmap density over time (hourly, last 24h)
    List<HeatmapDensityPoint> heatmapDensityTimeline,
    // Top 5 busiest nodes
    List<TopNode> topNodes,
    // Upcoming/ongoing events timeline
    List<EventTimelineItem> eventsTimeline,
    // School Admin Specific Data
    List<RoomOccupancy> roomOccupancyByBuilding,
    List<LiveClassAttendance> liveClassAttendance
) {
    public record BuildingUserCount(String buildingId, String buildingName, long userCount) {}
    public record HeatmapDensityPoint(String time, long totalCount) {}
    public record EventTimelineItem(String id, String name, String type, String status, String startTime, String endTime, int estimatedParticipants) {}
    
    // New Records for School Admin
    public record RoomOccupancy(String buildingName, long busyCount, long emptyCount) {}
    public record LiveClassAttendance(String className, String roomName, long actualParticipants, int estimatedParticipants) {}
    public record BehaviorDistribution(String name, long value) {}
    public record TopNode(String name, String time, long estimate) {}
}
