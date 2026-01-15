package com.lipa.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCashInRequest(
        @NotNull UUID clientAccountId,
        @NotNull UUID technicalAccountId,
        @NotNull BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotBlank @Size(max = 120) String idempotencyKey,
        @Size(max = 280) String description
) {}
