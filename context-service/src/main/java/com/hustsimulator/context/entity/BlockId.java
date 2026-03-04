package com.hustsimulator.context.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockId implements Serializable {

    @Column(name = "blocker_id")
    private UUID blockerId;

    @Column(name = "blocked_id")
    private UUID blockedId;
}
