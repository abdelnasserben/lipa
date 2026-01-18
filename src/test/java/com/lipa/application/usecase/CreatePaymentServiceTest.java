package com.lipa.application.usecase;

import com.lipa.application.dto.AccountSnapshot;
import com.lipa.application.dto.IdempotentTransactionSnapshot;
import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.port.in.CreatePaymentUseCase;
import com.lipa.application.port.in.VerifyPinUseCase;
import com.lipa.application.port.out.*;
import com.lipa.domain.model.Card;
import com.lipa.domain.model.CardStatus;
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
class CreatePaymentServiceTest {

    @Mock PaymentIdempotencyPort idempotency;
    @Mock VerifyPinUseCase verifyPin;

    @Mock CardRepositoryPort cards;
    @Mock AccountSnapshotPort accounts;
    @Mock AccountReadPort ledger;

    @Mock PaymentPersistencePort persistence;
    @Mock PaymentFeePort fees;
    @Mock PaymentAuditPort audit;
    @Mock TimeProviderPort time;

    @Captor ArgumentCaptor<PaymentPersistencePort.PersistCommand> persistCaptor;

    private CreatePaymentService service;

    @BeforeEach
    void setUp() {
        service = new CreatePaymentService(
                idempotency, verifyPin, cards, accounts, ledger, persistence, fees, audit, time
        );
    }

    @Test
    void create_fails_when_pin_invalid() {
        when(time.now()).thenReturn(Instant.parse("2026-01-18T10:00:00Z"));
        when(verifyPin.verify(any())).thenReturn(new VerifyPinUseCase.Result(false, false));

        var ex = assertThrows(BusinessRuleException.class, () ->
                service.create(new CreatePaymentUseCase.Command(
                        "UID", "1234", UUID.randomUUID(), new BigDecimal("10.00"),
                        "KMF", "IDEM", "desc"
                ))
        );

        assertEquals("Invalid PIN", ex.getMessage());
        verifyNoInteractions(cards, idempotency, accounts, ledger, persistence, fees, audit);
    }

    @Test
    void create_returns_existing_transaction_when_idempotency_key_already_used_after_pin() {
        when(time.now()).thenReturn(Instant.parse("2026-01-18T10:00:00Z"));
        when(verifyPin.verify(any())).thenReturn(new VerifyPinUseCase.Result(true, false));

        UUID payerAccountId = UUID.randomUUID();
        Card card = Card.restored(
                UUID.randomUUID(),
                "UID",
                payerAccountId,
                CardStatus.ACTIVE,
                "HASH",
                0,
                null,
                Instant.parse("2026-01-18T00:00:00Z"),
                Instant.parse("2026-01-18T00:00:00Z")
        );
        when(cards.findByUid("UID")).thenReturn(Optional.of(card));

        UUID existingTxId = UUID.randomUUID();
        when(idempotency.findByIdempotencyKey("IDEM"))
                .thenReturn(Optional.of(new IdempotentTransactionSnapshot(existingTxId, "SUCCESS")));

        var res = service.create(new CreatePaymentUseCase.Command(
                "UID", "1234", UUID.randomUUID(), new BigDecimal("10.00"), "KMF", "IDEM", "desc"
        ));

        assertEquals(existingTxId, res.transactionId());
        assertEquals("SUCCESS", res.status());

        verifyNoInteractions(accounts, ledger, persistence, fees, audit);
    }

