package com.lipa.application.port.in;

import java.math.BigDecimal;
import java.time.Instant;

public interface UpdateFeeConfigurationUseCase {

    Result update(Command command);

    record Command(
            BigDecimal percentage,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String currency
    ) {}

    record Result(
            BigDecimal percentage,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String currency,
            Instant updatedAt
    ) {}
}
