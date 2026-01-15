package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.PaymentLedgerPort;
import com.lipa.infrastructure.persistence.jpa.repo.LedgerEntryJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentLedgerAdapter implements PaymentLedgerPort {

    private final LedgerEntryJpaRepository repo;

    public PaymentLedgerAdapter(LedgerEntryJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public BigDecimal sumCredits(UUID accountId) {
        return repo.sumCredits(accountId);
    }

    @Override
    public BigDecimal sumDebits(UUID accountId) {
        return repo.sumDebits(accountId);
    }
}
