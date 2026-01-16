package com.lipa.application.port.out;

import java.math.BigDecimal;
import java.time.Instant;

public interface FeeConfigurationAdminPort {

    void updateActive(
            BigDecimal percentage,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String currency,
            Instant updatedAt
    );
}
