package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.ListAccountLedgerUseCase;
import com.lipa.application.port.out.AccountHistoryQueryPort;
import com.lipa.application.port.out.AccountLedgerAuditPort;
import com.lipa.application.port.out.AccountLookupPort;
import com.lipa.application.port.out.TimeProviderPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class ListAccountLedgerService implements ListAccountLedgerUseCase {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final AccountLookupPort accountLookup;
    private final AccountHistoryQueryPort historyQuery;
    private final AccountLedgerAuditPort audit;
    private final TimeProviderPort time;

    public ListAccountLedgerService(AccountLookupPort accountLookup,
                                    AccountHistoryQueryPort historyQuery,
                                    AccountLedgerAuditPort audit,
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

        var rows = historyQuery.findAccountLedger(accountId, safeLimit, safeOffset);

        var items = rows.stream()
                .map(r -> new Item(
                        r.ledgerEntryId(),
                        r.transactionId(),
                        r.direction(),
                        r.amount(),
                        r.createdAt()
                ))
                .toList();

        int returned = items.size();
        int nextOffset = safeOffset + returned;

        Instant now = time.now();
        audit.record(
                "LEDGER_VIEWED",
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
