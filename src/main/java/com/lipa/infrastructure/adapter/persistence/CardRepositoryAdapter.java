package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.CardRepositoryPort;
import com.lipa.infrastructure.persistence.jpa.entity.CardEntity;
import com.lipa.infrastructure.persistence.jpa.repo.CardJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CardRepositoryAdapter implements CardRepositoryPort {

    private final CardJpaRepository repo;

    public CardRepositoryAdapter(CardJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public CardEntity save(CardEntity entity) {
        return repo.save(entity);
    }

    @Override
    public Optional<CardEntity> findByUid(String uid) {
        return repo.findByUid(uid);
    }

    @Override
    public Optional<CardEntity> findById(UUID id) {
        return repo.findById(id);
    }
}
