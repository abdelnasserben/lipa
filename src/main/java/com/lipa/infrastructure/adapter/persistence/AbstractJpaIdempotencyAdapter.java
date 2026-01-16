package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.IdempotentTransactionSnapshot;
import com.lipa.infrastructure.persistence.jpa.repo.TransactionJpaRepository;

import java.util.Optional;

abstract class AbstractJpaIdempotencyAdapter {

    private final TransactionJpaRepository repo;

    protected AbstractJpaIdempotencyAdapter(TransactionJpaRepository repo) {
        this.repo = repo;
    }

    protected Optional<IdempotentTransactionSnapshot> findInternal(String idempotencyKey) {
        return repo.findByIdempotencyKey(idempotencyKey)
                .map(t -> new IdempotentTransactionSnapshot(t.getId(), t.getStatus().name()));
    }
}
