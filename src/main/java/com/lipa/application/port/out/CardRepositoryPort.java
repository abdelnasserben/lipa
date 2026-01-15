package com.lipa.application.port.out;

import com.lipa.infrastructure.persistence.jpa.entity.CardEntity;

import java.util.Optional;
import java.util.UUID;

public interface CardRepositoryPort {
    CardEntity save(CardEntity entity);
    Optional<CardEntity> findByUid(String uid);
    Optional<CardEntity> findById(UUID id);
}
