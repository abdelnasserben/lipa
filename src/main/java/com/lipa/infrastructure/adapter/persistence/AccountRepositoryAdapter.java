package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.AccountRepositoryPort;
import com.lipa.infrastructure.persistence.jpa.entity.AccountEntity;
import com.lipa.infrastructure.persistence.jpa.repo.AccountJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final AccountJpaRepository repo;

    public AccountRepositoryAdapter(AccountJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public AccountEntity save(AccountEntity entity) {
        return repo.save(entity);
    }

    @Override
    public Optional<AccountEntity> findById(UUID id) {
        return repo.findById(id);
    }

    @Override
    public Optional<AccountEntity> findByIdForUpdate(UUID id) {
        return repo.findByIdForUpdate(id);
    }
}
