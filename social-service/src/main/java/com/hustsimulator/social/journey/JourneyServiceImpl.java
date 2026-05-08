package com.hustsimulator.social.journey;

import com.hustsimulator.social.common.ResourceNotFoundException;
import com.hustsimulator.social.entity.Journey;
import com.hustsimulator.social.entity.JourneyItem;
import com.hustsimulator.social.entity.Post;
import com.hustsimulator.social.enums.JourneyItemType;
import com.hustsimulator.social.enums.JourneyStatus;
import com.hustsimulator.social.post.PostRepository;
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
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JourneyServiceImpl implements JourneyService {

    private final JourneyRepository journeyRepository;
    private final JourneyItemRepository journeyItemRepository;
    private final PostRepository postRepository;

    @Override
    public JourneyDTO.JourneyResponse getPreviewForToday(UUID userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Post> todaysPosts = postRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtAsc(userId, startOfDay, endOfDay);

        List<JourneyDTO.JourneyItemResponse> itemResponses = todaysPosts.stream()
                .map(post -> {
                    JourneyItemType type = determineType(post);
                    return new JourneyDTO.JourneyItemResponse(
                            null, // Not saved yet
                            type,
                            post.getId(),
                            post.getVideoUrl(), // Can be used for mediaUrl
                            post.getContent(),
                            post.getCreatedAt(),
                            0, // Sort order will be assigned dynamically or by client
                            "{}"
                    );
                })
                .collect(Collectors.toList());

        // Assign mock sort orders for preview
        for (int i = 0; i < itemResponses.size(); i++) {
            JourneyDTO.JourneyItemResponse item = itemResponses.get(i);
            itemResponses.set(i, new JourneyDTO.JourneyItemResponse(
                    item.id(), item.type(), item.referenceId(), item.mediaUrl(),
                    item.content(), item.timestamp(), i, item.metadata()
            ));
        }

        return new JourneyDTO.JourneyResponse(
                null,
                userId,
                "Hành trình ngày " + today.toString(),
                "Tự động tổng hợp từ các hoạt động trong ngày.",
                today,
                null,
                null,
                "DEFAULT",
                JourneyStatus.DRAFT,
                null,
                itemResponses,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
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
        
        // Simple privacy check for now - can only view own draft/private or anyone's public
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
                .musicUrl(request.musicUrl())
                .templateId(request.templateId())
                .visibility(request.visibility())
                .status(JourneyStatus.DRAFT)
                .build();

        Journey savedJourney = journeyRepository.save(journey);

        if (request.items() != null && !request.items().isEmpty()) {
            List<JourneyItem> items = request.items().stream()
                    .map(itemReq -> JourneyItem.builder()
                            .journey(savedJourney)
                            .type(itemReq.type())
                            .referenceId(itemReq.referenceId())
                            .mediaUrl(itemReq.mediaUrl())
                            .content(itemReq.content())
                            .timestamp(itemReq.timestamp())
                            .sortOrder(itemReq.sortOrder())
                            .metadata(itemReq.metadata() != null ? itemReq.metadata() : "{}")
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
        if (request.musicUrl() != null) journey.setMusicUrl(request.musicUrl());
        if (request.templateId() != null) journey.setTemplateId(request.templateId());
        if (request.visibility() != null) journey.setVisibility(request.visibility());

        // For simplicity, if items are provided, we replace all existing items
        if (request.items() != null) {
            journeyItemRepository.deleteAll(journey.getItems());
            journey.getItems().clear();

            List<JourneyItem> newItems = request.items().stream()
                    .map(itemReq -> JourneyItem.builder()
                            .journey(journey)
                            .type(itemReq.type())
                            .referenceId(itemReq.referenceId())
                            .mediaUrl(itemReq.mediaUrl())
                            .content(itemReq.content())
                            .timestamp(itemReq.timestamp())
                            .sortOrder(itemReq.sortOrder())
                            .metadata(itemReq.metadata() != null ? itemReq.metadata() : "{}")
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
        
        // Here we could trigger a RabbitMQ event to render the video asynchronously
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

    private JourneyItemType determineType(Post post) {
        if (post.getRoomId() != null) {
            return JourneyItemType.CLASS;
        } else if (post.getEventId() != null) {
            return JourneyItemType.EVENT;
        } else {
            return JourneyItemType.CHECKIN;
        }
    }

    private JourneyDTO.JourneyResponse mapToResponse(Journey journey) {
        List<JourneyDTO.JourneyItemResponse> itemResponses = journey.getItems().stream()
                .map(item -> new JourneyDTO.JourneyItemResponse(
                        item.getId(),
                        item.getType(),
                        item.getReferenceId(),
                        item.getMediaUrl(),
                        item.getContent(),
                        item.getTimestamp(),
                        item.getSortOrder(),
                        item.getMetadata()
                ))
                .collect(Collectors.toList());

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
                journey.getCreatedAt(),
                journey.getUpdatedAt()
        );
    }
}