    @Test
    void create_refuses_when_insufficient_funds() {
        Instant now = Instant.parse("2026-01-18T10:00:00Z");
        when(time.now()).thenReturn(now);
        when(verifyPin.verify(any())).thenReturn(new VerifyPinUseCase.Result(true, false));

        UUID payerAccountId = UUID.randomUUID();
        UUID merchantAccountId = UUID.randomUUID();

        Card card = Card.restored(
                UUID.randomUUID(), "UID", payerAccountId, CardStatus.ACTIVE,
                "HASH", 0, null,
                Instant.parse("2026-01-18T00:00:00Z"),
                Instant.parse("2026-01-18T00:00:00Z")
        );
        when(cards.findByUid("UID")).thenReturn(Optional.of(card));
        when(idempotency.findByIdempotencyKey("IDEM")).thenReturn(Optional.empty());

        when(accounts.findByIdForUpdate(payerAccountId))
                .thenReturn(Optional.of(new AccountSnapshot(payerAccountId, "CLIENT", "ACTIVE")));
        when(accounts.findById(merchantAccountId))
                .thenReturn(Optional.of(new AccountSnapshot(merchantAccountId, "MERCHANT", "ACTIVE")));

        when(ledger.sumCredits(payerAccountId)).thenReturn(new BigDecimal("50.00"));
        when(ledger.sumDebits(payerAccountId)).thenReturn(new BigDecimal("0.00"));
        when(fees.quote(new BigDecimal("60.00"))).thenReturn(new PaymentFeePort.FeeQuote(new BigDecimal("5.00")));

        var ex = assertThrows(BusinessRuleException.class, () ->
                service.create(new CreatePaymentUseCase.Command(
                        "UID", "1234", merchantAccountId,
                        new BigDecimal("60.00"), "KMF", "IDEM", "desc"
                ))
        );

        assertEquals("Insufficient funds", ex.getMessage());
        verify(audit).record(any());
        verify(persistence, never()).persist(any());
    }

    @Test
    void create_persists_payment_when_valid() {
        Instant now = Instant.parse("2026-01-18T10:00:00Z");
        when(time.now()).thenReturn(now);
        when(verifyPin.verify(any())).thenReturn(new VerifyPinUseCase.Result(true, false));

        UUID payerAccountId = UUID.randomUUID();
        UUID merchantAccountId = UUID.randomUUID();

        Card card = Card.restored(
                UUID.randomUUID(), "UID", payerAccountId, CardStatus.ACTIVE,
                "HASH", 0, null,
                Instant.parse("2026-01-18T00:00:00Z"),
                Instant.parse("2026-01-18T00:00:00Z")
        );
        when(cards.findByUid("UID")).thenReturn(Optional.of(card));
        when(idempotency.findByIdempotencyKey("IDEM")).thenReturn(Optional.empty());

        when(accounts.findByIdForUpdate(payerAccountId))
                .thenReturn(Optional.of(new AccountSnapshot(payerAccountId, "CLIENT", "ACTIVE")));
        when(accounts.findById(merchantAccountId))
                .thenReturn(Optional.of(new AccountSnapshot(merchantAccountId, "MERCHANT", "ACTIVE")));

        when(ledger.sumCredits(payerAccountId)).thenReturn(new BigDecimal("200.00"));
        when(ledger.sumDebits(payerAccountId)).thenReturn(new BigDecimal("0.00"));
        when(fees.quote(new BigDecimal("60.00"))).thenReturn(new PaymentFeePort.FeeQuote(new BigDecimal("5.00")));

        UUID txId = UUID.randomUUID();
        when(persistence.persist(any()))
                .thenReturn(new PaymentPersistencePort.PersistResult(txId, "SUCCESS"));

        var res = service.create(new CreatePaymentUseCase.Command(
                "UID", "1234", merchantAccountId,
                new BigDecimal("60.00"), "KMF", "IDEM", "desc"
        ));

        assertEquals(txId, res.transactionId());
        assertEquals("SUCCESS", res.status());

        verify(persistence).persist(persistCaptor.capture());
        var cmd = persistCaptor.getValue();

        assertEquals(payerAccountId, cmd.payerAccountId());
        assertEquals(merchantAccountId, cmd.merchantAccountId());
        assertEquals(new BigDecimal("60.00"), cmd.amount());
        assertEquals("KMF", cmd.currency());
        assertEquals("IDEM", cmd.idempotencyKey());
        assertEquals("desc", cmd.description());
        assertEquals(now, cmd.createdAt());

        verify(audit, atLeastOnce()).record(any());
    }
}
