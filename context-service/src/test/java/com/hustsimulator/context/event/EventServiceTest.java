package com.hustsimulator.context.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hustsimulator.context.building.BuildingRepository;
import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.Building;
import com.hustsimulator.context.entity.Event;
import com.hustsimulator.context.entity.IndoorEvent;
import com.hustsimulator.context.entity.OutdoorEvent;
import com.hustsimulator.context.enums.EventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private BuildingRepository buildingRepository;

    private EventServiceImpl eventService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        eventService = new EventServiceImpl(eventRepository, buildingRepository, objectMapper);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private IndoorEvent buildIndoorEvent() {
        UUID mapId = UUID.randomUUID();
        UUID buildingId = UUID.randomUUID();
        IndoorEvent event = new IndoorEvent(buildingId, List.of(UUID.randomUUID()));
        event.setName("Seminar D1");
        event.setMapId(mapId);
        event.setStatus(EventStatus.SCHEDULED);
        event.setStartTime(LocalDateTime.now().plusHours(1));
        event.setEndTime(LocalDateTime.now().plusHours(3));
        return event;
    }

    private OutdoorEvent buildOutdoorEvent(UUID mapId) {
        OutdoorEvent event = new OutdoorEvent(100.0, 100.0, 200.0, 200.0);
        event.setName("Outdoor Festival");
        event.setMapId(mapId);
        event.setStatus(EventStatus.SCHEDULED);
        event.setStartTime(LocalDateTime.now().plusHours(1));
        event.setEndTime(LocalDateTime.now().plusHours(4));
        return event;
    }

    // ─── findAll ─────────────────────────────────────────────────────────────────

    @Test
    void findAll_shouldReturnAllEvents() {
        when(eventRepository.findAll()).thenReturn(List.of(buildIndoorEvent()));

        List<Event> result = eventService.findAll();

        assertThat(result).hasSize(1);
        verify(eventRepository).findAll();
    }

    // ─── findActiveEvents ─────────────────────────────────────────────────────────

    @Test
    void findActiveEvents_shouldReturnScheduledAndOngoing() {
        IndoorEvent e = buildIndoorEvent();
        when(eventRepository.findByStatusIn(List.of(EventStatus.SCHEDULED, EventStatus.ONGOING)))
                .thenReturn(List.of(e));

        List<Event> result = eventService.findActiveEvents();

        assertThat(result).hasSize(1);
        verify(eventRepository).findByStatusIn(List.of(EventStatus.SCHEDULED, EventStatus.ONGOING));
    }

    // ─── findById ─────────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnEvent_whenExists() {
        UUID id = UUID.randomUUID();
        IndoorEvent event = buildIndoorEvent();
        event.setId(id);

        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        Event result = eventService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Seminar D1");
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── findByMapId ──────────────────────────────────────────────────────────────

    @Test
    void findByMapId_shouldDelegate_toRepository() {
        UUID mapId = UUID.randomUUID();
        when(eventRepository.findByMapId(mapId)).thenReturn(List.of());

        List<Event> result = eventService.findByMapId(mapId);

        assertThat(result).isEmpty();
        verify(eventRepository).findByMapId(mapId);
    }

    // ─── create: IndoorEvent ──────────────────────────────────────────────────────

    @Nested
    class CreateIndoorEvent {

        @Test
        void create_indoor_shouldSaveAndReturn() {
            IndoorEvent event = buildIndoorEvent();
            when(eventRepository.save(any(IndoorEvent.class))).thenAnswer(i -> i.getArgument(0));

            Event result = eventService.create(event);

            assertThat(result).isInstanceOf(IndoorEvent.class);
            assertThat(result.getName()).isEqualTo("Seminar D1");
            assertThat(result.getStatus()).isEqualTo(EventStatus.SCHEDULED);
            verify(eventRepository).save(event);
        }

        @Test
        void create_indoor_shouldNotCheckBuildingOverlap() {
            IndoorEvent event = buildIndoorEvent();
            when(eventRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            eventService.create(event);

            // BuildingRepository should never be called for INDOOR events
            verifyNoInteractions(buildingRepository);
        }
    }

    // ─── create: OutdoorEvent ──────────────────────────────────────────────────────

    @Nested
    class CreateOutdoorEvent {

        @Test
        void create_outdoor_shouldSave_whenNoBuildingOverlap() throws Exception {
            UUID mapId = UUID.randomUUID();
            OutdoorEvent event = buildOutdoorEvent(mapId);

            // Building polygon far from event bounding box [100,100 → 200,200]
            String coords = "[[0,0],[10,0],[10,10],[0,10]]";
            Building building = Building.builder().name("D1").mapId(mapId).coordinates(coords).build();

            when(buildingRepository.findByMapId(mapId)).thenReturn(List.of(building));
            when(eventRepository.save(any(OutdoorEvent.class))).thenAnswer(i -> i.getArgument(0));

            Event result = eventService.create(event);

            assertThat(result).isInstanceOf(OutdoorEvent.class);
            assertThat(result.getName()).isEqualTo("Outdoor Festival");
            verify(eventRepository).save(event);
        }

        @Test
        void create_outdoor_shouldThrow_whenOverlapsBuilding() throws Exception {
            UUID mapId = UUID.randomUUID();
            // Event bounding box: x[100,200] y[100,200]
            OutdoorEvent event = buildOutdoorEvent(mapId);

            // Building that overlaps with the event bbox — centered at (150,150)
            String coords = "[[140,140],[160,140],[160,160],[140,160]]";
            Building building = Building.builder().name("D-Overlap").mapId(mapId).coordinates(coords).build();

            when(buildingRepository.findByMapId(mapId)).thenReturn(List.of(building));

            assertThatThrownBy(() -> eventService.create(event))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("overlaps with building");

            verify(eventRepository, never()).save(any());
        }

        @Test
        void create_outdoor_shouldThrow_whenBoundingBoxMissing() {
            UUID mapId = UUID.randomUUID();
            OutdoorEvent event = new OutdoorEvent(null, 100.0, 200.0, 200.0); // minX is null
            event.setName("Bad Event");
            event.setMapId(mapId);
            event.setStatus(EventStatus.SCHEDULED);
            event.setStartTime(LocalDateTime.now().plusHours(1));
            event.setEndTime(LocalDateTime.now().plusHours(2));

            assertThatThrownBy(() -> eventService.create(event))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("bounding box coordinates");

            verify(eventRepository, never()).save(any());
        }

        @Test
        void create_outdoor_shouldSucceed_whenNoBuildings() {
            UUID mapId = UUID.randomUUID();
            OutdoorEvent event = buildOutdoorEvent(mapId);

            when(buildingRepository.findByMapId(mapId)).thenReturn(List.of());
            when(eventRepository.save(any(OutdoorEvent.class))).thenAnswer(i -> i.getArgument(0));

            Event result = eventService.create(event);

            assertThat(result).isInstanceOf(OutdoorEvent.class);
        }
    }

    // ─── update ───────────────────────────────────────────────────────────────────

    @Nested
    class UpdateEvent {

        @Test
        void update_indoor_shouldUpdateFields() {
            UUID id = UUID.randomUUID();
            IndoorEvent stored = buildIndoorEvent();
            stored.setId(id);

            UUID newBuildingId = UUID.randomUUID();
            UUID newRoomId = UUID.randomUUID();
            IndoorEvent details = new IndoorEvent(newBuildingId, List.of(newRoomId));
            details.setName("Updated Seminar");
            details.setMapId(stored.getMapId());
            details.setStatus(EventStatus.ONGOING);
            details.setStartTime(stored.getStartTime());
            details.setEndTime(stored.getEndTime());

            when(eventRepository.findById(id)).thenReturn(Optional.of(stored));
            when(eventRepository.save(any(IndoorEvent.class))).thenAnswer(i -> i.getArgument(0));

            Event result = eventService.update(id, details);

            assertThat(result.getName()).isEqualTo("Updated Seminar");
            assertThat(result.getStatus()).isEqualTo(EventStatus.ONGOING);
            assertThat(((IndoorEvent) result).getBuildingId()).isEqualTo(newBuildingId);
        }

        @Test
        void update_outdoor_shouldUpdateBoundingBox() {
            UUID id = UUID.randomUUID();
            UUID mapId = UUID.randomUUID();
            OutdoorEvent stored = buildOutdoorEvent(mapId);
            stored.setId(id);

            OutdoorEvent details = new OutdoorEvent(500.0, 500.0, 600.0, 600.0);
            details.setName("Relocated Festival");
            details.setMapId(mapId);
            details.setStatus(EventStatus.SCHEDULED);
            details.setStartTime(stored.getStartTime());
            details.setEndTime(stored.getEndTime());

            when(eventRepository.findById(id)).thenReturn(Optional.of(stored));
            when(buildingRepository.findByMapId(mapId)).thenReturn(List.of());
            when(eventRepository.save(any(OutdoorEvent.class))).thenAnswer(i -> i.getArgument(0));

            Event result = eventService.update(id, details);

            OutdoorEvent updatedOutdoor = (OutdoorEvent) result;
            assertThat(updatedOutdoor.getMinX()).isEqualTo(500.0);
            assertThat(updatedOutdoor.getName()).isEqualTo("Relocated Festival");
        }

        @Test
        void update_shouldThrow_whenChangingEventType() {
            UUID id = UUID.randomUUID();
            IndoorEvent stored = buildIndoorEvent();
            stored.setId(id);

            UUID mapId = UUID.randomUUID();
            OutdoorEvent details = buildOutdoorEvent(mapId); // DIFFERENT type

            when(eventRepository.findById(id)).thenReturn(Optional.of(stored));

            assertThatThrownBy(() -> eventService.update(id, details))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot change event type");
        }
    }

    // ─── delete ───────────────────────────────────────────────────────────────────

    @Test
    void delete_shouldCallRepositoryDelete() {
        UUID id = UUID.randomUUID();
        IndoorEvent event = buildIndoorEvent();
        event.setId(id);

        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        eventService.delete(id);

        verify(eventRepository).delete(event);
    }

    @Test
    void delete_shouldThrow_whenEventNotFound() {
        UUID id = UUID.randomUUID();
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(eventRepository, never()).delete(any());
    }
}
