package com.lipa.api.dto;

import java.math.BigDecimal;

public record FeeConfigurationRequest(
        BigDecimal percentage,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String currency
) {}
