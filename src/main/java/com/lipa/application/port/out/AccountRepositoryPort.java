package com.lipa.application.port.out;

import com.lipa.infrastructure.persistence.jpa.entity.AccountEntity;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {
    AccountEntity save(AccountEntity entity);
    Optional<AccountEntity> findById(UUID id);
    Optional<AccountEntity> findByIdForUpdate(UUID id); // verrouille la ligne account (pour éviter 2 paiements concurrents)
}
