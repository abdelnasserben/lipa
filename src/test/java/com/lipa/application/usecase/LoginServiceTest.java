package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.port.in.LoginUseCase;
import com.lipa.application.port.out.AuthUserRepositoryPort;
import com.lipa.application.port.out.PasswordHasherPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.application.port.out.TokenIssuerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock AuthUserRepositoryPort users;
    @Mock PasswordHasherPort hasher;
    @Mock TokenIssuerPort tokenIssuer;
    @Mock TimeProviderPort time;

    private LoginService service;

    @BeforeEach
    void setUp() {
        service = new LoginService(users, hasher, tokenIssuer, time);
    }

    @Test
    void login_fails_when_user_not_found() {
        when(users.findByUsername("admin")).thenReturn(Optional.empty());

        var ex = assertThrows(BusinessRuleException.class, () ->
                service.login(new LoginUseCase.Command("admin", "x"))
        );
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void login_fails_when_user_disabled() {
        when(users.findByUsername("admin")).thenReturn(Optional.of(
                new AuthUserRepositoryPort.AuthUserView(
                        UUID.randomUUID(), "admin", "HASH", "ADMIN", "DISABLED"
                )
        ));

        var ex = assertThrows(BusinessRuleException.class, () ->
                service.login(new LoginUseCase.Command("admin", "x"))
        );
        assertEquals("User disabled", ex.getMessage());
    }

    @Test
    void login_fails_when_password_invalid() {
        var userId = UUID.randomUUID();
        when(users.findByUsername("admin")).thenReturn(Optional.of(
                new AuthUserRepositoryPort.AuthUserView(userId, "admin", "HASH", "ADMIN", "ACTIVE")
        ));
        when(hasher.matches("bad", "HASH")).thenReturn(false);

        var ex = assertThrows(BusinessRuleException.class, () ->
                service.login(new LoginUseCase.Command("admin", "bad"))
        );
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void login_returns_token_when_ok() {
        var userId = UUID.randomUUID();
        when(users.findByUsername("admin")).thenReturn(Optional.of(
                new AuthUserRepositoryPort.AuthUserView(userId, "admin", "HASH", "ADMIN", "ACTIVE")
        ));
        when(hasher.matches("ok", "HASH")).thenReturn(true);

        Instant now = Instant.parse("2026-01-18T10:00:00Z");
        when(time.now()).thenReturn(now);

        var token = new TokenIssuerPort.Token("TOKEN", 3600, now, now.plusSeconds(3600));
        when(tokenIssuer.issue(any(), eq(now))).thenReturn(token);

        var res = service.login(new LoginUseCase.Command("admin", "ok"));

        assertEquals("TOKEN", res.accessToken());
        assertEquals("Bearer", res.tokenType());
        assertEquals(3600, res.expiresInSeconds());
        assertEquals("admin", res.username());
        assertEquals("ADMIN", res.role());
        assertEquals(now, res.issuedAt());
        assertEquals(now.plusSeconds(3600), res.expiresAt());
    }
}
