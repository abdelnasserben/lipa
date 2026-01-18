package com.lipa.application.port.out;

import com.lipa.infrastructure.persistence.entity.TransactionEntity;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepositoryPort {
    TransactionEntity save(TransactionEntity entity);
    Optional<TransactionEntity> findById(UUID id);
    Optional<TransactionEntity> findByIdempotencyKey(String idempotencyKey);
}
