package com.lipa.application.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface PaymentPersistencePort {

    PersistResult persist(PersistCommand command);

    record PersistCommand(
            UUID payerAccountId,
            UUID merchantAccountId,
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
