package com.hustsimulator.social.journey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hustsimulator.social.common.GeometryUtils;
import com.hustsimulator.social.common.ResourceNotFoundException;
import com.hustsimulator.social.context.ContextServiceClient;
import com.hustsimulator.social.entity.Journey;
import com.hustsimulator.social.entity.JourneyItem;
import com.hustsimulator.social.enums.JourneyStatus;
import com.hustsimulator.social.location.UserLocation;
import com.hustsimulator.social.location.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JourneyServiceImpl implements JourneyService {

    private final JourneyRepository journeyRepository;
    private final JourneyItemRepository journeyItemRepository;
    private final UserLocationRepository userLocationRepository;
    private final ContextServiceClient contextServiceClient;
    private final ObjectMapper objectMapper;

    private static final double STOP_DISTANCE_THRESHOLD_METERS = 30.0;
    private static final long STOP_TIME_THRESHOLD_MINUTES = 5;

    @Override
    public JourneyDTO.JourneyResponse generateJourneyDraft(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<UserLocation> locations = userLocationRepository.findByUserIdAndTimestampBetweenOrderByTimestampAsc(userId, startTime, endTime);

        List<JourneyDTO.JourneyItemResponse> itemResponses = detectStops(locations);
        List<JourneyDTO.LocationPoint> pathCoordinates = new ArrayList<>();

        if (locations != null && !locations.isEmpty()) {
            UserLocation firstLoc = locations.get(0);
            UserLocation lastLoc = locations.get(locations.size() - 1);

            // Create start item if not covered
            if (itemResponses.isEmpty() || !itemResponses.get(0).startTime().equals(firstLoc.getTimestamp())) {
                ContextServiceClient.BuildingDTO b = contextServiceClient.getNearestBuilding(firstLoc.getLatitude(), firstLoc.getLongitude(), STOP_DISTANCE_THRESHOLD_METERS);
                String content = b != null ? "Khởi hành từ " + b.getName() : "Khởi hành";
                itemResponses.add(0, new JourneyDTO.JourneyItemResponse(
                        null, b != null ? b.getId() : null, null, content, firstLoc.getTimestamp(), 0, "{}",
                        firstLoc.getLatitude(), firstLoc.getLongitude(), firstLoc.getTimestamp(), firstLoc.getTimestamp(),
                        null, null
                ));
            } else if (!itemResponses.isEmpty() && itemResponses.get(0).startTime().equals(firstLoc.getTimestamp())) {
                // Rename first item to indicate start if it was detected as a stop
                JourneyDTO.JourneyItemResponse first = itemResponses.get(0);
                String content = first.content().replace("Ghé thăm", "Khởi hành từ").replace("Dừng chân", "Khởi hành");
                itemResponses.set(0, new JourneyDTO.JourneyItemResponse(
                        first.id(), first.referenceId(), first.mediaUrl(), content, first.timestamp(), first.sortOrder(),
                        first.metadata(), first.latitude(), first.longitude(), first.startTime(), first.endTime(),
                        first.eventId(), first.postIds()
                ));
            }

            // Create end item if not covered
            if (itemResponses.get(itemResponses.size() - 1).endTime().isBefore(lastLoc.getTimestamp())) {
                ContextServiceClient.BuildingDTO b = contextServiceClient.getNearestBuilding(lastLoc.getLatitude(), lastLoc.getLongitude(), STOP_DISTANCE_THRESHOLD_METERS);
                String content = b != null ? "Điểm đến " + b.getName() : "Điểm kết thúc";
                itemResponses.add(new JourneyDTO.JourneyItemResponse(
                        null, b != null ? b.getId() : null, null, content, lastLoc.getTimestamp(), 0, "{}",
                        lastLoc.getLatitude(), lastLoc.getLongitude(), lastLoc.getTimestamp(), lastLoc.getTimestamp(),
                        null, null
                ));
            } else if (itemResponses.size() > 1 && itemResponses.get(itemResponses.size() - 1).endTime().equals(lastLoc.getTimestamp())) {
                // Rename last item to indicate end
                JourneyDTO.JourneyItemResponse last = itemResponses.get(itemResponses.size() - 1);
                String content = last.content().replace("Ghé thăm", "Điểm đến ").replace("Dừng chân", "Điểm kết thúc");
                itemResponses.set(itemResponses.size() - 1, new JourneyDTO.JourneyItemResponse(
                        last.id(), last.referenceId(), last.mediaUrl(), content, last.timestamp(), last.sortOrder(),
                        last.metadata(), last.latitude(), last.longitude(), last.startTime(), last.endTime(),
                        last.eventId(), last.postIds()
                ));
            }

            // Fix sort orders
            for (int i = 0; i < itemResponses.size(); i++) {
                JourneyDTO.JourneyItemResponse old = itemResponses.get(i);
                itemResponses.set(i, new JourneyDTO.JourneyItemResponse(
                        old.id(), old.referenceId(), old.mediaUrl(), old.content(), old.timestamp(), i,
                        old.metadata(), old.latitude(), old.longitude(), old.startTime(), old.endTime(),
                        old.eventId(), old.postIds()
                ));
            }

            // Populate path coordinates: MUST start with first item's coords and end with last item's coords
            JourneyDTO.JourneyItemResponse firstItem = itemResponses.get(0);
            JourneyDTO.JourneyItemResponse lastItem = itemResponses.get(itemResponses.size() - 1);

            pathCoordinates.add(new JourneyDTO.LocationPoint(firstItem.latitude(), firstItem.longitude(), firstItem.timestamp()));
            
            for (UserLocation loc : locations) {
                if (loc.getTimestamp().isAfter(firstItem.timestamp()) && loc.getTimestamp().isBefore(lastItem.timestamp())) {
                    pathCoordinates.add(new JourneyDTO.LocationPoint(loc.getLatitude(), loc.getLongitude(), loc.getTimestamp()));
                }
            }
            
            if (itemResponses.size() > 1) {
                pathCoordinates.add(new JourneyDTO.LocationPoint(lastItem.latitude(), lastItem.longitude(), lastItem.timestamp()));
            }
        }

        LocalDate journeyDate = startTime.toLocalDate();

        return new JourneyDTO.JourneyResponse(
                null,
                userId,
                "Hành trình ngày " + journeyDate.toString(),
                "Tự động tổng hợp từ lịch sử di chuyển.",
                journeyDate,
                null,
                null,
                "DEFAULT",
                JourneyStatus.DRAFT,
                null,
                itemResponses,
                pathCoordinates,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private List<JourneyDTO.JourneyItemResponse> detectStops(List<UserLocation> locations) {
        List<JourneyDTO.JourneyItemResponse> stops = new ArrayList<>();
        if (locations == null || locations.isEmpty()) {
            return stops;
        }

        int sortOrder = 0;
        int i = 0;
        int n = locations.size();

        while (i < n) {
            int j = i + 1;
            while (j < n) {
                double distance = GeometryUtils.calculateDistance(
                        locations.get(i).getLatitude(), locations.get(i).getLongitude(),
                        locations.get(j).getLatitude(), locations.get(j).getLongitude()
                );
                if (distance > STOP_DISTANCE_THRESHOLD_METERS) {
                    break;
                }
                j++;
            }

            long durationMinutes = java.time.Duration.between(locations.get(i).getTimestamp(), locations.get(j - 1).getTimestamp()).toMinutes();
            
            if (durationMinutes >= STOP_TIME_THRESHOLD_MINUTES) {
                double sumLat = 0;
                double sumLng = 0;
                for (int k = i; k < j; k++) {
                    sumLat += locations.get(k).getLatitude();
                    sumLng += locations.get(k).getLongitude();
                }
                double meanLat = sumLat / (j - i);
                double meanLng = sumLng / (j - i);

                ContextServiceClient.BuildingDTO building = contextServiceClient.getNearestBuilding(meanLat, meanLng, STOP_DISTANCE_THRESHOLD_METERS);
                UUID referenceId = building != null ? building.getId() : null;
                String content = building != null ? "Ghé thăm " + building.getName() : "Dừng chân";

                stops.add(new JourneyDTO.JourneyItemResponse(
                        null,
                        referenceId,
                        null,
                        content,
                        locations.get(i).getTimestamp(),
                        sortOrder++,
                        "{}",
                        meanLat,
                        meanLng,
                        locations.get(i).getTimestamp(),
                        locations.get(j - 1).getTimestamp(),
                        null,
                        null
                ));
                i = j;
            } else {
                i++;
            }
        }

        return stops;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JourneyDTO.JourneyResponse> getUserJourneys(UUID userId, Pageable pageable) {
        return journeyRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public JourneyDTO.JourneyResponse getJourneyById(UUID id, UUID userId) {
        Journey journey = journeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journey", id));
        
        if (!journey.getUserId().equals(userId) && journey.getStatus() == JourneyStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot view other user's draft journey");
        }

        return mapToResponse(journey);
    }

    @Override
    @Transactional
    public JourneyDTO.JourneyResponse createJourney(JourneyDTO.CreateJourneyRequest request, UUID userId) {
        Journey journey = Journey.builder()
                .userId(userId)
                .title(request.title())
                .description(request.description())
                .journeyDate(request.journeyDate())
                .videoUrl(request.videoUrl())
                .musicUrl(request.musicUrl())
                .templateId(request.templateId())
                .visibility(request.visibility())
                .status(JourneyStatus.DRAFT)
                .build();

        if (request.pathCoordinates() != null) {
            journey.setPathCoordinates(serializePath(request.pathCoordinates()));
        }

        Journey savedJourney = journeyRepository.save(journey);

        if (request.items() != null && !request.items().isEmpty()) {
            List<JourneyItem> items = request.items().stream()
                    .map(itemReq -> JourneyItem.builder()
                            .journey(savedJourney)
                            .referenceId(itemReq.referenceId())
                            .mediaUrl(itemReq.mediaUrl())
                            .content(itemReq.content())
                            .timestamp(itemReq.timestamp())
                            .sortOrder(itemReq.sortOrder())
                            .metadata(itemReq.metadata() != null ? itemReq.metadata() : "{}")
                            .latitude(itemReq.latitude())
                            .longitude(itemReq.longitude())
                            .startTime(itemReq.startTime())
                            .endTime(itemReq.endTime())
                            .eventId(itemReq.eventId())
                            .postIds(itemReq.postIds())
                            .build())
                    .collect(Collectors.toList());
            journeyItemRepository.saveAll(items);
            savedJourney.setItems(items);
        }

        return mapToResponse(savedJourney);
    }

    @Override
    @Transactional
    public JourneyDTO.JourneyResponse updateJourney(UUID id, JourneyDTO.UpdateJourneyRequest request, UUID userId) {
        Journey journey = journeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journey", id));

        if (!journey.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to update this journey");
        }

        if (request.title() != null) journey.setTitle(request.title());
        if (request.description() != null) journey.setDescription(request.description());
        if (request.videoUrl() != null) journey.setVideoUrl(request.videoUrl());
        if (request.musicUrl() != null) journey.setMusicUrl(request.musicUrl());
        if (request.templateId() != null) journey.setTemplateId(request.templateId());
        if (request.status() != null) journey.setStatus(request.status());
        if (request.visibility() != null) journey.setVisibility(request.visibility());
        if (request.pathCoordinates() != null) journey.setPathCoordinates(serializePath(request.pathCoordinates()));

        if (request.items() != null) {
            journeyItemRepository.deleteAll(journey.getItems());
            journey.getItems().clear();

            List<JourneyItem> newItems = request.items().stream()
                    .map(itemReq -> JourneyItem.builder()
                            .journey(journey)
                            .referenceId(itemReq.referenceId())
                            .mediaUrl(itemReq.mediaUrl())
                            .content(itemReq.content())
                            .timestamp(itemReq.timestamp())
                            .sortOrder(itemReq.sortOrder())
                            .metadata(itemReq.metadata() != null ? itemReq.metadata() : "{}")
                            .latitude(itemReq.latitude())
                            .longitude(itemReq.longitude())
                            .startTime(itemReq.startTime())
                            .endTime(itemReq.endTime())
                            .eventId(itemReq.eventId())
                            .postIds(itemReq.postIds())
                            .build())
                    .collect(Collectors.toList());
            
            journey.getItems().addAll(newItems);
        }

        Journey updatedJourney = journeyRepository.save(journey);
        return mapToResponse(updatedJourney);
    }

    @Override
    @Transactional
    public JourneyDTO.JourneyResponse publishJourney(UUID id, UUID userId) {
        Journey journey = journeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journey", id));

        if (!journey.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to publish this journey");
        }

        journey.setStatus(JourneyStatus.PUBLISHED);
        Journey updatedJourney = journeyRepository.save(journey);
        
        log.info("Journey {} published for user {}", id, userId);

        return mapToResponse(updatedJourney);
    }

    @Override
    @Transactional
    public void deleteJourney(UUID id, UUID userId) {
        Journey journey = journeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journey", id));

        if (!journey.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to delete this journey");
        }

        journeyRepository.delete(journey);
        log.info("Journey {} deleted by user {}", id, userId);
    }

    private JourneyDTO.JourneyResponse mapToResponse(Journey journey) {
        List<JourneyDTO.JourneyItemResponse> itemResponses = journey.getItems().stream()
                .map(item -> new JourneyDTO.JourneyItemResponse(
                        item.getId(),
                        item.getReferenceId(),
                        item.getMediaUrl(),
                        item.getContent(),
                        item.getTimestamp(),
                        item.getSortOrder(),
                        item.getMetadata(),
                        item.getLatitude(),
                        item.getLongitude(),
                        item.getStartTime(),
                        item.getEndTime(),
                        item.getEventId(),
                        item.getPostIds()
                ))
                .collect(Collectors.toList());

        List<JourneyDTO.LocationPoint> path = deserializePath(journey.getPathCoordinates());

        return new JourneyDTO.JourneyResponse(
                journey.getId(),
                journey.getUserId(),
                journey.getTitle(),
                journey.getDescription(),
                journey.getJourneyDate(),
                journey.getVideoUrl(),
                journey.getMusicUrl(),
                journey.getTemplateId(),
                journey.getStatus(),
                journey.getVisibility(),
                itemResponses,
                path,
                journey.getCreatedAt(),
                journey.getUpdatedAt()
        );
    }

    private String serializePath(List<JourneyDTO.LocationPoint> path) {
        if (path == null) return null;
        try {
            return objectMapper.writeValueAsString(path);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize pathCoordinates", e);
            return null;
        }
    }

    private List<JourneyDTO.LocationPoint> deserializePath(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<JourneyDTO.LocationPoint>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize pathCoordinates", e);
            return null;
        }
    }
}
