package com.lipa.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BackofficeTransactionItemDto(
        UUID id,
        String type,
        String status,
        BigDecimal amount,
        String currency,
        String idempotencyKey,
        String description,
        Instant createdAt
) {
}
