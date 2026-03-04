package com.hustsimulator.context.block;

import com.hustsimulator.context.entity.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;

    public List<Block> findAll() {
        return blockRepository.findAll();
    }

    public List<Block> findByBlockerId(UUID blockerId) {
        return blockRepository.findByIdBlockerId(blockerId);
    }

    public Block create(UUID blockerId, UUID blockedId) {
        Block block = Block.builder()
                .id(new BlockId(blockerId, blockedId))
                .build();
        return blockRepository.save(block);
    }

    public void delete(UUID blockerId, UUID blockedId) {
        blockRepository.deleteById(new BlockId(blockerId, blockedId));
    }
}
