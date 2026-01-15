package com.lipa.infrastructure.persistence.jpa.repo;

import com.lipa.infrastructure.persistence.jpa.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryEntity, UUID> {}
