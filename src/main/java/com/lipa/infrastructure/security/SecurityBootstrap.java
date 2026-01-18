package com.lipa.infrastructure.security;

import com.lipa.application.port.out.PasswordHasherPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.infrastructure.persistence.entity.AgentUserEntity;
import com.lipa.infrastructure.persistence.repo.AgentUserJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class SecurityBootstrap implements ApplicationRunner {

    private final AgentUserJpaRepository repo;
    private final TimeProviderPort time;
    private final PasswordHasherPort passwordHasher;

    @Value("${lipa.security.bootstrap.enabled:true}")
    private boolean enabled;

    @Value("${lipa.security.bootstrap.admin.username:admin}")
    private String adminUsername;

    @Value("${lipa.security.bootstrap.admin.password:admin123}")
    private String adminPassword;

    @Value("${lipa.security.bootstrap.agent.username:agent}")
    private String agentUsername;

    @Value("${lipa.security.bootstrap.agent.password:agent123}")
    private String agentPassword;

    public SecurityBootstrap(AgentUserJpaRepository repo, TimeProviderPort time, PasswordHasherPort passwordHasher) {
        this.repo = repo;
        this.time = time;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) return;
        if (repo.count() > 0) return;

        Instant now = time.now();

        AgentUserEntity admin = new AgentUserEntity();
        admin.setId(UUID.randomUUID());
        admin.setUsername(adminUsername);
        admin.setPasswordHash(passwordHasher.hash(adminPassword));
        admin.setRole(AgentUserEntity.Role.ADMIN);
        admin.setStatus(AgentUserEntity.Status.ACTIVE);
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);

        AgentUserEntity agent = new AgentUserEntity();
        agent.setId(UUID.randomUUID());
        agent.setUsername(agentUsername);
        agent.setPasswordHash(passwordHasher.hash(agentPassword));
        agent.setRole(AgentUserEntity.Role.AGENT);
        agent.setStatus(AgentUserEntity.Status.ACTIVE);
        agent.setCreatedAt(now);
        agent.setUpdatedAt(now);

        repo.save(admin);
        repo.save(agent);
    }
}
