package com.hustsimulator.context.block;

import com.hustsimulator.context.entity.Block;
import com.hustsimulator.context.entity.BlockId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockServiceTest {

    @Mock
    private BlockRepository blockRepository;

    @InjectMocks
    private BlockService blockService;

    @Test
    void findAll_shouldReturnAllBlocks() {
        UUID blockerId = UUID.randomUUID();
        UUID blockedId = UUID.randomUUID();
        Block block = Block.builder().id(new BlockId(blockerId, blockedId)).build();
        when(blockRepository.findAll()).thenReturn(List.of(block));

        List<Block> result = blockService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void create_shouldSaveBlock() {
        UUID blockerId = UUID.randomUUID();
        UUID blockedId = UUID.randomUUID();
        Block block = Block.builder().id(new BlockId(blockerId, blockedId)).build();
        when(blockRepository.save(any(Block.class))).thenReturn(block);

        Block result = blockService.create(blockerId, blockedId);

        assertThat(result.getId().getBlockerId()).isEqualTo(blockerId);
        verify(blockRepository).save(any(Block.class));
    }

    @Test
    void delete_shouldCallDeleteById() {
        UUID blockerId = UUID.randomUUID();
        UUID blockedId = UUID.randomUUID();

        blockService.delete(blockerId, blockedId);

        verify(blockRepository).deleteById(new BlockId(blockerId, blockedId));
    }
}
