package com.lipa.infrastructure.adapter.security;

import com.lipa.application.port.out.TokenIssuerPort;
import com.lipa.infrastructure.persistence.entity.AgentUserEntity;
import com.lipa.infrastructure.security.JwtService;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtTokenIssuerAdapter implements TokenIssuerPort {

    private final JwtService jwtService;

    public JwtTokenIssuerAdapter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Token issue(Subject subject, Instant now) {
        // On réutilise ton JwtService (NimbusJwtEncoder + claims role/userId/sub)
        AgentUserEntity user = new AgentUserEntity();
        user.setId(subject.userId());
        user.setUsername(subject.username());
        user.setRole(AgentUserEntity.Role.valueOf(subject.role()));

        var issued = jwtService.issueToken(user, now);

        return new Token(
                issued.token(),
                issued.expiresInSeconds(),
                issued.issuedAt(),
                issued.expiresAt()
        );
    }
}
