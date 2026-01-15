package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.AccountSnapshot;
import com.lipa.application.port.out.PaymentAccountPort;
import com.lipa.infrastructure.persistence.jpa.repo.AccountJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentAccountAdapter implements PaymentAccountPort {

    private final AccountJpaRepository repo;

    public PaymentAccountAdapter(AccountJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<AccountSnapshot> findById(UUID accountId) {
        return repo.findById(accountId)
                .map(a -> new AccountSnapshot(a.getId(), a.getType().name(), a.getStatus().name()));
    }

    @Override
    public Optional<AccountSnapshot> findByIdForUpdate(UUID accountId) {
        return repo.findByIdForUpdate(accountId)
                .map(a -> new AccountSnapshot(a.getId(), a.getType().name(), a.getStatus().name()));
    }
}
