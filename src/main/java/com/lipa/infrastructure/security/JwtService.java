package com.lipa.infrastructure.security;

import com.lipa.infrastructure.persistence.jpa.entity.AgentUserEntity;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class JwtService {

    private final JwtEncoder encoder;
    private final long expirationSeconds;

    public JwtService(
            @Value("${lipa.security.jwt.secret}") String secret,
            @Value("${lipa.security.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.expirationSeconds = expirationSeconds;

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec key = new SecretKeySpec(secretBytes, "HmacSHA256");

        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    public TokenResult issueToken(AgentUserEntity user, Instant now) {
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        // Claims simples : sub, role, userId
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("lipa")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getUsername())
                .claims(c -> {
                    c.put("role", user.getRole().name());
                    c.put("userId", user.getId().toString());
                })
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new TokenResult(token, expirationSeconds, now, expiresAt);
    }

    public record TokenResult(
            String token,
            long expiresInSeconds,
            Instant issuedAt,
            Instant expiresAt
    ) {}
}
