package com.lipa.infrastructure.persistence.repo;

import com.lipa.infrastructure.persistence.entity.FeeConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FeeConfigurationJpaRepository extends JpaRepository<FeeConfigurationEntity, UUID> {
    Optional<FeeConfigurationEntity> findByActiveTrue();
}
