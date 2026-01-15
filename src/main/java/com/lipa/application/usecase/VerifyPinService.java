package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.VerifyPinUseCase;
import com.lipa.application.port.out.AuditRepositoryPort;
import com.lipa.application.port.out.CardRepositoryPort;
import com.lipa.application.port.out.PinHasherPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.infrastructure.persistence.jpa.entity.AuditEventEntity;
import com.lipa.infrastructure.persistence.jpa.entity.CardEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class VerifyPinService implements VerifyPinUseCase {

    private static final int MAX_FAILS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private final CardRepositoryPort cardRepository;
    private final PinHasherPort hasher;
    private final AuditRepositoryPort auditRepository;
    private final TimeProviderPort time;

    public VerifyPinService(CardRepositoryPort cardRepository,
                            PinHasherPort hasher,
                            AuditRepositoryPort auditRepository,
                            TimeProviderPort time) {
        this.cardRepository = cardRepository;
        this.hasher = hasher;
        this.auditRepository = auditRepository;
        this.time = time;
    }

    @Override
    @Transactional
    public Result verify(Command command) {
        String uid = normalizeUid(command.cardUid());
        String pin = command.rawPin();
        if (pin == null || pin.trim().isEmpty()) {
            throw new BusinessRuleException("PIN is required");
        }

        CardEntity card = cardRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Card not found for uid=" + uid));

        if (card.getStatus() != CardEntity.CardStatus.ACTIVE) {
            audit("PIN_VERIFY_DENIED", card, Map.of("uid", uid, "reason", "CARD_NOT_ACTIVE"));
            return new Result(false, true);
        }

        Instant now = time.now();
        if (card.getPinBlockedUntil() != null && now.isBefore(card.getPinBlockedUntil())) {
            audit("PIN_VERIFY_DENIED", card, Map.of(
                    "uid", uid,
                    "reason", "PIN_BLOCKED",
                    "blockedUntil", card.getPinBlockedUntil().toString()
            ));
            return new Result(false, true);
        }

        if (card.getPinHash() == null || card.getPinHash().isBlank()) {
            audit("PIN_VERIFY_DENIED", card, Map.of("uid", uid, "reason", "PIN_NOT_SET"));
            return new Result(false, false);
        }

        boolean ok = hasher.matches(pin, card.getPinHash());
        if (ok) {
            card.setPinFailCount(0);
            card.setPinBlockedUntil(null);
            card.setUpdatedAt(now);
            cardRepository.save(card);

            audit("PIN_VERIFY_SUCCESS", card, Map.of("uid", uid));
            return new Result(true, false);
        }

        int newFails = card.getPinFailCount() + 1;
        card.setPinFailCount(newFails);

        boolean blocked = false;
        if (newFails >= MAX_FAILS) {
            blocked = true;
            card.setPinBlockedUntil(now.plus(BLOCK_DURATION));
        }
        card.setUpdatedAt(now);
        cardRepository.save(card);

        audit("PIN_VERIFY_FAILED", card, Map.of(
                "uid", uid,
                "failCount", String.valueOf(newFails),
                "blocked", String.valueOf(blocked),
                "blockedUntil", card.getPinBlockedUntil() == null ? "" : card.getPinBlockedUntil().toString()
        ));

        return new Result(false, blocked);
    }

    private void audit(String action, CardEntity card, Map<String, Object> metadata) {
        AuditEventEntity audit = new AuditEventEntity();
        audit.setId(UUID.randomUUID());
        audit.setActorType(AuditEventEntity.ActorType.SYSTEM);
        audit.setActorId(null);
        audit.setAction(action);
        audit.setTargetType(AuditEventEntity.TargetType.CARD);
        audit.setTargetId(card.getId());
        audit.setMetadata(metadata);
        audit.setCreatedAt(time.now());
        auditRepository.save(audit);
    }

    private String normalizeUid(String uid) {
        if (uid == null) throw new BusinessRuleException("cardUid is required");
        String trimmed = uid.trim();
        if (trimmed.isEmpty()) throw new BusinessRuleException("cardUid is required");
        return trimmed;
    }
}
