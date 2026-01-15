package com.lipa.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnrollCardRequest(
        @NotBlank @Size(max = 80) String cardUid,
        @Size(max = 120) String displayName,
        @Size(max = 32) String phone
) {}
