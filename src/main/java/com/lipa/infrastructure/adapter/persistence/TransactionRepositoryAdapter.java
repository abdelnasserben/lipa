package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.TransactionRepositoryPort;
import com.lipa.infrastructure.persistence.jpa.entity.TransactionEntity;
import com.lipa.infrastructure.persistence.jpa.repo.TransactionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final TransactionJpaRepository repo;

    public TransactionRepositoryAdapter(TransactionJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public TransactionEntity save(TransactionEntity entity) {
        return repo.save(entity);
    }

    @Override
    public Optional<TransactionEntity> findById(UUID id) {
        return repo.findById(id);
    }

    @Override
    public Optional<TransactionEntity> findByIdempotencyKey(String idempotencyKey) {
        return repo.findByIdempotencyKey(idempotencyKey);
    }
}
