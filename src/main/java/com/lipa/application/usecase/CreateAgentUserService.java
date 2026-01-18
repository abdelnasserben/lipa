package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.port.in.CreateAgentUserUseCase;
import com.lipa.application.port.out.AgentUserRepositoryPort;
import com.lipa.application.port.out.PasswordHasherPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.application.util.InputRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CreateAgentUserService implements CreateAgentUserUseCase {

    private final AgentUserRepositoryPort agentUsers;
    private final PasswordHasherPort passwordHasher;
    private final TimeProviderPort time;

    public CreateAgentUserService(AgentUserRepositoryPort agentUsers,
                                  PasswordHasherPort passwordHasher,
                                  TimeProviderPort time) {
        this.agentUsers = agentUsers;
        this.passwordHasher = passwordHasher;
        this.time = time;
    }

    @Override
    @Transactional
    public Result create(Command command) {
        String username = InputRules.requireTrimmedNotBlank(command.username(), "username");
        String password = InputRules.requireTrimmedNotBlank(command.password(), "password");

        AgentUserRepositoryPort.Role role = parseRole(command.role());

        if (agentUsers.findIdByUsername(username).isPresent()) {
            throw new BusinessRuleException("Username already exists");
        }

        Instant now = time.now();
        String hash = passwordHasher.hash(password);

        var createdId = agentUsers.create(new AgentUserRepositoryPort.CreateUserCommand(
                username,
                hash,
                role,
                AgentUserRepositoryPort.Status.ACTIVE,
                now
        ));

        return new Result(createdId, username, role.name(), "ACTIVE", now);
    }

    private AgentUserRepositoryPort.Role parseRole(String role) {
        String r = InputRules.requireTrimmedNotBlank(role, "role").toUpperCase();
        return switch (r) {
            case "ADMIN" -> AgentUserRepositoryPort.Role.ADMIN;
            case "AGENT" -> AgentUserRepositoryPort.Role.AGENT;
            default -> throw new BusinessRuleException("Invalid role");
        };
    }
}
