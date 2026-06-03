package com.hustsimulator.social.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Post post;

    @Column(name = "url")
    private String url;

    @Column(name = "thumb")
    private String thumb;
}
