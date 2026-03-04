package com.hustsimulator.context.report;

import com.hustsimulator.context.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByUserId(UUID userId);

    List<Report> findByPostId(UUID postId);
}
