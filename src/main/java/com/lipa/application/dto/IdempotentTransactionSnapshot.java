package com.lipa.application.dto;

import java.util.UUID;

/**
 * Minimal transaction view returned when an idempotency key already exists.
 */
public record IdempotentTransactionSnapshot(
        UUID transactionId,
        String status
) {
}
