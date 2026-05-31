package com.hustsimulator.context.campusway;

import com.hustsimulator.context.entity.CampusWay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CampusWayRepository extends JpaRepository<CampusWay, UUID> {
    List<CampusWay> findByIsActiveTrue();
    List<CampusWay> findByWayType(String wayType);
}
