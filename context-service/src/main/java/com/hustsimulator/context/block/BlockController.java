package com.hustsimulator.context.block;

import com.hustsimulator.context.entity.*;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @GetMapping
    public List<Block> findAll() {
        return blockService.findAll();
    }

    @GetMapping("/blocker/{blockerId}")
    public List<Block> findByBlockerId(@PathVariable UUID blockerId) {
        return blockService.findByBlockerId(blockerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Block create(@RequestParam UUID blockerId, @RequestParam UUID blockedId) {
        return blockService.create(blockerId, blockedId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam UUID blockerId, @RequestParam UUID blockedId) {
        blockService.delete(blockerId, blockedId);
    }
}
