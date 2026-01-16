package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.port.in.CreateAgentUserUseCase;
import com.lipa.application.port.out.AgentUserLookupPort;
import com.lipa.application.port.out.AgentUserPersistencePort;
import com.lipa.application.port.out.PasswordHasherPort;
import com.lipa.application.port.out.TimeProviderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAgentUserServiceTest {

    @Mock AgentUserLookupPort lookupPort;
    @Mock AgentUserPersistencePort persistencePort;
    @Mock PasswordHasherPort passwordHasher;
    @Mock TimeProviderPort time;

    @Captor ArgumentCaptor<AgentUserPersistencePort.CreateUserCommand> persistCaptor;

    private CreateAgentUserService service;
    private Instant now;

    @BeforeEach
    void setUp() {
        service = new CreateAgentUserService(lookupPort, persistencePort, passwordHasher, time);
        now = Instant.parse("2026-01-16T10:15:30Z");
    }

    @Test
    void create_user_requires_username_and_password() {
        assertThrows(BusinessRuleException.class, () ->
                service.create(new CreateAgentUserUseCase.Command("   ", "pass", "AGENT"))
        );
        assertThrows(BusinessRuleException.class, () ->
                service.create(new CreateAgentUserUseCase.Command("john", "   ", "AGENT"))
        );
    }

    @Test
    void create_user_requires_valid_role() {
        assertThrows(BusinessRuleException.class, () ->
                service.create(new CreateAgentUserUseCase.Command("john", "pass", ""))
        );
        assertThrows(BusinessRuleException.class, () ->
                service.create(new CreateAgentUserUseCase.Command("john", "pass", "UNKNOWN"))
        );
    }

    @Test
    void create_user_fails_if_username_already_exists() {
        when(lookupPort.findByUsername("john")).thenReturn(Optional.of(UUID.randomUUID()));

        assertThrows(BusinessRuleException.class, () ->
                service.create(new CreateAgentUserUseCase.Command("  john  ", "pass", "AGENT"))
        );

        verify(persistencePort, never()).create(any());
    }

    @Test
    void create_user_happy_path_hashes_password_and_persists() {
        when(time.now()).thenReturn(now);

        when(lookupPort.findByUsername("john")).thenReturn(Optional.empty());
        when(passwordHasher.hash("pass")).thenReturn("HASHED");
        UUID newId = UUID.randomUUID();
        when(persistencePort.create(any())).thenReturn(newId);

        CreateAgentUserUseCase.Result res = service.create(
                new CreateAgentUserUseCase.Command("  john  ", "pass", "ADMIN")
        );

        assertEquals(newId, res.userId());
        assertEquals("john", res.username());
        assertEquals("ADMIN", res.role());
        assertEquals("ACTIVE", res.status());
        assertEquals(now, res.createdAt());

        verify(persistencePort).create(persistCaptor.capture());
        var cmd = persistCaptor.getValue();

        assertEquals("john", cmd.username());
        assertEquals("HASHED", cmd.passwordHash());
        assertEquals(AgentUserPersistencePort.Role.ADMIN, cmd.role());
        assertEquals(AgentUserPersistencePort.Status.ACTIVE, cmd.status());
        assertEquals(now, cmd.now());
    }
}
