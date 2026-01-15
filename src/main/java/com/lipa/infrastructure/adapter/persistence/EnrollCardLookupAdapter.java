package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.EnrollCardLookupPort;
import com.lipa.infrastructure.persistence.jpa.repo.CardJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class EnrollCardLookupAdapter implements EnrollCardLookupPort {

    private final CardJpaRepository cardRepo;

    public EnrollCardLookupAdapter(CardJpaRepository cardRepo) {
        this.cardRepo = cardRepo;
    }

    @Override
    public boolean existsByUid(String uid) {
        return cardRepo.findByUid(uid).isPresent();
    }
}
