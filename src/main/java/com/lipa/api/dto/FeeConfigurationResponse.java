package com.lipa.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FeeConfigurationResponse(
        BigDecimal percentage,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String currency,
        Instant updatedAt
) {}
