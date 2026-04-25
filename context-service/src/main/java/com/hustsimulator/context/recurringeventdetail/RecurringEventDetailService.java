package com.hustsimulator.context.recurringeventdetail;

import com.hustsimulator.context.entity.RecurringEventDetail;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringEventDetailService {

    /** Tạo hoặc lấy lại detail cho một lần diễn ra cụ thể của recurring event. */
    RecurringEventDetail getOrCreate(UUID recurringEventId, LocalDateTime scheduledAt);

    /** Chuyển detail sang ONGOING và trả về detail đã cập nhật. */
    RecurringEventDetail activate(UUID detailId);

    /** Chuyển detail sang COMPLETED và ghi endedAt. */
    RecurringEventDetail complete(UUID detailId);

    /** Lấy detail đang ONGOING của recurring event (nếu có). */
    Optional<RecurringEventDetail> findCurrent(UUID recurringEventId);

    /** Lịch sử tất cả detail của recurring event, mới nhất trước. */
    List<RecurringEventDetail> findAll(UUID recurringEventId);

    /** Lấy detail theo ID. */
    RecurringEventDetail findById(UUID detailId);
}
