package com.lipa.infrastructure.persistence.repo;

import com.lipa.infrastructure.persistence.entity.AgentUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgentUserJpaRepository extends JpaRepository<AgentUserEntity, UUID> {

    Optional<AgentUserEntity> findByUsername(String username);
}
