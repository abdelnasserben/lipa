package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.SetPinUseCase;
import com.lipa.application.port.out.AuditRepositoryPort;
import com.lipa.application.port.out.CardRepositoryPort;
import com.lipa.application.port.out.PinHasherPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.infrastructure.persistence.jpa.entity.AuditEventEntity;
import com.lipa.infrastructure.persistence.jpa.entity.CardEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class SetPinService implements SetPinUseCase {

    private final CardRepositoryPort cardRepository;
    private final PinHasherPort hasher;
    private final AuditRepositoryPort auditRepository;
    private final TimeProviderPort time;

    public SetPinService(CardRepositoryPort cardRepository,
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
    public void setPin(Command command) {
        String uid = normalizeUid(command.cardUid());
        validatePin(command.rawPin());

        CardEntity card = cardRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Card not found for uid=" + uid));

        if (card.getStatus() != CardEntity.CardStatus.ACTIVE) {
            throw new BusinessRuleException("Card is not active");
        }

        card.setPinHash(hasher.hash(command.rawPin()));
        card.setPinFailCount(0);
        card.setPinBlockedUntil(null);
        card.setUpdatedAt(time.now());
        cardRepository.save(card);

        AuditEventEntity audit = new AuditEventEntity();
        audit.setId(UUID.randomUUID());
        audit.setActorType(AuditEventEntity.ActorType.SYSTEM);
        audit.setActorId(null);
        audit.setAction("PIN_SET");
        audit.setTargetType(AuditEventEntity.TargetType.CARD);
        audit.setTargetId(card.getId());
        audit.setMetadata(Map.of(
                "uid", uid,
                "reason", safeReason(command.reason())
        ));
        audit.setCreatedAt(time.now());
        auditRepository.save(audit);
    }

    private void validatePin(String pin) {
        if (pin == null) throw new BusinessRuleException("PIN is required");
        String t = pin.trim();
        if (t.length() < 4 || t.length() > 8) {
            throw new BusinessRuleException("PIN length must be between 4 and 8");
        }
        for (int i = 0; i < t.length(); i++) {
            if (!Character.isDigit(t.charAt(i))) {
                throw new BusinessRuleException("PIN must contain only digits");
            }
        }
    }

    private String normalizeUid(String uid) {
        if (uid == null) throw new BusinessRuleException("cardUid is required");
        String trimmed = uid.trim();
        if (trimmed.isEmpty()) throw new BusinessRuleException("cardUid is required");
        return trimmed;
    }

    private String safeReason(String reason) {
        if (reason == null) return "N/A";
        String r = reason.trim();
        if (r.isEmpty()) return "N/A";
        return r.length() > 120 ? r.substring(0, 120) : r;
    }
}
