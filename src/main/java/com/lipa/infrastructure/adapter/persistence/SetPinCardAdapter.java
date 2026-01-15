package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.SetPinCardSnapshot;
import com.lipa.application.dto.SetPinCardUpdate;
import com.lipa.application.port.out.SetPinCardPort;
import com.lipa.infrastructure.persistence.jpa.repo.CardJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SetPinCardAdapter implements SetPinCardPort {

    private final CardJpaRepository repo;

    public SetPinCardAdapter(CardJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<SetPinCardSnapshot> findByUid(String uid) {
        return repo.findByUid(uid)
                .map(c -> new SetPinCardSnapshot(
                        c.getId(),
                        c.getUid(),
                        c.getStatus().name()
                ));
    }

    @Override
    public void applyUpdate(SetPinCardUpdate update) {
        var entity = repo.findById(update.cardId())
                .orElseThrow(() -> new IllegalStateException("Card not found id=" + update.cardId()));

        entity.setPinHash(update.pinHash());
        entity.setPinFailCount(update.pinFailCount());
        entity.setPinBlockedUntil(update.pinBlockedUntil());
        entity.setUpdatedAt(update.updatedAt());

        repo.save(entity);
    }
}
