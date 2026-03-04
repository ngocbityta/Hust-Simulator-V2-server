package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "conversations", uniqueConstraints = @UniqueConstraint(columnNames = {"partner_a_id", "partner_b_id"}))
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation extends BaseEntity {

    @Column(name = "partner_a_id")
    private UUID partnerAId;

    @Column(name = "partner_b_id")
    private UUID partnerBId;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
