package com.lipa.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntryItem(
        UUID ledgerEntryId,
        UUID transactionId,
        String direction,     // DEBIT | CREDIT
        BigDecimal amount,
        Instant createdAt
) {}
