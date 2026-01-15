package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.EnrollCardPersistCommand;
import com.lipa.application.dto.EnrollCardPersistResult;
import com.lipa.application.port.out.EnrollCardPersistencePort;
import com.lipa.domain.model.Account;
import com.lipa.domain.model.Card;
import com.lipa.infrastructure.persistence.jpa.entity.AccountEntity;
import com.lipa.infrastructure.persistence.jpa.entity.CardEntity;
import com.lipa.infrastructure.persistence.jpa.repo.AccountJpaRepository;
import com.lipa.infrastructure.persistence.jpa.repo.CardJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class EnrollCardPersistenceAdapter implements EnrollCardPersistencePort {

    private final AccountJpaRepository accountRepo;
    private final CardJpaRepository cardRepo;

    public EnrollCardPersistenceAdapter(AccountJpaRepository accountRepo,
                                        CardJpaRepository cardRepo) {
        this.accountRepo = accountRepo;
        this.cardRepo = cardRepo;
    }

    @Override
    public EnrollCardPersistResult persist(EnrollCardPersistCommand command) {
        Account account = command.account();
        Card card = command.card();

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(account.id());
        accountEntity.setType(AccountEntity.AccountType.valueOf(account.type().name()));
        accountEntity.setStatus(AccountEntity.AccountStatus.valueOf(account.status().name()));
        accountEntity.setDisplayName(account.displayName());
        accountEntity.setPhone(account.phone());
        accountEntity.setCreatedAt(account.createdAt());

        accountRepo.save(accountEntity);

        CardEntity cardEntity = new CardEntity();
        cardEntity.setId(card.id());
        cardEntity.setUid(card.uid());
        cardEntity.setAccount(accountEntity);
        cardEntity.setStatus(CardEntity.CardStatus.valueOf(card.status().name()));
        cardEntity.setPinHash(card.pinHash());
        cardEntity.setPinFailCount(card.pinFailCount());
        cardEntity.setPinBlockedUntil(card.pinBlockedUntil());
        cardEntity.setCreatedAt(card.createdAt());
        cardEntity.setUpdatedAt(card.updatedAt());

        cardRepo.save(cardEntity);

        return new EnrollCardPersistResult(accountEntity.getId(), cardEntity.getId(), cardEntity.getUid());
    }
}
