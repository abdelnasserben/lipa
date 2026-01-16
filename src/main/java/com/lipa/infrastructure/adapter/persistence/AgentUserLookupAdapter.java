package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.AgentUserLookupPort;
import com.lipa.infrastructure.persistence.jpa.entity.AgentUserEntity;
import com.lipa.infrastructure.persistence.jpa.repo.AgentUserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AgentUserLookupAdapter implements AgentUserLookupPort {

    private final AgentUserJpaRepository repo;

    public AgentUserLookupAdapter(AgentUserJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<UUID> findByUsername(String username) {
        return repo.findByUsername(username).map(AgentUserEntity::getId);
    }
}
