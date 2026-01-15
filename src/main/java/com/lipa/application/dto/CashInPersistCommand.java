package com.lipa.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Command sent from the application use case to the persistence adapter.
 */
public record CashInPersistCommand(
        UUID clientAccountId,
        UUID technicalAccountId,
        BigDecimal amount,
        String currency,
        String idempotencyKey,
        String description,
        Instant createdAt
) {
}
