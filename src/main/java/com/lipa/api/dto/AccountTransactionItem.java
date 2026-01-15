package com.lipa.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountTransactionItem(
        UUID transactionId,
        String type,
        String status,
        BigDecimal amount,
        String currency,
        String direction,      // DEBIT ou CREDIT (vu depuis CE compte)
        Instant createdAt,
        String description
) {}
