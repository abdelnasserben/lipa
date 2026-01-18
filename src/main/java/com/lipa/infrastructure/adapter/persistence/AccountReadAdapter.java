package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.AccountReadPort;
import com.lipa.infrastructure.persistence.repo.AccountJpaRepository;
import com.lipa.infrastructure.persistence.repo.LedgerEntryJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class AccountReadAdapter implements AccountReadPort {

    private final AccountJpaRepository accountRepo;
    private final LedgerEntryJpaRepository ledgerRepo;

    @PersistenceContext
    private EntityManager em;

    public AccountReadAdapter(AccountJpaRepository accountRepo, LedgerEntryJpaRepository ledgerRepo) {
        this.accountRepo = accountRepo;
        this.ledgerRepo = ledgerRepo;
    }

    @Override
    public boolean existsById(UUID accountId) {
        return accountRepo.existsById(accountId);
    }

    @Override
    public BigDecimal sumCredits(UUID accountId) {
        return ledgerRepo.sumCredits(accountId);
    }

    @Override
    public BigDecimal sumDebits(UUID accountId) {
        return ledgerRepo.sumDebits(accountId);
    }

    @Override
    public List<AccountTransactionRow> findAccountTransactions(UUID accountId, int limit, int offset) {
        var query = em.createQuery("""
                select new com.lipa.application.port.out.AccountReadPort$AccountTransactionRow(
                    t.id,
                    cast(t.type as string),
                    cast(t.status as string),
                    t.amount,
                    t.currency,
                    cast(le.direction as string),
                    t.createdAt,
                    t.description
                )
                from LedgerEntryEntity le
                join le.transaction t
                where le.account.id = :accountId
                order by t.createdAt desc, t.id desc
                """, AccountTransactionRow.class);

        query.setParameter("accountId", accountId);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }

    @Override
    public List<LedgerEntryRow> findAccountLedger(UUID accountId, int limit, int offset) {
        var query = em.createQuery("""
                select new com.lipa.application.port.out.AccountReadPort$LedgerEntryRow(
                    le.id,
                    le.transaction.id,
                    cast(le.direction as string),
                    le.amount,
                    le.createdAt
                )
                from LedgerEntryEntity le
                where le.account.id = :accountId
                order by le.createdAt desc, le.id desc
                """, LedgerEntryRow.class);

        query.setParameter("accountId", accountId);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }
}
