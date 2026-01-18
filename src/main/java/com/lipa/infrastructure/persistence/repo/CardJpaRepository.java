package com.lipa.infrastructure.persistence.repo;

import com.lipa.infrastructure.persistence.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CardJpaRepository extends JpaRepository<CardEntity, UUID> {
    Optional<CardEntity> findByUid(String uid);
}
