package com.lipa.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetPinRequest(
        @NotBlank @Size(min = 4, max = 8) String pin,
        @Size(max = 120) String reason
) {}
