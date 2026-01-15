package com.lipa.application.port.out;

import com.lipa.application.dto.IdempotentTransactionSnapshot;

import java.util.Optional;

/**
 * Lookup of an existing transaction by idempotency key for the cash-in use case.
 */
public interface CashInIdempotencyPort {

    Optional<IdempotentTransactionSnapshot> findByIdempotencyKey(String idempotencyKey);
}
