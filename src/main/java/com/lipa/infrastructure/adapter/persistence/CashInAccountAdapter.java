package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.AccountSnapshot;
import com.lipa.application.port.out.CashInAccountPort;
import com.lipa.infrastructure.persistence.jpa.repo.AccountJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CashInAccountAdapter implements CashInAccountPort {

    private final AccountJpaRepository repo;

    public CashInAccountAdapter(AccountJpaRepository repo) {
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
}
