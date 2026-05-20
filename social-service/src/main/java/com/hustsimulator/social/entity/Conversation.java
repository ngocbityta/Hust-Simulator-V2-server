package com.hustsimulator.social.entity;

import com.hustsimulator.social.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "conversations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"partner_a_id", "partner_b_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation extends BaseEntity {

    @Column(name = "partner_a_id", nullable = false)
    private UUID partnerAId;

    @Column(name = "partner_b_id", nullable = false)
    private UUID partnerBId;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
