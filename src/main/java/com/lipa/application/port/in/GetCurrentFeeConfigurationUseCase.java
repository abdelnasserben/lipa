package com.lipa.application.port.in;

import java.math.BigDecimal;
import java.time.Instant;

public interface GetCurrentFeeConfigurationUseCase {

    Result get();

    record Result(
            BigDecimal percentage,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String currency,
            Instant updatedAt
    ) {}
}
