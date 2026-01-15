package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.IdempotentTransactionSnapshot;
import com.lipa.application.port.out.CashInIdempotencyPort;
import com.lipa.infrastructure.persistence.jpa.repo.TransactionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CashInIdempotencyAdapter implements CashInIdempotencyPort {

    private final TransactionJpaRepository repo;

    public CashInIdempotencyAdapter(TransactionJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<IdempotentTransactionSnapshot> findByIdempotencyKey(String idempotencyKey) {
        return repo.findByIdempotencyKey(idempotencyKey)
                .map(t -> new IdempotentTransactionSnapshot(t.getId(), t.getStatus().name()));
    }
}
