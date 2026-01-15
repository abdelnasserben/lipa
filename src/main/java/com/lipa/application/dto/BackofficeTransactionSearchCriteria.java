package com.lipa.application.dto;

import java.time.Instant;
import java.util.UUID;

public record BackofficeTransactionSearchCriteria(
        UUID accountId,
        String type,
        String status,
        String idempotencyKey,
        Instant from,
        Instant to
) {
}
