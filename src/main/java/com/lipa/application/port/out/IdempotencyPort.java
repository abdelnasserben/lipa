package com.lipa.application.port.out;

import com.lipa.application.dto.IdempotentTransactionSnapshot;

import java.util.Optional;

/**
 * Generic idempotency lookup by idempotency key.
 */
public interface IdempotencyPort {

    Optional<IdempotentTransactionSnapshot> findByIdempotencyKey(String idempotencyKey);
}
