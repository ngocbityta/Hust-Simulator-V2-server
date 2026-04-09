package com.hustsimulator.context.recurringevent;

import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.RecurringEvent;
import com.hustsimulator.context.entity.Room;
import com.hustsimulator.context.enums.RecurringEventStatus;
import com.hustsimulator.context.enums.RoomStatus;
import com.hustsimulator.context.room.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringEventServiceTest {

    @Mock private RecurringEventRepository recurringEventRepository;
    @Mock private RoomRepository roomRepository;

    private RecurringEventServiceImpl recurringEventService;

    @BeforeEach
    void setUp() {
        recurringEventService = new RecurringEventServiceImpl(recurringEventRepository, roomRepository);
    }

    @Test
    void activateClass_shouldSetOngoingAndBusyRoom() {
        UUID classId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        
        RecurringEvent event = RecurringEvent.builder()
                .name("Calculus 1")
                .roomId(roomId)
                .status(RecurringEventStatus.SCHEDULED)
                .build();
        event.setId(classId);

        Room room = Room.builder().name("Room 101").status(RoomStatus.EMPTY).build();
        room.setId(roomId);

        when(recurringEventRepository.findById(classId)).thenReturn(Optional.of(event));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        recurringEventService.activateClass(classId);

        assertThat(event.getStatus()).isEqualTo(RecurringEventStatus.ONGOING);
        assertThat(room.getStatus()).isEqualTo(RoomStatus.BUSY);
        
        verify(recurringEventRepository).save(event);
        verify(roomRepository).save(room);
    }

    @Test
    void completeClass_shouldSetCompletedAndEmptyRoom() {
        UUID classId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();

        RecurringEvent event = RecurringEvent.builder()
                .name("Physics 2")
                .roomId(roomId)
                .status(RecurringEventStatus.ONGOING)
                .build();
        event.setId(classId);

        Room room = Room.builder().name("Room 202").status(RoomStatus.BUSY).build();
        room.setId(roomId);

        when(recurringEventRepository.findById(classId)).thenReturn(Optional.of(event));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        recurringEventService.completeClass(classId);

        assertThat(event.getStatus()).isEqualTo(RecurringEventStatus.COMPLETED);
        assertThat(room.getStatus()).isEqualTo(RoomStatus.EMPTY);
    }

    @Test
    void activateClass_shouldThrow_whenEventNotFound() {
        UUID id = UUID.randomUUID();
        when(recurringEventRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recurringEventService.activateClass(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
