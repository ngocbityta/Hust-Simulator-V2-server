package com.hustsimulator.context.campusnode;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/campus-nodes")
@RequiredArgsConstructor
public class CampusNodeController {

    private final CampusNodeService nodeService;

    @GetMapping
    public ResponseEntity<List<CampusNodeDTO.NodeResponse>> getNodes(
            @RequestParam(required = false) String type) {
        if (type != null) {
            return ResponseEntity.ok(nodeService.getNodesByType(type.toUpperCase()));
        }
        return ResponseEntity.ok(nodeService.getAllNodes());
    }

    @PostMapping
    public ResponseEntity<CampusNodeDTO.NodeResponse> createNode(
            @RequestBody CampusNodeDTO.CreateNodeRequest request) {
        return ResponseEntity.ok(nodeService.createNode(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNode(@PathVariable UUID id) {
        nodeService.deleteNode(id);
        return ResponseEntity.noContent().build();
    }
}
