package com.lipa.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FeeConfigurationSnapshot(
        BigDecimal percentage,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String currency,
        Instant updatedAt
) {}
