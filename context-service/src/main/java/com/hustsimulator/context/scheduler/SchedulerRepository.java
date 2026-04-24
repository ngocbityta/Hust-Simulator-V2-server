package com.hustsimulator.context.scheduler;

import com.hustsimulator.context.entity.ScheduledJob;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hustsimulator.context.enums.JobType;
import com.hustsimulator.context.enums.JobStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchedulerRepository extends JpaRepository<ScheduledJob, UUID> {

    Optional<ScheduledJob> findByJobIdAndJobTypeAndTargetTime(String jobId, JobType jobType, LocalDateTime targetTime);

    List<ScheduledJob> findByStatusAndTargetTimeBefore(JobStatus status, LocalDateTime time);
}
