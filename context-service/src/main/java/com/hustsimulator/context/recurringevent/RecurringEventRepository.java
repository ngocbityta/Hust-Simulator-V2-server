package com.hustsimulator.context.recurringevent;

import com.hustsimulator.context.entity.RecurringEvent;
import com.hustsimulator.context.enums.RecurringEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RecurringEventRepository extends JpaRepository<RecurringEvent, UUID> {
    List<RecurringEvent> findByMapId(UUID mapId);
    List<RecurringEvent> findByStatusIn(Collection<RecurringEventStatus> statuses);
    List<RecurringEvent> findByStatus(RecurringEventStatus status);

    /**
     * Lấy recurring events theo danh sách ID.
     * Dùng sau khi context-service nhận event IDs từ messaging-service qua HTTP API.
     * Không còn query cross-service vào bảng messages_chat nữa.
     */
    List<RecurringEvent> findByIdIn(@Param("ids") Collection<UUID> ids);
}
