package com.hustsimulator.context.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "post_videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "post_id")
    private UUID postId;

    private String url;
    private String thumb;
}
