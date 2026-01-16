package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.ListAccountLedgerUseCase;
import com.lipa.application.port.out.AccountHistoryQueryPort;
import com.lipa.application.port.out.AccountLookupPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListAccountLedgerService implements ListAccountLedgerUseCase {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final AccountLookupPort accountLookup;
    private final AccountHistoryQueryPort historyQuery;

    public ListAccountLedgerService(AccountLookupPort accountLookup,
                                    AccountHistoryQueryPort historyQuery) {
        this.accountLookup = accountLookup;
        this.historyQuery = historyQuery;
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
