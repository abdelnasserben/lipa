package com.lipa.infrastructure.persistence.jpa.repo;

import com.lipa.infrastructure.persistence.jpa.entity.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventEntity, UUID> {}
