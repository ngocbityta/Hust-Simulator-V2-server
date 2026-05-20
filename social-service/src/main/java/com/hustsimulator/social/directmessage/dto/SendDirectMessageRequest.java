package com.hustsimulator.social.directmessage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendDirectMessageRequest {
    @NotBlank
    private String content;
}
