package com.hustsimulator.context.campus;

import com.hustsimulator.context.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CampusZoneRepository extends JpaRepository<CampusZone, UUID> {

    List<CampusZone> findByType(String type);

    List<CampusZone> findByIsActiveTrue();
}
