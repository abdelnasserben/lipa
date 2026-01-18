package com.lipa.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnrollCardRequest(
        @NotBlank @Size(max = 80) String cardUid,
        @NotBlank @Size(max = 120) String displayName,
        @NotBlank @Size(max = 32) String phone
) {}
