package com.lipa.infrastructure.persistence.jpa.repo;

import com.lipa.infrastructure.persistence.jpa.entity.AgentUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgentUserJpaRepository extends JpaRepository<AgentUserEntity, UUID> {

    Optional<AgentUserEntity> findByUsername(String username);
}
