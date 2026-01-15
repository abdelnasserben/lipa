package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.CardSnapshot;
import com.lipa.application.port.out.PaymentCardPort;
import com.lipa.infrastructure.persistence.jpa.repo.CardJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentCardAdapter implements PaymentCardPort {

    private final CardJpaRepository repo;

    public PaymentCardAdapter(CardJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<CardSnapshot> findByUid(String uid) {
        return repo.findByUid(uid)
                .map(c -> new CardSnapshot(
                        c.getId(),
                        c.getUid(),
                        c.getStatus().name(),
                        c.getAccount() == null ? null : c.getAccount().getId()
                ));
    }
}
