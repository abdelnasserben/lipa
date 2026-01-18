package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.IdempotentTransactionSnapshot;
import com.lipa.application.port.out.PaymentIdempotencyPort;
import com.lipa.infrastructure.persistence.repo.TransactionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentIdempotencyAdapter extends AbstractJpaIdempotencyAdapter implements PaymentIdempotencyPort {

    public PaymentIdempotencyAdapter(TransactionJpaRepository repo) {
        super(repo);
    }

    @Override
    public Optional<IdempotentTransactionSnapshot> findByIdempotencyKey(String idempotencyKey) {
        return findInternal(idempotencyKey);
    }
}
