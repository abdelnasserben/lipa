package com.lipa.infrastructure.security;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.infrastructure.persistence.jpa.entity.AgentUserEntity;
import com.lipa.infrastructure.persistence.jpa.repo.AgentUserJpaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final AgentUserJpaRepository users;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AgentUserJpaRepository users, JwtService jwtService) {
        this.users = users;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public LoginResult login(String username, String rawPassword, Instant now) {
        AgentUserEntity user = users.findByUsername(username.trim())
                .orElseThrow(() -> new BusinessRuleException("Invalid credentials"));

        if (user.getStatus() != AgentUserEntity.Status.ACTIVE) {
            throw new BusinessRuleException("User disabled");
        }

        boolean ok = passwordEncoder.matches(rawPassword, user.getPasswordHash());
        if (!ok) {
            throw new BusinessRuleException("Invalid credentials");
        }

        var token = jwtService.issueToken(user, now);
        return new LoginResult(token.token(), token.expiresInSeconds(), user.getUsername(), user.getRole().name(), token.issuedAt(), token.expiresAt());
    }

    public record LoginResult(
            String accessToken,
            long expiresInSeconds,
            String username,
            String role,
            Instant issuedAt,
            Instant expiresAt
    ) {}
}
