package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.AccountHistoryQueryPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AccountHistoryQueryAdapter implements AccountHistoryQueryPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<AccountTransactionRow> findAccountTransactions(UUID accountId, int limit, int offset) {
        // On liste les transactions vues depuis ce compte via ledger_entry.
        // Comme chaque transaction n’a qu’une écriture pour CE compte (DEBIT ou CREDIT),
        // on peut retourner la ligne ledger + infos transaction.
        var query = em.createQuery("""
                select new com.lipa.application.port.out.AccountHistoryQueryPort$AccountTransactionRow(
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
                select new com.lipa.application.port.out.AccountHistoryQueryPort$LedgerEntryRow(
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
