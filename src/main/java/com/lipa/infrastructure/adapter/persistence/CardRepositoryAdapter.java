package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.CardRepositoryPort;
import com.lipa.domain.model.Account;
import com.lipa.domain.model.Card;
import com.lipa.domain.model.CardStatus;
import com.lipa.infrastructure.persistence.entity.AccountEntity;
import com.lipa.infrastructure.persistence.entity.CardEntity;
import com.lipa.infrastructure.persistence.repo.AccountJpaRepository;
import com.lipa.infrastructure.persistence.repo.CardJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CardRepositoryAdapter implements CardRepositoryPort {

    private final AccountJpaRepository accountRepo;
    private final CardJpaRepository cardRepo;

    public CardRepositoryAdapter(AccountJpaRepository accountRepo, CardJpaRepository cardRepo) {
        this.accountRepo = accountRepo;
        this.cardRepo = cardRepo;
    }

    @Override
    public boolean existsByUid(String uid) {
        return cardRepo.findByUid(uid).isPresent();
    }

    @Override
    public Optional<Card> findByUid(String uid) {
        return cardRepo.findByUid(uid).map(this::toDomain);
    }

    @Override
    public void createAccountAndCard(Account account, Card card) {
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
    }

    @Override
    public void save(Card card) {
        var entity = cardRepo.findByUid(card.uid())
                .orElseThrow(() -> new IllegalStateException("Card not found for uid=" + card.uid()));

        entity.setStatus(CardEntity.CardStatus.valueOf(card.status().name()));
        entity.setPinHash(card.pinHash());
        entity.setPinFailCount(card.pinFailCount());
        entity.setPinBlockedUntil(card.pinBlockedUntil());
        entity.setUpdatedAt(card.updatedAt());

        cardRepo.save(entity);
    }

    private Card toDomain(CardEntity e) {
        return Card.restored(
                e.getId(),
                e.getUid(),
                e.getAccount().getId(),
                CardStatus.valueOf(e.getStatus().name()),
                e.getPinHash(),
                e.getPinFailCount(),
                e.getPinBlockedUntil(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
