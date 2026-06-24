package com.hustsimulator.auth.entity;

import com.hustsimulator.auth.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.hustsimulator.auth.enums.UserStatus;
import lombok.*;

@Entity
@Table(name = "users", schema = "auth")
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
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String username;
    
    @Column(name = "full_name")
    private String fullName;
    
    private String avatar;

    @Column(name = "cover_image")
    private String coverImage;

    private String description;

    @com.fasterxml.jackson.annotation.JsonIgnore
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean online = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private com.hustsimulator.auth.enums.Role role = com.hustsimulator.auth.enums.Role.USER;
}
