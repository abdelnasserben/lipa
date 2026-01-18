package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.VerifyPinUseCase;
import com.lipa.application.port.out.CardRepositoryPort;
import com.lipa.application.port.out.PinHasherPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.application.port.out.VerifyPinAuditPort;
import com.lipa.domain.model.Card;
import com.lipa.domain.model.CardStatus;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyPinServiceTest {

    @Mock CardRepositoryPort cards;
    @Mock PinHasherPort hasher;
    @Mock VerifyPinAuditPort audit;
    @Mock TimeProviderPort time;

    @Captor ArgumentCaptor<Card> savedCaptor;

    private VerifyPinService service;

    @BeforeEach
    void setUp() {
        service = new VerifyPinService(cards, hasher, audit, time);
    }

    @Test
    void verify_requires_pin() {
        assertThrows(BusinessRuleException.class, () -> service.verify(new VerifyPinUseCase.Command("UID", " ")));
    }

    @Test
    void verify_throws_not_found_when_card_missing() {
        when(cards.findByUid("UID")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.verify(new VerifyPinUseCase.Command("UID", "1234")));
    }

    @Test
    void verify_returns_denied_when_pin_not_set() {
        Card c = Card.restored(
                UUID.randomUUID(), "UID", UUID.randomUUID(),
                CardStatus.ACTIVE, null, 0, null,
                Instant.parse("2026-01-18T00:00:00Z"),
                Instant.parse("2026-01-18T00:00:00Z")
        );
        when(cards.findByUid("UID")).thenReturn(Optional.of(c));
        Instant now = Instant.parse("2026-01-18T10:00:00Z");
        when(time.now()).thenReturn(now);

        var res = service.verify(new VerifyPinUseCase.Command("UID", "1234"));

        assertFalse(res.success());
        assertFalse(res.cardBlocked());
        verify(cards, never()).save(any());
    }

    @Test
    void verify_success_resets_fail_count() {
        UUID id = UUID.randomUUID();
        Card c = Card.restored(
                id, "UID", UUID.randomUUID(),
                CardStatus.ACTIVE, "HASH", 4, Instant.parse("2026-01-18T09:00:00Z"),
                Instant.parse("2026-01-18T00:00:00Z"),
                Instant.parse("2026-01-18T00:00:00Z")
        );
        when(cards.findByUid("UID")).thenReturn(Optional.of(c));
        Instant now = Instant.parse("2026-01-18T10:00:00Z");
        when(time.now()).thenReturn(now);
        when(hasher.matches("1234", "HASH")).thenReturn(true);

        var res = service.verify(new VerifyPinUseCase.Command("UID", "1234"));

        assertTrue(res.success());
        assertFalse(res.cardBlocked());
        verify(cards).save(savedCaptor.capture());
        assertEquals(0, savedCaptor.getValue().pinFailCount());
        assertNull(savedCaptor.getValue().pinBlockedUntil());
    }
}
