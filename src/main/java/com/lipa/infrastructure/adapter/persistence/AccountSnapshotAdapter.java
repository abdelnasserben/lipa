package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.AccountSnapshot;
import com.lipa.application.port.out.AccountSnapshotPort;
import com.lipa.infrastructure.persistence.repo.AccountJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AccountSnapshotAdapter implements AccountSnapshotPort {

    private final AccountJpaRepository repo;

    public AccountSnapshotAdapter(AccountJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<AccountSnapshot> findById(UUID accountId) {
        return repo.findById(accountId)
                .map(a -> new AccountSnapshot(
                        a.getId(),
                        a.getType().name(),
                        a.getStatus().name()
                ));
    }

    @Override
    public Optional<AccountSnapshot> findByIdForUpdate(UUID accountId) {
        return repo.findByIdForUpdate(accountId)
                .map(a -> new AccountSnapshot(
                        a.getId(),
                        a.getType().name(),
                        a.getStatus().name()
                ));
    }
}
