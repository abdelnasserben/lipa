package com.lipa.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BackofficeTransactionItem(
        UUID id,
        String type,
        String status,
        BigDecimal amount,
        String currency,
        String idempotencyKey,
        String description,
        Instant createdAt
) {}
