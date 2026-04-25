package com.hustsimulator.context.recurringeventdetail;

import com.hustsimulator.context.entity.RecurringEventDetail;
import com.hustsimulator.context.enums.RecurringEventDetailStatus;
import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringEventDetailServiceImpl implements RecurringEventDetailService {

    private final RecurringEventDetailRepository repository;

    @Override
    @Transactional
    public RecurringEventDetail getOrCreate(UUID recurringEventId, LocalDateTime scheduledAt) {
        return repository.findByRecurringEventIdAndScheduledAt(recurringEventId, scheduledAt)
                .orElseGet(() -> {
                    log.info("Creating new detail for recurring event {} at {}", recurringEventId, scheduledAt);
                    RecurringEventDetail newDetail = RecurringEventDetail.builder()
                            .recurringEventId(recurringEventId)
                            .scheduledAt(scheduledAt)
                            .status(RecurringEventDetailStatus.SCHEDULED)
                            .build();
                    return repository.save(newDetail);
                });
    }

    @Override
    @Transactional
    public RecurringEventDetail activate(UUID detailId) {
        RecurringEventDetail detail = findById(detailId);
        detail.setStatus(RecurringEventDetailStatus.ONGOING);
        log.info("Activated detail {}", detailId);
        return repository.save(detail);
    }

    @Override
    @Transactional
    public RecurringEventDetail complete(UUID detailId) {
        RecurringEventDetail detail = findById(detailId);
        detail.setStatus(RecurringEventDetailStatus.COMPLETED);
        detail.setEndedAt(LocalDateTime.now());
        log.info("Completed detail {}", detailId);
        return repository.save(detail);
    }

    @Override
    public Optional<RecurringEventDetail> findCurrent(UUID recurringEventId) {
        return repository.findFirstByRecurringEventIdAndStatus(recurringEventId, RecurringEventDetailStatus.ONGOING);
    }

    @Override
    public List<RecurringEventDetail> findAll(UUID recurringEventId) {
        return repository.findByRecurringEventIdOrderByScheduledAtDesc(recurringEventId);
    }

    @Override
    public RecurringEventDetail findById(UUID detailId) {
        return repository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringEventDetail", detailId));
    }
}
