package com.lipa.application.port.out;

import com.lipa.application.dto.IdempotentTransactionSnapshot;

import java.util.Optional;

public interface PaymentIdempotencyPort {

    Optional<IdempotentTransactionSnapshot> findByIdempotencyKey(String idempotencyKey);
}
