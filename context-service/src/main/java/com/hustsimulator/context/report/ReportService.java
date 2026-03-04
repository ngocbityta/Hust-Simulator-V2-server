package com.hustsimulator.context.report;

import com.hustsimulator.context.entity.*;

import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    public Report findById(UUID id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", id));
    }

    public List<Report> findByPostId(UUID postId) {
        return reportRepository.findByPostId(postId);
    }

    public Report create(Report report) {
        return reportRepository.save(report);
    }

    public void delete(UUID id) {
        if (!reportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Report", id);
        }
        reportRepository.deleteById(id);
    }
}
