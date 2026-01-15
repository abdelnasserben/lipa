package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.CardPinSnapshot;
import com.lipa.application.dto.VerifyPinCardUpdate;
import com.lipa.application.port.out.VerifyPinCardPort;
import com.lipa.infrastructure.persistence.jpa.repo.CardJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VerifyPinCardAdapter implements VerifyPinCardPort {

    private final CardJpaRepository repo;

    public VerifyPinCardAdapter(CardJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<CardPinSnapshot> findByUid(String uid) {
        return repo.findByUid(uid)
                .map(c -> new CardPinSnapshot(
                        c.getId(),
                        c.getUid(),
                        c.getStatus().name(),
                        c.getPinHash(),
                        c.getPinFailCount(),
                        c.getPinBlockedUntil()
                ));
    }

    @Override
    public void applyUpdate(VerifyPinCardUpdate update) {
        var entity = repo.findById(update.cardId())
                .orElseThrow(() -> new IllegalStateException("Card not found id=" + update.cardId()));

        entity.setPinFailCount(update.pinFailCount());
        entity.setPinBlockedUntil(update.pinBlockedUntil());
        entity.setUpdatedAt(update.updatedAt());

        repo.save(entity);
    }
}
