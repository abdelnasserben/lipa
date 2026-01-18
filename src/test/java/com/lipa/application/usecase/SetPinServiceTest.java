package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.SetPinUseCase;
import com.lipa.application.port.out.CardRepositoryPort;
import com.lipa.application.port.out.PinHasherPort;
import com.lipa.application.port.out.SetPinAuditPort;
import com.lipa.application.port.out.TimeProviderPort;
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
class SetPinServiceTest {

    @Mock CardRepositoryPort cards;
    @Mock PinHasherPort hasher;
    @Mock SetPinAuditPort audit;
    @Mock TimeProviderPort time;

    @Captor ArgumentCaptor<Card> savedCaptor;

    private SetPinService service;

    @BeforeEach
    void setUp() {
        service = new SetPinService(cards, hasher, audit, time);
    }

    @Test
    void setPin_fails_when_card_missing() {
        when(cards.findByUid("UID")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.setPin(new SetPinUseCase.Command("UID", "1234", "r")));
        verify(cards, never()).save(any());
    }

    @Test
    void setPin_fails_when_card_not_active() {
        Card c = Card.restored(
                UUID.randomUUID(), "UID", UUID.randomUUID(),
                CardStatus.BLOCKED, null, 0, null,
                Instant.parse("2026-01-18T00:00:00Z"),
                Instant.parse("2026-01-18T00:00:00Z")
        );
        when(cards.findByUid("UID")).thenReturn(Optional.of(c));

        var ex = assertThrows(BusinessRuleException.class,
                () -> service.setPin(new SetPinUseCase.Command("UID", "1234", "r")));
        assertEquals("Card is not active", ex.getMessage());
    }

    @Test
    void setPin_saves_hashed_pin_and_resets_state() {
        UUID cardId = UUID.randomUUID();
        Card c = Card.restored(
                cardId, "UID", UUID.randomUUID(),
                CardStatus.ACTIVE, "OLD", 3, Instant.parse("2026-01-18T09:00:00Z"),
                Instant.parse("2026-01-18T00:00:00Z"),
                Instant.parse("2026-01-18T00:00:00Z")
        );

        when(cards.findByUid("UID")).thenReturn(Optional.of(c));
        when(hasher.hash("1234")).thenReturn("HASH");
        Instant now = Instant.parse("2026-01-18T10:00:00Z");
        when(time.now()).thenReturn(now);

        service.setPin(new SetPinUseCase.Command("UID", "1234", "reason"));

        verify(cards).save(savedCaptor.capture());
        Card saved = savedCaptor.getValue();
        assertEquals(cardId, saved.id());
        assertEquals("HASH", saved.pinHash());
        assertEquals(0, saved.pinFailCount());
        assertNull(saved.pinBlockedUntil());
        assertEquals(now, saved.updatedAt());
    }
}
