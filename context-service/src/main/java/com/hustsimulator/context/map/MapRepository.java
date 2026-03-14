package com.hustsimulator.context.map;

import com.hustsimulator.context.entity.Map;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MapRepository extends JpaRepository<Map, UUID> {
    List<Map> findByIsActiveTrue();
}
