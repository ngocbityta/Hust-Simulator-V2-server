package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.hustsimulator.context.enums.UserRole;
import com.hustsimulator.context.enums.UserStatus;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @NotBlank
    @Column(unique = true, nullable = false)
    private String phonenumber;

    @NotBlank
    @Column(nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;

    private String username;
    private String avatar;

    @Column(name = "cover_image")
    private String coverImage;

    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private UserRole role;

    @com.fasterxml.jackson.annotation.JsonIgnore
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean online = false;

    // PostGIS geometry columns omitted — use native queries for spatial ops
}
