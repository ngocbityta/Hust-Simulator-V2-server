package com.hustsimulator.context.campusgraph;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/campus-graph")
@RequiredArgsConstructor
public class CampusGraphController {

    private final CampusGraphService campusGraphService;

    @GetMapping
    public ResponseEntity<CampusGraphDTO.GraphResponse> getFullGraph() {
        return ResponseEntity.ok(campusGraphService.getFullGraph());
    }

    @GetMapping("/full")
    public ResponseEntity<CampusGraphDTO.FullGraphResponse> getFullGraphWithBuildings() {
        return ResponseEntity.ok(campusGraphService.getFullGraphWithBuildings());
    }

    @GetMapping("/nodes")
    public ResponseEntity<List<CampusGraphDTO.NodeResponse>> getNodesByType(
            @RequestParam(required = false) String type) {
        if (type != null) {
            return ResponseEntity.ok(campusGraphService.getNodesByType(type.toUpperCase()));
        }
        return ResponseEntity.ok(campusGraphService.getFullGraph().nodes());
    }

    @PostMapping("/nodes")
    public ResponseEntity<CampusGraphDTO.NodeResponse> createNode(
            @RequestBody CampusGraphDTO.CreateNodeRequest request) {
        return ResponseEntity.ok(campusGraphService.createNode(request));
    }

    @DeleteMapping("/nodes/{id}")
    public ResponseEntity<Void> deleteNode(@PathVariable UUID id) {
        campusGraphService.deleteNode(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/edges")
    public ResponseEntity<CampusGraphDTO.EdgeResponse> createEdge(
            @RequestBody CampusGraphDTO.CreateEdgeRequest request) {
        return ResponseEntity.ok(campusGraphService.createEdge(request));
    }

    @DeleteMapping("/edges/{id}")
    public ResponseEntity<Void> deleteEdge(@PathVariable UUID id) {
        campusGraphService.deleteEdge(id);
        return ResponseEntity.noContent().build();
    }
}
