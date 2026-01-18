package com.lipa.application.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Persists a cash-in operation (transaction + ledger entries + audit) in the underlying storage.
 */
public interface CashInPersistencePort {

    PersistResult persist(PersistCommand command);

    record PersistCommand(
            UUID clientAccountId,
            UUID technicalAccountId,
            BigDecimal amount,
            String currency,
            String idempotencyKey,
            String description,
            Instant createdAt
    ) {}

    record PersistResult(
            UUID transactionId,
            String status
    ) {}
}
