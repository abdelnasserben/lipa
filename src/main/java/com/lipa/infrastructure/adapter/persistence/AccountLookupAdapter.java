package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.AccountLookupPort;
import com.lipa.infrastructure.persistence.jpa.repo.AccountJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountLookupAdapter implements AccountLookupPort {

    private final AccountJpaRepository repo;

    public AccountLookupAdapter(AccountJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean existsById(UUID accountId) {
        return repo.existsById(accountId);
    }
}
