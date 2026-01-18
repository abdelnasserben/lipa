package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.port.in.LoginUseCase;
import com.lipa.application.port.out.AuthUserRepositoryPort;
import com.lipa.application.port.out.PasswordHasherPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.application.port.out.TokenIssuerPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService implements LoginUseCase {

    private final AuthUserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final TokenIssuerPort tokenIssuer;
    private final TimeProviderPort time;

    public LoginService(AuthUserRepositoryPort users,
                        PasswordHasherPort passwordHasher,
                        TokenIssuerPort tokenIssuer,
                        TimeProviderPort time) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.time = time;
    }

    @Override
    @Transactional(readOnly = true)
    public Result login(Command command) {
        String username = requireTrimmed(command.username(), "username");
        String password = requireTrimmed(command.password(), "password");

        var user = users.findByUsername(username)
                .orElseThrow(() -> new BusinessRuleException("Invalid credentials"));

        // Conservation du message existant
        if (!"ACTIVE".equalsIgnoreCase(user.status())) {
            throw new BusinessRuleException("User disabled");
        }

        boolean ok = passwordHasher.matches(password, user.passwordHash());
        if (!ok) {
            throw new BusinessRuleException("Invalid credentials");
        }

        var now = time.now();
        var token = tokenIssuer.issue(new TokenIssuerPort.Subject(user.id(), user.username(), user.role()), now);

        return new Result(
                token.token(),
                "Bearer",
                token.expiresInSeconds(),
                user.username(),
                user.role(),
                token.issuedAt(),
                token.expiresAt()
        );
    }

    private static String requireTrimmed(String value, String field) {
        if (value == null) throw new BusinessRuleException(field + " is required");
        String t = value.trim();
        if (t.isEmpty()) throw new BusinessRuleException(field + " is required");
        return t;
    }
}
