package com.hustsimulator.context.block;

import com.hustsimulator.context.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BlockRepository extends JpaRepository<Block, BlockId> {

    List<Block> findByIdBlockerId(UUID blockerId);

    List<Block> findByIdBlockedId(UUID blockedId);
}
