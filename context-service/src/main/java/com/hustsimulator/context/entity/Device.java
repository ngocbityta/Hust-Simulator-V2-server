package com.hustsimulator.context.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "devices", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "dev_token"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "dev_token")
    private String devToken;
}
