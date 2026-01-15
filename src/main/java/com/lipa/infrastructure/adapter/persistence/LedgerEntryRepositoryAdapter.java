package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.LedgerEntryRepositoryPort;
import com.lipa.infrastructure.persistence.jpa.entity.LedgerEntryEntity;
import com.lipa.infrastructure.persistence.jpa.repo.LedgerEntryJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class LedgerEntryRepositoryAdapter implements LedgerEntryRepositoryPort {

    private final LedgerEntryJpaRepository repo;

    public LedgerEntryRepositoryAdapter(LedgerEntryJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public LedgerEntryEntity save(LedgerEntryEntity entity) {
        return repo.save(entity);
    }
}
