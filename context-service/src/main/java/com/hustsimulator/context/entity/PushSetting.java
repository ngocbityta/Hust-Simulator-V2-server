package com.hustsimulator.context.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "push_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushSetting {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Builder.Default private Boolean likeComment = true;
    @Builder.Default private Boolean fromFriends = true;
    @Builder.Default private Boolean requestedFriend = true;
    @Builder.Default private Boolean suggestedFriend = true;
    @Builder.Default private Boolean birthday = true;
    @Builder.Default private Boolean video = true;
    @Builder.Default private Boolean report = true;
    @Builder.Default private Boolean soundOn = true;
    @Builder.Default private Boolean notificationOn = true;
    @Builder.Default private Boolean vibrantOn = true;
    @Builder.Default private Boolean ledOn = true;
}
