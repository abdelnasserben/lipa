package com.lipa.application.usecase;

import com.lipa.application.dto.AccountSnapshot;
import com.lipa.application.dto.IdempotentTransactionSnapshot;
import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.CreateCashInUseCase;
import com.lipa.application.port.out.AccountSnapshotPort;
import com.lipa.application.port.out.CashInPersistencePort;
import com.lipa.application.port.out.IdempotencyPort;
import com.lipa.application.port.out.TimeProviderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCashInServiceTest {

    @Mock AccountSnapshotPort accounts;
    @Mock IdempotencyPort idempotency;
    @Mock CashInPersistencePort persistence;
    @Mock TimeProviderPort time;

    @Captor ArgumentCaptor<CashInPersistencePort.PersistCommand> persistCaptor;

    private CreateCashInService service;

    @BeforeEach
    void setUp() {
        service = new CreateCashInService(accounts, idempotency, persistence, time);
    }

    @Test
    void create_returns_existing_transaction_when_idempotency_key_already_used() {
        UUID txId = UUID.randomUUID();
        when(idempotency.findByIdempotencyKey("K")).thenReturn(Optional.of(new IdempotentTransactionSnapshot(txId, "SUCCESS")));

        var res = service.create(new CreateCashInUseCase.Command(
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                "KMF",
                "K",
                "desc",
                UUID.randomUUID()
        ));

        assertEquals(txId, res.transactionId());
        assertEquals("SUCCESS", res.status());
        verifyNoInteractions(accounts);
        verifyNoInteractions(persistence);
    }

    @Test
    void create_requires_client_and_technical_account_ids() {
        assertThrows(BusinessRuleException.class, () ->
                service.create(new CreateCashInUseCase.Command(
                        null,
                        new BigDecimal("10.00"),
                        "KMF",
                        "K",
                        null,
                        UUID.randomUUID()
                ))
        );

        assertThrows(BusinessRuleException.class, () ->
                service.create(new CreateCashInUseCase.Command(
                        UUID.randomUUID(),
                        new BigDecimal("10.00"),
                        "KMF",
                        "K",
                        null,
                        null
                ))
        );
    }

    @Test
    void create_throws_not_found_when_accounts_missing() {
        when(idempotency.findByIdempotencyKey("K")).thenReturn(Optional.empty());

        UUID clientId = UUID.randomUUID();
        UUID technicalId = UUID.randomUUID();

        when(accounts.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                service.create(new CreateCashInUseCase.Command(
                        clientId,
                        new BigDecimal("10.00"),
                        "KMF",
                        "K",
                        null,
                        technicalId
                ))
        );
    }

    @Test
    void create_persists_cash_in_when_valid() {
        when(idempotency.findByIdempotencyKey("K")).thenReturn(Optional.empty());

        UUID clientId = UUID.randomUUID();
        UUID technicalId = UUID.randomUUID();
        when(accounts.findById(clientId)).thenReturn(Optional.of(new AccountSnapshot(clientId, "CLIENT", "ACTIVE")));
        when(accounts.findById(technicalId)).thenReturn(Optional.of(new AccountSnapshot(technicalId, "TECHNICAL", "ACTIVE")));

        Instant now = Instant.parse("2026-01-18T10:00:00Z");
        when(time.now()).thenReturn(now);

        UUID txId = UUID.randomUUID();
        when(persistence.persist(any())).thenReturn(new CashInPersistencePort.PersistResult(txId, "SUCCESS"));

        var res = service.create(new CreateCashInUseCase.Command(
                clientId,
                new BigDecimal("25.00"),
                "kmf",
                " K ",
                "  desc  ",
                technicalId
        ));

        assertEquals(txId, res.transactionId());
        assertEquals("SUCCESS", res.status());

        verify(persistence).persist(persistCaptor.capture());
        var cmd = persistCaptor.getValue();

        assertEquals(clientId, cmd.clientAccountId());
        assertEquals(technicalId, cmd.technicalAccountId());
        assertEquals(new BigDecimal("25.00"), cmd.amount());
        assertEquals("KMF", cmd.currency());
        assertEquals("K", cmd.idempotencyKey());
        assertEquals("desc", cmd.description());
        assertEquals(now, cmd.createdAt());
    }
}
