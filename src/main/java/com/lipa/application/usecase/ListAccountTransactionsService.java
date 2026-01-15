package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.ListAccountTransactionsUseCase;
import com.lipa.application.port.out.AccountHistoryQueryPort;
import com.lipa.application.port.out.AccountLookupPort;
import com.lipa.application.port.out.AccountTransactionsAuditPort;
import com.lipa.application.port.out.TimeProviderPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class ListAccountTransactionsService implements ListAccountTransactionsUseCase {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 200;

    private final AccountLookupPort accountLookup;
    private final AccountHistoryQueryPort historyQuery;
    private final AccountTransactionsAuditPort audit;
    private final TimeProviderPort time;

    public ListAccountTransactionsService(AccountLookupPort accountLookup,
                                          AccountHistoryQueryPort historyQuery,
                                          AccountTransactionsAuditPort audit,
                                          TimeProviderPort time) {
        this.accountLookup = accountLookup;
        this.historyQuery = historyQuery;
        this.audit = audit;
        this.time = time;
    }

    @Override
    @Transactional(readOnly = true)
    public Result list(UUID accountId, int limit, int offset) {
        if (accountId == null) {
            throw new BusinessRuleException("accountId is required");
        }

        int safeLimit = normalizeLimit(limit);
        int safeOffset = normalizeOffset(offset);

        if (!accountLookup.existsById(accountId)) {
            throw new NotFoundException("Account not found id=" + accountId);
        }

        var rows = historyQuery.findAccountTransactions(accountId, safeLimit, safeOffset);

        var items = rows.stream()
                .map(r -> new Item(
                        r.transactionId(),
                        r.type(),
                        r.status(),
                        r.amount(),
                        r.currency(),
                        r.direction(),
                        r.createdAt(),
                        r.description()
                ))
                .toList();

        int returned = items.size();
        int nextOffset = safeOffset + returned;

        Instant now = time.now();
        audit.record(
                "ACCOUNT_TRANSACTIONS_VIEWED",
                accountId,
                Map.of(
                        "limit", String.valueOf(safeLimit),
                        "offset", String.valueOf(safeOffset),
                        "returned", String.valueOf(returned),
                        "nextOffset", String.valueOf(nextOffset)
                ),
                now
        );

        return new Result(accountId, safeLimit, safeOffset, returned, nextOffset, items);
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) return DEFAULT_LIMIT;
        return Math.min(limit, MAX_LIMIT);
    }

    private int normalizeOffset(int offset) {
        return Math.max(offset, 0);
    }
}
