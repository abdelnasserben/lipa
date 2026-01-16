package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.AgentUserPersistencePort;
import com.lipa.infrastructure.persistence.jpa.entity.AgentUserEntity;
import com.lipa.infrastructure.persistence.jpa.repo.AgentUserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AgentUserPersistenceAdapter implements AgentUserPersistencePort {

    private final AgentUserJpaRepository repo;

    public AgentUserPersistenceAdapter(AgentUserJpaRepository repo) {
        this.repo = repo;
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
