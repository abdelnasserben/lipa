package com.lipa.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record FeeConfigurationRequest(
        @NotNull @PositiveOrZero BigDecimal percentage,
        @NotNull @Positive BigDecimal minAmount,
        @NotNull @Positive BigDecimal maxAmount,
        @NotBlank @Size(min = 3, max = 3) String currency
) {
}
