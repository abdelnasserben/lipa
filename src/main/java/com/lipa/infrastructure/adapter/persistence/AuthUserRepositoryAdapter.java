package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.AuthUserRepositoryPort;
import com.lipa.infrastructure.persistence.repo.AgentUserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthUserRepositoryAdapter implements AuthUserRepositoryPort {

    private final AgentUserJpaRepository repo;

    public AuthUserRepositoryAdapter(AgentUserJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<AuthUserView> findByUsername(String username) {
        return repo.findByUsername(username.trim())
                .map(u -> new AuthUserView(
                        u.getId(),
                        u.getUsername(),
                        u.getPasswordHash(),
                        u.getRole().name(),
                        u.getStatus().name()
                ));
    }
}
