package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.port.in.CreateAgentUserUseCase;
import com.lipa.application.port.out.AgentUserLookupPort;
import com.lipa.application.port.out.AgentUserPersistencePort;
import com.lipa.application.port.out.PasswordHasherPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.application.util.InputRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CreateAgentUserService implements CreateAgentUserUseCase {

    private final AgentUserLookupPort lookupPort;
    private final AgentUserPersistencePort persistencePort;
    private final PasswordHasherPort passwordHasher;
    private final TimeProviderPort time;

    public CreateAgentUserService(AgentUserLookupPort lookupPort,
                                  AgentUserPersistencePort persistencePort,
                                  PasswordHasherPort passwordHasher,
                                  TimeProviderPort time) {
        this.lookupPort = lookupPort;
        this.persistencePort = persistencePort;
        this.passwordHasher = passwordHasher;
        this.time = time;
    }

    @Override
    @Transactional
    public Result create(Command command) {
        String username = InputRules.requireTrimmedNotBlank(command.username(), "username");
        String password = InputRules.requireTrimmedNotBlank(command.password(), "password");

        AgentUserPersistencePort.Role role = parseRole(command.role());

        if (lookupPort.findByUsername(username).isPresent()) {
            throw new BusinessRuleException("Username already exists");
        }

        Instant now = time.now();

        String hash = passwordHasher.hash(password);

        var createdId = persistencePort.create(new AgentUserPersistencePort.CreateUserCommand(
                username,
                hash,
                role,
                AgentUserPersistencePort.Status.ACTIVE,
                now
        ));

        return new Result(createdId, username, role.name(), "ACTIVE", now);
    }

    private AgentUserPersistencePort.Role parseRole(String role) {
        String r = InputRules.requireTrimmedNotBlank(role, "role").toUpperCase();
        return switch (r) {
            case "ADMIN" -> AgentUserPersistencePort.Role.ADMIN;
            case "AGENT" -> AgentUserPersistencePort.Role.AGENT;
            default -> throw new BusinessRuleException("Invalid role");
        };
    }
}
