package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.port.in.EnrollCardUseCase;
import com.lipa.application.port.out.AccountRepositoryPort;
import com.lipa.application.port.out.AuditRepositoryPort;
import com.lipa.application.port.out.CardRepositoryPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.infrastructure.persistence.jpa.entity.AccountEntity;
import com.lipa.infrastructure.persistence.jpa.entity.AuditEventEntity;
import com.lipa.infrastructure.persistence.jpa.entity.CardEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class EnrollCardService implements EnrollCardUseCase {

    private final AccountRepositoryPort accountRepository;
    private final CardRepositoryPort cardRepository;
    private final AuditRepositoryPort auditRepository;
    private final TimeProviderPort time;

    public EnrollCardService(AccountRepositoryPort accountRepository,
                             CardRepositoryPort cardRepository,
                             AuditRepositoryPort auditRepository,
                             TimeProviderPort time) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.auditRepository = auditRepository;
        this.time = time;
    }

    @Override
    @Transactional
    public Result enroll(Command command) {
        String uid = normalizeUid(command.cardUid());

        cardRepository.findByUid(uid).ifPresent(existing -> {
            throw new BusinessRuleException("Card already enrolled for uid=" + uid);
        });

        AccountEntity account = new AccountEntity();
        account.setType(AccountEntity.AccountType.CLIENT);
        account.setStatus(AccountEntity.AccountStatus.ACTIVE);
        account.setDisplayName(command.displayName());
        account.setPhone(command.phone());
        account.setCreatedAt(time.now());
        account.setId(UUID.randomUUID());

        account = accountRepository.save(account);

        CardEntity card = new CardEntity();
        card.setId(UUID.randomUUID());
        card.setUid(uid);
        card.setAccount(account);
        card.setStatus(CardEntity.CardStatus.ACTIVE);
        card.setPinHash(null);
        card.setPinFailCount(0);
        card.setPinBlockedUntil(null);
        card.setCreatedAt(time.now());
        card.setUpdatedAt(time.now());

        card = cardRepository.save(card);

        AuditEventEntity audit = new AuditEventEntity();
        audit.setId(UUID.randomUUID());
        audit.setActorType(AuditEventEntity.ActorType.SYSTEM);
        audit.setActorId(null);
        audit.setAction("CARD_ENROLLED");
        audit.setTargetType(AuditEventEntity.TargetType.CARD);
        audit.setTargetId(card.getId());
        audit.setMetadata(Map.of(
                "uid", uid,
                "accountId", account.getId().toString()
        ));
        audit.setCreatedAt(time.now());
        auditRepository.save(audit);

        return new Result(account.getId(), card.getId(), card.getUid());
    }

    private String normalizeUid(String uid) {
        if (uid == null) throw new BusinessRuleException("cardUid is required");
        String trimmed = uid.trim();
        if (trimmed.isEmpty()) throw new BusinessRuleException("cardUid is required");
        return trimmed;
    }
}