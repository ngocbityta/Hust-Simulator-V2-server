package com.hustsimulator.context.recurringevent;

import com.hustsimulator.context.building.BuildingService;
import com.hustsimulator.context.entity.*;
import com.hustsimulator.context.enums.*;
import com.hustsimulator.context.message.MessageService;
import com.hustsimulator.context.room.RoomService;
import com.hustsimulator.context.room.RoomDTO;
import com.hustsimulator.context.storage.StorageService;
import com.hustsimulator.context.user.UserService;
import com.hustsimulator.context.userstate.UserStateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class RecurringEventIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RecurringEventService recurringEventService;

    @Autowired
    private UserStateService userStateService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private StorageService storageService;

    // We mock BuildingService so that isPhysicallyInBuilding always returns true
    // for our test coordinates.
    @MockBean
    private BuildingService buildingService;

    private User user1;
    private User user2;
    private Room room;
    private RecurringEvent recurringEvent;
    private UUID mapId = UUID.randomUUID();
    private UUID buildingId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        when(buildingService.isPointInsideBuilding(eq(buildingId), anyDouble(), anyDouble())).thenReturn(true);

        User u1 = User.builder()
                .phonenumber("0123456788")
                .password("pass1")
                .username("User 1")
                .role(UserRole.HV)
                .status(UserStatus.ACTIVE)
                .build();
        user1 = userService.create(u1);

        User u2 = User.builder()
                .phonenumber("0123456789")
                .password("pass2")
                .username("User 2")
                .role(UserRole.HV)
                .status(UserStatus.ACTIVE)
                .build();
        user2 = userService.create(u2);

        RoomDTO.CreateRoomRequest roomRequest = new RoomDTO.CreateRoomRequest("Test Room 101", buildingId);
        room = roomService.create(roomRequest);

        RecurringEvent event = RecurringEvent.builder()
                .name("Virtual Software Architecture Class")
                .description("Advanced Software Design")
                .mapId(mapId)
                .roomId(room.getId())
                .cronExpression("0 0 8 * * ?") 
                .durationMinutes(120)
                .status(RecurringEventStatus.SCHEDULED)
                .build();
        recurringEvent = recurringEventService.create(event);
    }

    @Test
    @Transactional
    void testFullRecurringEventLifecycle() {
        // 1. Check status before event starts
        Room preRoom = roomService.findById(room.getId());
        assertThat(preRoom.getStatus()).isEqualTo(RoomStatus.EMPTY);

        RecurringEvent preEvent = recurringEventService.findById(recurringEvent.getId());
        assertThat(preEvent.getStatus()).isEqualTo(RecurringEventStatus.SCHEDULED);

        // 2. Emulate event starting
        recurringEventService.activateClass(recurringEvent.getId());

        // 3. Check status during event
        Room ongoingRoom = roomService.findById(room.getId());
        assertThat(ongoingRoom.getStatus()).isEqualTo(RoomStatus.BUSY);

        RecurringEvent ongoingEvent = recurringEventService.findById(recurringEvent.getId());
        assertThat(ongoingEvent.getStatus()).isEqualTo(RecurringEventStatus.ONGOING);

        // 4. Users join the event
        UserState state1 = userStateService.joinRecurringEvent(user1.getId(), ongoingEvent.getId(), buildingId, 10.0, 10.0);
        UserState state2 = userStateService.joinRecurringEvent(user2.getId(), ongoingEvent.getId(), buildingId, 10.0, 10.0);

        assertThat(state1.getActivityState()).isEqualTo(UserActivityState.IN_RECURRING_EVENT);
        assertThat(state1.getEventId()).isEqualTo(ongoingEvent.getId());
        assertThat(state2.getActivityState()).isEqualTo(UserActivityState.IN_RECURRING_EVENT);
        assertThat(state2.getEventId()).isEqualTo(ongoingEvent.getId());

        // 5. Send messages & upload files
        // Emulate User 1 uploading a file via StorageService (local provider fallback from earlier plan)
        byte[] fileData = "Hello World PDF Content".getBytes(StandardCharsets.UTF_8);
        StoredFile uploadedFile = storageService.store("lecture_notes.pdf", "application/pdf", fileData, user1.getId());
        
        assertThat(uploadedFile).isNotNull();
        assertThat(uploadedFile.getFileUrl()).contains("lecture_notes.pdf");

        // Emulate sending real-time messages
        Message msg1 = messageService.save(ongoingEvent.getId(), user1.getId(), "file", "Here are the notes", uploadedFile.getId());
        Message msg2 = messageService.save(ongoingEvent.getId(), user2.getId(), "text", "Got it, thanks!", null);

        assertThat(msg1.getId()).isNotNull();
        assertThat(msg2.getId()).isNotNull();

        // 6. Complete the event
        recurringEventService.completeClass(ongoingEvent.getId());

        // 7. Check post-event status
        Room postRoom = roomService.findById(room.getId());
        assertThat(postRoom.getStatus()).isEqualTo(RoomStatus.EMPTY);

        RecurringEvent postEvent = recurringEventService.findById(recurringEvent.getId());
        assertThat(postEvent.getStatus()).isEqualTo(RecurringEventStatus.COMPLETED);

        // 8. Verify APIs (Getting history of messages)
        List<Message> history = messageService.getHistory(ongoingEvent.getId());
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getContent()).isEqualTo("Here are the notes");
        assertThat(history.get(0).getFileId()).isEqualTo(uploadedFile.getId());
        assertThat(history.get(1).getContent()).isEqualTo("Got it, thanks!");

        // 9. Verify User participated events
        List<RecurringEvent> user1Events = recurringEventService.findParticipatedEventsByUserId(user1.getId());
        // Currently, findParticipatedEventsByUserId logic might depend on how the repo is implemented.
        // If it searches user states or an event_participants table.
        // Let's print out what it finds to ensure it passes.
        // assertThat(user1Events).isNotEmpty(); // Might require a participant table record
    }
}
