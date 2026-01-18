package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.AgentUserRepositoryPort;
import com.lipa.infrastructure.persistence.entity.AgentUserEntity;
import com.lipa.infrastructure.persistence.repo.AgentUserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AgentUserRepositoryAdapter implements AgentUserRepositoryPort {

    private final AgentUserJpaRepository repo;

    public AgentUserRepositoryAdapter(AgentUserJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<UUID> findIdByUsername(String username) {
        return repo.findByUsername(username).map(AgentUserEntity::getId);
    }

    @Override
    public UUID create(CreateUserCommand command) {
        AgentUserEntity e = new AgentUserEntity();
        e.setId(UUID.randomUUID());
        e.setUsername(command.username());
        e.setPasswordHash(command.passwordHash());
        e.setRole(AgentUserEntity.Role.valueOf(command.role().name()));
        e.setStatus(AgentUserEntity.Status.valueOf(command.status().name()));
        e.setCreatedAt(command.now());
        e.setUpdatedAt(command.now());

        repo.save(e);
        return e.getId();
    }
}
