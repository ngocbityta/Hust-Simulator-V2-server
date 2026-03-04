package com.hustsimulator.context.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Block {

    @EmbeddedId
    private BlockId id;
}
