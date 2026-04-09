package com.hustsimulator.context.scheduler;

import com.hustsimulator.context.entity.ScheduledJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchedulerRepository extends JpaRepository<ScheduledJob, UUID> {

    Optional<ScheduledJob> findByJobIdAndJobTypeAndTargetTime(String jobId, String jobType, LocalDateTime targetTime);

    List<ScheduledJob> findByStatusAndTargetTimeBefore(String status, LocalDateTime time);
}
